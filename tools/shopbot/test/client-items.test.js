'use strict'
// Items shown on the player's screen only (ImusAPI ClientItems): a shop covers the player's own inventory with black
// panes, and the enchanting table blinks the items that fit it as lime panes. The real items never change and are
// back on the screen when the menu closes. Not in creative, where the client decides what its inventory holds.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Vec3 } = require('vec3')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer, openShop } = require('../lib/fixture')
const { count } = require('../lib/enchant-fixture')

const BOT = 'FakeBot'
const SHOP = 'FakeShop'
const X = 640
const Z = 300
const Y = 100

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT)
  await rcon.command(`minecraft:give ${BOT} diamond 5`)
  await bot.command(`gs create shop ${SHOP}`)
  await sleep(500)
})

after(async () => {
  bot?.closeWindow()
  await rcon?.command(`gamemode survival ${BOT}`)
  bot?.quit()
  rcon?.close()
})

/**
 * The names of the items in the open window's part for the bot's own inventory.
 */
function ownSlots () {
  const window = bot.bot.currentWindow
  return window.slots.slice(window.inventoryStart, window.inventoryEnd).map(item => item?.name ?? null)
}

test('a shop covers the own inventory on the screen, and the items are back after', async () => {
  await openShop(rcon, bot, SHOP, { includeHotbar: false })
  await bot.until(() => ownSlots().every(name => name === 'black_stained_glass_pane'), 3000,
    () => `own slots: ${ownSlots().filter(Boolean).join(', ')}`)
  assert.equal(await count(rcon, BOT, 'diamond'), 5, 'the real diamonds are still there')

  bot.closeWindow()
  await bot.until(() => bot.count('diamond') === 5 && bot.count('black_stained_glass_pane') === 0, 3000,
    () => `after closing the bot has ${bot.bot.inventory.items().map(item => item.name).join(', ')}`)
})

test('in creative nothing is covered', async () => {
  await rcon.command(`gamemode creative ${BOT}`)
  try {
    await openShop(rcon, bot, SHOP, { includeHotbar: false })
    await sleep(1500)
    assert.ok(ownSlots().includes('diamond'), 'the diamonds show')
    assert.ok(!ownSlots().includes('black_stained_glass_pane'))
    bot.closeWindow()
  } finally {
    await rcon.command(`gamemode survival ${BOT}`)
  }
})

test('the enchanting table blinks the items that fit it', async () => {
  await rcon.command(`minecraft:clear ${BOT}`)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await sleep(1500)
  await rcon.command(`fill ${X - 3} ${Y - 1} ${Z - 3} ${X + 3} ${Y - 1} ${Z + 3} stone`)
  await rcon.command(`fill ${X - 3} ${Y} ${Z - 3} ${X + 3} ${Y + 3} ${Z + 3} air`)
  await rcon.command(`setblock ${X + 2} ${Y} ${Z} enchanting_table`)
  await bot.until(() => bot.bot.blockAt(new Vec3(X + 2, Y, Z))?.name === 'enchanting_table', 5000, 'The bot does not see the table')

  // A pickaxe with slots in hand opens the table with it inside; a book and dirt stay in the inventory
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
  await rcon.command(`ien book tunnel 1 ${BOT}`)
  await rcon.command(`minecraft:give ${BOT} dirt 3`)
  await bot.until(() => bot.count('enchanted_book') === 1 && bot.count('dirt') === 3, 3000, 'No book and dirt')

  const opened = bot.nextWindow(5000, 'The table opened nothing')
  await bot.bot.activateBlock(bot.bot.blockAt(new Vec3(X + 2, Y, Z)))
  await opened

  // The book blinks lime, the dirt doesn't fit and stays
  await bot.until(() => ownSlots().includes('lime_stained_glass_pane'), 4000, () => `own slots: ${ownSlots().filter(Boolean).join(', ')}`)
  assert.ok(!ownSlots().includes('enchanted_book'), 'the book shows as a pane')
  assert.ok(ownSlots().includes('dirt'), 'the dirt shows as itself')
  await bot.until(() => ownSlots().includes('enchanted_book'), 2000, 'The book did not come back')
  assert.equal(await count(rcon, BOT, 'enchanted_book'), 1, 'the real book never changed')

  bot.closeWindow()
  await bot.until(() => bot.count('enchanted_book') === 1 && bot.count('lime_stained_glass_pane') === 0, 3000, 'No book after closing')
})
