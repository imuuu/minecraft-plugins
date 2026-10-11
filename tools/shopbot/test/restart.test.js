'use strict'
// Shops, their stock and material prices survive a server restart. Restarts the server, so it only runs with
// SHOPBOT_RESTART=1. Run it after the other tests, which fill the shops.
const { test, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { openShop } = require('../lib/fixture')
const server = require('../lib/server')

const BOT = 'RestartBot'
const run = process.env.SHOPBOT_RESTART === '1'

let rcon
let bot

after(async () => {
  bot?.closeWindow()
  bot?.quit()
  rcon?.close()
})

/**
 * What a player sees of the shops: item and amount of every listing on the first page, per shop.
 */
async function snapshot (shops) {
  const seen = {}
  for (const shop of shops) {
    await openShop(rcon, bot, shop, { includeHotbar: false })
    const listings = []
    for (let slot = 0; slot < 27; slot++) {
      const item = bot.slot(slot)
      if (item) listings.push(`${slot}:${item.name}x${bot.amount(slot)}`)
    }
    seen[shop] = listings.join(' ')
  }
  bot.closeWindow()
  bot.clearMessages()
  await bot.command('g getprice hand')
  return seen
}

test('shops, stock and prices are the same after a restart', { skip: !run && 'set SHOPBOT_RESTART=1 to restart the server' }, async () => {
  const shops = ['BotTest', 'BotStock', 'BotShulker']
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await rcon.command(`op ${BOT}`)
  await rcon.command(`clear ${BOT}`)
  await rcon.command(`give ${BOT} diamond 1`)
  await sleep(300)
  bot.bot.setQuickBarSlot(0)
  const before = await snapshot(shops)
  const priceBefore = await bot.waitForMessage(/DIAMOND price is/)

  bot.quit()
  rcon.close()
  await server.stop()
  await server.start()

  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await sleep(500)
  bot.bot.setQuickBarSlot(0)
  const afterRestart = await snapshot(shops)
  const priceAfter = await bot.waitForMessage(/DIAMOND price is/)

  assert.deepEqual(afterRestart, before)
  assert.equal(priceAfter, priceBefore)
  for (const shop of shops)
    assert.ok(before[shop].length > 0, shop + ' should have had listings to compare')
})
