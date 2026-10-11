'use strict'
// The default price profile prices every material, scales with the diamond price and sets tags.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer } = require('../lib/fixture')

const BOT = 'ProfileBot'

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT)
})

after(async () => {
  bot?.quit()
  rcon?.close()
})

/**
 * What /g getprice says the item is worth.
 */
async function priceOf (item) {
  await rcon.command(`clear ${BOT}`)
  await rcon.command(`give ${BOT} ${item} 1`)
  await sleep(300)
  bot.bot.setQuickBarSlot(0)
  bot.clearMessages()
  await bot.command('g getprice hand')
  const message = await bot.waitForMessage(/price is:/)
  return Number(/price is:\s*([\d.]+)/.exec(message)[1])
}

test('without confirm the profile only shows what it would do', async () => {
  await bot.command('gs profile apply default 1000 confirm')
  const before = await priceOf('diamond')
  bot.clearMessages()

  await bot.command('gs profile apply default 500')

  await bot.waitForMessage(/would price \d+ materials/)
  await bot.waitForMessage(/Add confirm/)
  assert.equal(await priceOf('diamond'), before)
})

test('the default profile prices diamond at 1000 and crafted items from their recipes', async () => {
  await bot.command('gs profile apply default 1000 confirm')
  await bot.waitForMessage(/Priced \d+ materials/)

  assert.equal(await priceOf('diamond'), 1000)
  assert.equal(await priceOf('iron_ingot'), 80)
  // 9 ingots x 1.1
  assert.equal(await priceOf('iron_block'), 792)
  // a log is 4, it makes 4 planks: 1 x 1.1
  assert.equal(await priceOf('oak_planks'), 1.1)
  assert.equal(await priceOf('pig_spawn_egg'), 0)
})

test('a lower diamond price scales everything', async () => {
  await bot.command('gs profile apply default 100 confirm')
  await bot.waitForMessage(/Priced \d+ materials/)

  assert.equal(await priceOf('diamond'), 100)
  assert.equal(await priceOf('iron_ingot'), 8)
  assert.equal(await priceOf('iron_block'), 79.2)
})

test('profile tags work with /gs tag', async () => {
  await bot.command('gs profile apply default 1000 confirm')
  bot.clearMessages()

  await bot.command('gs tag materials increase_price ores 2')

  await bot.waitForMessage(/with tag 'ores'/)
  assert.equal(await priceOf('iron_ore'), 160)
  assert.equal(await priceOf('iron_ingot'), 80, 'Ingots are not ores')

  // Back to the plain profile for the other tests
  await bot.command('gs profile apply default 1000 confirm')
})
