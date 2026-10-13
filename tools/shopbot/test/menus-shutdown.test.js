'use strict'
// When the server stops with a menu open, the items in it come back: an enchanting table holding a pickaxe and
// an unsaved book is open while the server stops, and both are in the player's inventory after it starts again.
// Restarts the server, so it only runs with SHOPBOT_RESTART=1.
const { test, after } = require('node:test')
const assert = require('node:assert/strict')
const { Vec3 } = require('vec3')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer } = require('../lib/fixture')
const server = require('../lib/server')
const { count } = require('../lib/enchant-fixture')

const BOT = 'ShutBot'
const X = 620
const Z = 300
const Y = 100
const run = process.env.SHOPBOT_RESTART === '1'

let rcon
let bot

after(async () => {
  bot?.quit()
  rcon?.close()
})

function has (name) {
  return bot.bot.inventory.items().some(item => item.name === name)
}

test('items in an open enchanting table come back when the server stops', { skip: !run && 'set SHOPBOT_RESTART=1 to restart the server' }, async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await sleep(1500)
  await rcon.command(`fill ${X - 3} ${Y - 1} ${Z - 3} ${X + 3} ${Y - 1} ${Z + 3} stone`)
  await rcon.command(`fill ${X - 3} ${Y} ${Z - 3} ${X + 3} ${Y + 3} ${Z + 3} air`)
  await rcon.command(`setblock ${X + 2} ${Y} ${Z} enchanting_table`)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await bot.until(() => bot.bot.blockAt(new Vec3(X + 2, Y, Z))?.name === 'enchanting_table', 5000, 'The bot does not see the table')

  // A pickaxe with slots, and a book for its grid
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
  await bot.until(() => has('enchanted_book'), 3000, 'No book given')

  const opened = bot.nextWindow(5000, 'The table opened no grid')
  await bot.bot.activateBlock(bot.bot.blockAt(new Vec3(X + 2, Y, Z)))
  const window = await opened
  const open = [...Array(45).keys()].find(slot => !window.slots[slot])
  await bot.click(await bot.waitForOwnSlot('enchanted_book'))
  await bot.click(open)
  await bot.until(() => bot.slot(open)?.name === 'enchanted_book', 3000, 'The book did not go in')
  // Counted on the server: the bot's own view of its inventory doesn't change while a window is open
  assert.equal(await count(rcon, BOT, 'diamond_pickaxe'), 0, 'the pickaxe is in the table')
  assert.equal(await count(rcon, BOT, 'enchanted_book'), 0, 'the book is in the table')

  // The server stops with the table open and the player still in it
  rcon.close()
  rcon = null
  await server.stop()
  try {
    bot.quit()
  } catch {
    // Already disconnected by the server
  }
  bot = null
  await server.start()

  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  assert.equal(await count(rcon, BOT, 'diamond_pickaxe'), 1, 'the pickaxe came back')
  assert.equal(await count(rcon, BOT, 'enchanted_book'), 1, 'the unsaved book came back')
})
