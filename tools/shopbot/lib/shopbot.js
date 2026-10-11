'use strict'
// A test player on the local server, with helpers for the imusGS shop menu.
const mineflayer = require('mineflayer')
const nbt = require('prismarine-nbt')
const { serverProperties } = require('./rcon')

// Mineflayer speaks 26.1; ViaBackwards on the test server lets it join the 26.2 server
const PROTOCOL_VERSION = '26.1'

const SHOP_SLOTS = 27
const CUSTOMER_START = 36

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
   * Waits for a menu to open and returns it.
   */
  async waitForWindow (timeoutMs = 5000) {
    if (this.bot.currentWindow) return this.bot.currentWindow
    return await new Promise((resolve, reject) => {
      const timer = setTimeout(() => reject(new Error('No menu opened')), timeoutMs)
      this.bot.once('windowOpen', window => { clearTimeout(timer); resolve(window) })
    })
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
    await this.bot.clickWindow(slot, button, mode)
    // The plugin answers on the next ticks, and its click cooldown is 0.2 s by default
    await sleep(400)
  }

  /**
   * Clicks a slot that opens another menu, and returns that menu.
   */
  async clickForNewWindow (slot, click = 'left', timeoutMs = 5000) {
    const opened = new Promise((resolve, reject) => {
      const timer = setTimeout(() => reject(new Error(`Clicking slot ${slot} opened no menu`)), timeoutMs)
      this.bot.once('windowOpen', window => { clearTimeout(timer); resolve(window) })
    })
    await this.click(slot, click)
    const window = await opened
    await sleep(300)
    return window
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

module.exports = { joinServer, ShopBot, sleep, SHOP_SLOTS, CUSTOMER_START }
