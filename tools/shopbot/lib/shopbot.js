'use strict'
// A test player on the local server, with helpers for the imusGS shop menu.
const mineflayer = require('mineflayer')
const nbt = require('prismarine-nbt')
const { serverProperties } = require('./rcon')

// The prismarine-item that mineflayer uses, for the item format of its protocol version
const ITEM_MODULE = require.resolve('prismarine-item', { paths: [require.resolve('mineflayer')] })

// Mineflayer speaks 26.1; ViaBackwards on the test server lets it join the 26.2 server
const PROTOCOL_VERSION = '26.1'

const SHOP_SLOTS = 27
const CUSTOMER_START = 36

// Packets that change what the bot sees in a menu or its inventory
const INVENTORY_PACKETS = new Set(['open_window', 'close_window', 'window_items', 'set_slot', 'set_cursor_item', 'set_player_inventory'])
// A menu counts as settled when no inventory packet has come for this long
const QUIET_MS = 100
// Upper bound for settling: the shop's flash button blinks every 0.5 s and never goes quiet for long
const SETTLE_MAX_MS = 1500
// The shop's click cooldown is 0.2 s by default (shop.click-cooldown-seconds), counted from when the server
// handled the click; a click that comes sooner is ignored
const CLICK_GAP_MS = 250

/**
 * Text of a chat component (already simplified from NBT), colours left out.
 */
function plainText (component) {
  if (component == null) return ''
  if (typeof component === 'string') return component.replace(/§./g, '')
  if (Array.isArray(component)) return component.map(plainText).join('')
  return plainText(component.text ?? '') + plainText(component.extra ?? [])
}

function sleep (ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * Calls check until it returns something other than undefined, null or false, and returns that. Throws
 * message (a string or a function giving one) when timeoutMs passes first.
 */
async function waitUntil (check, timeoutMs = 3000, message = 'Timed out') {
  const deadline = Date.now() + timeoutMs
  for (;;) {
    const value = check()
    if (value !== undefined && value !== null && value !== false) return value
    if (Date.now() >= deadline) throw new Error(typeof message === 'function' ? message() : message)
    await sleep(50)
  }
}

/**
 * Joins the test server as {@code name} (offline mode) and resolves when the bot is in the world.
 */
async function joinServer (name) {
  const props = serverProperties()
  const bot = mineflayer.createBot({
    host: props['server-ip'] || '127.0.0.1',
    port: Number(props['server-port'] || 25565),
    username: name,
    auth: 'offline',
    version: PROTOCOL_VERSION
  })
  await new Promise((resolve, reject) => {
    bot.once('spawn', resolve)
    bot.once('kicked', reason => reject(new Error('Kicked: ' + JSON.stringify(reason))))
    bot.once('error', reject)
  })
  return new ShopBot(bot)
}

class ShopBot {
  constructor (bot) {
    this.bot = bot
    this.messages = []
    bot.on('messagestr', message => this.messages.push(message))
    this._Item = require(ITEM_MODULE)(bot.registry)
    this._lastInventoryPacket = 0
    this._lastClick = 0
    // Mineflayer sets currentWindow on open_window but emits windowOpen only once the window_items came
    this._openedWindows = new WeakSet()
    bot.on('windowOpen', window => this._openedWindows.add(window))
    bot._client.on('packet', (data, meta) => {
      if (INVENTORY_PACKETS.has(meta.name)) this._lastInventoryPacket = Date.now()
    })
  }

  get name () {
    return this.bot.username
  }

  quit () {
    this.bot.quit()
  }

  /**
   * Sends a chat command (without the slash) and waits for the server to answer.
   */
  async command (text, waitMs = 500) {
    this.bot.chat('/' + text)
    await sleep(waitMs)
  }

  /**
   * Waits for a chat message matching the pattern, also one that already came.
   */
  async waitForMessage (pattern, timeoutMs = 5000) {
    const deadline = Date.now() + timeoutMs
    while (Date.now() < deadline) {
      const found = this.messages.find(m => pattern.test(m))
      if (found) return found
      await sleep(100)
    }
    throw new Error('No message matching ' + pattern + '. Last messages:\n' + this.messages.slice(-10).join('\n'))
  }

  clearMessages () {
    this.messages.length = 0
  }

  /**
   * Calls check until it gives a value (see waitUntil) and returns it.
   */
  async until (check, timeoutMs = 3000, message = 'Timed out') {
    return await waitUntil(check, timeoutMs, message)
  }

  /**
   * Waits until the server has sent everything it has for the open menu. The plugins open a menu first and
   * fill it after, so the window_items that comes with open_window can be empty. A click with an impossible
   * state id makes the server send the whole menu as it is after everything it handled before; then this
   * waits for the inventory packets to stop, for changes the plugin makes on later ticks.
   */
  async settle () {
    const window = this.bot.currentWindow
    if (window && this._openedWindows.has(window)) await this._syncWindow(window)
    const deadline = Date.now() + SETTLE_MAX_MS
    while (Date.now() - this._lastInventoryPacket < QUIET_MS && Date.now() < deadline) await sleep(25)
  }

  /**
   * Asks the server for the whole menu and waits for it. Also ends when the menu changes, since the server
   * doesn't answer for a menu it already closed.
   */
  _syncWindow (window, timeoutMs = 2000) {
    return new Promise(resolve => {
      const event = `setWindowItems:${window.id}`
      const done = () => {
        clearTimeout(timer)
        this.bot.removeListener(event, done)
        this.bot.removeListener('windowOpen', done)
        this.bot.removeListener('windowClose', done)
        resolve()
      }
      const timer = setTimeout(done, timeoutMs)
      this.bot.on(event, done)
      this.bot.on('windowOpen', done)
      this.bot.on('windowClose', done)
      // What mineflayer's bot._syncWindow sends. That one waits for the answer with a 20 s timer it never
      // clears, which keeps the test process alive after the tests.
      this.bot._client.write('window_click', {
        windowId: window.id,
        stateId: -1,
        slot: -999,
        mouseButton: 2, // end of a drag that never started: changes nothing
        mode: 5,
        changedSlots: [],
        cursorItem: this._Item.toNotch(window.selectedItem)
      })
    })
  }

  /**
   * Waits for the next menu to open, with its contents, and returns it.
   */
  nextWindow (timeoutMs = 5000, message = 'No menu opened') {
    const opened = new Promise((resolve, reject) => {
      const onOpen = window => { clearTimeout(timer); resolve(window) }
      const timer = setTimeout(() => {
        this.bot.removeListener('windowOpen', onOpen)
        reject(new Error(message))
      }, timeoutMs)
      this.bot.once('windowOpen', onOpen)
    }).then(async window => {
      await this.settle()
      return window
    })
    // The caller may await it only after a click that throws; that must not be an unhandled rejection
    opened.catch(() => {})
    return opened
  }

  /**
   * Waits for a menu to be open, with its contents, and returns it.
   */
  async waitForWindow (timeoutMs = 5000) {
    const window = this.bot.currentWindow
    if (window && this._openedWindows.has(window)) {
      await this.settle()
      return this.bot.currentWindow ?? window
    }
    return await this.nextWindow(timeoutMs)
  }

  windowTitle () {
    const window = this.bot.currentWindow
    if (!window) return null
    try {
      return typeof window.title === 'string' ? window.title : JSON.stringify(window.title)
    } catch {
      return String(window.title)
    }
  }

  /**
   * Clicks a menu slot like a player. click: 'left', 'right', 'shiftLeft', 'shiftRight', 'middle'.
   */
  async click (slot, click = 'left') {
    const modes = {
      left: [0, 0],
      right: [1, 0],
      shiftLeft: [0, 1],
      shiftRight: [1, 1],
      middle: [2, 3]
    }
    const [button, mode] = modes[click]
    if (!Number.isInteger(slot)) throw new Error(`No slot to click (${slot}) in ${this.windowTitle()}`)
    // Clicking with no menu open would click the bot's own inventory
    const window = this.bot.currentWindow
    if (!window || !this._openedWindows.has(window)) await this.waitForWindow()
    const wait = this._lastClick + CLICK_GAP_MS - Date.now()
    if (wait > 0) await sleep(wait)
    await this.bot.clickWindow(slot, button, mode)
    await this.settle()
    this._lastClick = Date.now()
  }

  /**
   * Clicks a slot that opens another menu, and returns that menu with its contents.
   */
  async clickForNewWindow (slot, click = 'left', timeoutMs = 5000) {
    const opened = this.nextWindow(timeoutMs, `Clicking slot ${slot} opened no menu`)
    await this.click(slot, click)
    return await opened
  }

  /**
   * Slots of the open menu that show the bot's own inventory and hold the item.
   */
  ownSlotsWith (itemName) {
    const window = this.bot.currentWindow
    if (!window) return []
    const result = []
    for (let slot = window.inventoryStart; slot < window.inventoryEnd; slot++) {
      const item = window.slots[slot]
      if (item && item.name === itemName) result.push(slot)
    }
    return result
  }

  closeWindow () {
    if (this.bot.currentWindow) this.bot.closeWindow(this.bot.currentWindow)
  }

  /**
   * The item in a slot of the open menu, or null.
   */
  slot (slot) {
    const window = this.bot.currentWindow
    return window ? window.slots[slot] : null
  }

  /**
   * Shop side slots (0-26) holding the item, e.g. 'diamond'.
   */
  shopSlotsWith (itemName) {
    const result = []
    for (let slot = 0; slot < SHOP_SLOTS; slot++) {
      const item = this.slot(slot)
      if (item && item.name === itemName) result.push(slot)
    }
    return result
  }

  /**
   * Customer side slots (36-53) holding the item.
   */
  customerSlotsWith (itemName) {
    const result = []
    for (let slot = CUSTOMER_START; slot < CUSTOMER_START + 18; slot++) {
      const item = this.slot(slot)
      if (item && item.name === itemName) result.push(slot)
    }
    return result
  }

  /**
   * The first shop side slot holding the item, waiting for it to show up.
   */
  async waitForShopSlot (itemName, timeoutMs = 3000) {
    return await this.until(() => this.shopSlotsWith(itemName)[0], timeoutMs,
      () => `No ${itemName} on the shop side of ${this.windowTitle()}`)
  }

  /**
   * The first customer side slot holding the item, waiting for it to show up.
   */
  async waitForCustomerSlot (itemName, timeoutMs = 3000) {
    return await this.until(() => this.customerSlotsWith(itemName)[0], timeoutMs,
      () => `No ${itemName} on the customer side of ${this.windowTitle()}`)
  }

  /**
   * The first slot of the bot's own inventory part of the menu holding the item, waiting for it to show up.
   */
  async waitForOwnSlot (itemName, timeoutMs = 3000) {
    return await this.until(() => this.ownSlotsWith(itemName)[0], timeoutMs,
      () => `No ${itemName} in the bot's items in ${this.windowTitle()}`)
  }

  /**
   * Lore lines of a menu slot, as plain text.
   */
  lore (slot) {
    const item = this.slot(slot)
    if (!item) return []
    const lore = item.customLore ?? []
    return lore.map(line => plainText(typeof line === 'string' ? line : nbt.simplify(line)))
  }

  /**
   * The amount a shop menu item shows in its lore ("____ Amount: 24 ____"), or null.
   */
  amount (slot) {
    for (const line of this.lore(slot)) {
      const match = /Amount:\s*(\d+)/.exec(line)
      if (match) return Number(match[1])
    }
    return null
  }

  /**
   * How many of the item the bot has in its own inventory.
   */
  count (itemName) {
    return this.bot.inventory.items().filter(item => item.name === itemName).reduce((sum, item) => sum + item.count, 0)
  }
}

module.exports = { joinServer, ShopBot, sleep, waitUntil, SHOP_SLOTS, CUSTOMER_START }
