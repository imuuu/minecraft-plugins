'use strict'
// ImusAPI's menus in a real client: /ia config with the list of plugins, a settings menu, going back, a toggle and
// a reset to the default saved to config.yml, and that items can't be taken out of a menu or put in.
// The menus' own logic has unit tests (ImusAPI MenusTest); this checks the part that talks to the game.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer } = require('../lib/fixture')

const BOT = 'MenuBot'
const KEEP_USABLE = 3 // the fourth setting of imusTNT
const BACK = 49

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT)
  await rcon.command('ia config imustnt set keep-usable-blocks true')
})

after(async () => {
  await rcon?.command('ia config imustnt set keep-usable-blocks true')
  bot?.quit()
  rcon?.close()
})

function nameOf (item) {
  return item ? JSON.stringify(item.customName ?? '').replace(/§./g, '') : ''
}

function slotNamed (pattern) {
  const window = bot.bot.currentWindow
  return window.slots.slice(0, window.inventoryStart).findIndex(item => pattern.test(nameOf(item)))
}

async function setting (path) {
  const list = (await rcon.command('ia config imustnt list')).replace(/§./g, '')
  return new RegExp(`${path} (\\S+)`).exec(list)?.[1]
}

async function openTntSettings () {
  const list = bot.nextWindow(5000, '/ia config opened nothing')
  await bot.command('ia config')
  await list
  assert.match(bot.windowTitle(), /Plugin settings/)
  const slot = slotNamed(/imusTNT/)
  assert.ok(slot >= 0, 'imusTNT is not in the list')
  await bot.clickForNewWindow(slot)
  assert.match(bot.windowTitle(), /imusTNT/)
}

test('a toggle saves at once and a right click resets it', async () => {
  await openTntSettings()
  assert.match(nameOf(bot.slot(KEEP_USABLE)), /Keep usable blocks/)
  assert.equal(bot.slot(KEEP_USABLE).name, 'lime_dye')

  await bot.click(KEEP_USABLE)
  await bot.until(() => bot.slot(KEEP_USABLE)?.name === 'gray_dye', 3000, 'The toggle did not change')
  assert.equal(await setting('keep-usable-blocks'), 'false')

  await bot.click(KEEP_USABLE, 'right')
  await bot.until(() => bot.slot(KEEP_USABLE)?.name === 'lime_dye', 3000, 'The reset did not show')
  assert.equal(await setting('keep-usable-blocks'), 'true')
  bot.closeWindow()
})

test('back returns to the list', async () => {
  await openTntSettings()
  assert.match(nameOf(bot.slot(BACK)), /Go Back/)
  await bot.clickForNewWindow(BACK)
  assert.match(bot.windowTitle(), /Plugin settings/)
  bot.closeWindow()
})

test('nothing can be taken out of a menu or put in', async () => {
  await rcon.command(`minecraft:clear ${BOT}`)
  await rcon.command(`minecraft:give ${BOT} dirt 5`)
  await bot.until(() => bot.count('dirt') === 5, 3000, 'No dirt')
  await openTntSettings()

  // Taking background panes, with a shift click too (a click on a setting would open its dialog)
  await bot.click(46)
  await bot.click(47, 'shiftLeft')
  await bot.click(48, 'middle')
  bot.closeWindow()
  await sleep(500)
  assert.equal(bot.bot.inventory.items().filter(item => item.name !== 'dirt').length, 0, 'took something out')

  // Putting the bot's dirt in with a shift click
  await openTntSettings()
  const window = bot.bot.currentWindow
  const own = bot.ownSlotsWith('dirt')[0]
  assert.ok(own !== undefined, 'no dirt in the window')
  await bot.click(own, 'shiftLeft')
  const inMenu = window.slots.slice(0, window.inventoryStart).filter(item => item?.name === 'dirt').length
  assert.equal(inMenu, 0, 'dirt went into the menu')
  bot.closeWindow()
  await sleep(500)
  assert.equal(bot.count('dirt'), 5)
})
