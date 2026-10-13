'use strict'
// ImusAPI's menus in a real client: /ia config with the list of plugins, a settings menu, going back, a toggle and
// a reset to the default saved to config.yml, and that items can't be taken out of a menu or put in. Also the
// menus no other test opens: /ien admin, the enchanting table's shop and /waystones list.
// The menus' own logic has unit tests (ImusAPI MenusTest); this checks the part that talks to the game.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Vec3 } = require('vec3')
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

test('/ien admin switches an enchant off and on', async () => {
  await rcon.command(`op ${BOT}`)
  const admin = bot.nextWindow(5000, '/ien admin opened nothing')
  await bot.command('ien admin')
  await admin
  // The custom tab, then its first enchant
  await bot.click(5)
  await bot.until(() => /Selected/.test(JSON.stringify(bot.lore(5))), 3000, 'The custom tab did not open')
  const name = nameOf(bot.slot(9))
  await bot.click(9)
  await bot.until(() => bot.slot(9)?.name === 'gray_dye', 3000, 'The enchant did not switch off')
  await bot.click(9)
  await bot.until(() => bot.slot(9)?.name === 'enchanted_book', 3000, 'The enchant did not switch on')
  assert.equal(nameOf(bot.slot(9)), name)
  bot.closeWindow()
})

test('the enchanting table\'s shop sells a booster for levels and goes back to the table', async () => {
  const X = 600
  const Z = 300
  const Y = 100
  await rcon.command(`minecraft:clear ${BOT}`)
  await rcon.command(`gamemode survival ${BOT}`)
  // The chunks load around the bot first
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await sleep(1500)
  await rcon.command(`fill ${X - 3} ${Y - 1} ${Z - 3} ${X + 3} ${Y - 1} ${Z + 3} stone`)
  await rcon.command(`fill ${X - 3} ${Y} ${Z - 3} ${X + 3} ${Y + 3} ${Z + 3} air`)
  await rcon.command(`setblock ${X + 2} ${Y} ${Z} enchanting_table`)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await bot.until(() => bot.bot.blockAt(new Vec3(X + 2, Y, Z))?.name === 'enchanting_table', 5000, 'The bot does not see the table')

  // The table opens for an item with slots: a Slot Core clicked on a pickaxe
  await rcon.command(`minecraft:item replace entity ${BOT} hotbar.0 with diamond_pickaxe`)
  bot.bot.setQuickBarSlot(0)
  await rcon.command(`ien core ${BOT}`)
  await bot.until(() => bot.bot.inventory.items().length >= 2, 3000, 'No core given')
  const core = bot.bot.inventory.items().find(item => item.name !== 'diamond_pickaxe')
  bot.clearMessages()
  await bot.bot.clickWindow(core.slot, 0, 0)
  await sleep(300)
  await bot.bot.clickWindow(36, 0, 0)
  await bot.waitForMessage(/given slots/)
  await rcon.command(`minecraft:xp set ${BOT} 50 levels`)
  await bot.until(() => bot.bot.experience.level === 50, 3000, 'No levels')

  const table = bot.nextWindow(5000, 'The table opened nothing')
  await bot.bot.activateBlock(bot.bot.blockAt(new Vec3(X + 2, Y, Z)))
  await table
  await bot.clickForNewWindow(48)
  assert.match(bot.windowTitle(), /Buy/)

  // A tier 1 booster costs 10 levels
  await bot.click(4)
  await bot.until(() => bot.bot.experience.level === 40, 3000, () => `Level ${bot.bot.experience.level}`)
  // Back opens the table again, empty: the pickaxe came back when the shop opened
  await bot.clickForNewWindow(45)
  assert.match(bot.windowTitle(), /Enchanting Table/)
  bot.closeWindow()
  await bot.until(() => bot.bot.inventory.items().some(item => item.name === 'diamond_pickaxe'), 3000, 'The pickaxe is gone')
  await bot.until(() => bot.bot.inventory.items().some(item => item.name === 'netherite_upgrade_smithing_template'), 3000, 'No booster')
})

test('/waystones list opens', async () => {
  const list = bot.nextWindow(5000, '/waystones list opened nothing')
  await bot.command('waystones list')
  await list
  assert.match(bot.windowTitle(), /Waystones \(\d+\)/)
  bot.closeWindow()
})
