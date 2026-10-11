'use strict'
// Sell limits: a shop buys about 5 stacks of iron at the full price, then the price drops for every seller.
// Uses the default profile at diamond = 1000: iron ingot 80, iron allowance 25 600 (320 ingots).
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer, balance, openShop } = require('../lib/fixture')

const SHOP = 'BotLimit'
const OTHER_SHOP = 'BotLimitOther'
const BOT = 'LimitBot'
const OTHER_BOT = 'LimitBot2'

let rcon
let bot
let other

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  other = await joinServer(OTHER_BOT)
  await resetPlayer(rcon, BOT)
  await resetPlayer(rcon, OTHER_BOT)
  await bot.command('gs profile apply default 1000 confirm')
  await bot.waitForMessage(/Priced \d+ materials/)
  await bot.command(`gs create shop ${SHOP}`)
  await bot.command(`gs create shop ${OTHER_SHOP}`)
  // Earlier runs filled the limits; the shops are back to the full price
  await bot.command(`gs reset limits ${SHOP}`)
  await bot.command(`gs reset limits ${OTHER_SHOP}`)
  await rcon.command(`give ${BOT} iron_ingot 384`)
  await rcon.command(`give ${OTHER_BOT} iron_ingot 8`)
  await sleep(300)
})

after(async () => {
  bot?.closeWindow()
  other?.closeWindow()
  bot?.quit()
  other?.quit()
  rcon?.close()
})

function loreOfIron (player) {
  return player.lore(player.customerSlotsWith('iron_ingot')[0]).join('\n')
}

test('the lore tells how much the shop still buys at the full price', async () => {
  await openShop(rcon, bot, SHOP)
  assert.match(loreOfIron(bot), /Full Price for: 321/)
})

test('5 stacks of iron sell at the full price', async () => {
  const before = await balance(rcon, BOT)
  await openShop(rcon, bot, SHOP)

  for (let i = 0; i < 5; i++)
    await bot.click(bot.customerSlotsWith('iron_ingot')[0], 'shiftLeft')

  assert.equal(bot.count('iron_ingot'), 64)
  assert.equal(await balance(rcon, BOT), before + 320 * 80)
})

test('the sixth stack sells for less', async () => {
  const before = await balance(rcon, BOT)
  await openShop(rcon, bot, SHOP)

  await bot.click(bot.customerSlotsWith('iron_ingot')[0], 'shiftLeft')

  const got = await balance(rcon, BOT) - before
  assert.equal(bot.count('iron_ingot'), 0)
  assert.ok(got < 64 * 80, `64 ingots past the limit should pay under ${64 * 80}, got ${got}`)
  assert.ok(got > 64 * 80 * 0.2, `but over the 20% minimum, got ${got}`)
})

test('another seller gets the lower price too', async () => {
  const before = await balance(rcon, OTHER_BOT)
  await openShop(rcon, other, SHOP)
  assert.match(loreOfIron(other), /Shop is Full: \d+% price/)

  await other.click(other.customerSlotsWith('iron_ingot')[0], 'right')

  const got = await balance(rcon, OTHER_BOT) - before
  assert.ok(got < 8 * 80, `Iron should pay under the full ${8 * 80} after the shop is full, got ${got}`)
})

test('another shop has its own limit', async () => {
  await rcon.command(`give ${OTHER_BOT} iron_ingot 8`)
  await sleep(300)
  const before = await balance(rcon, OTHER_BOT)
  await openShop(rcon, other, OTHER_SHOP)

  await other.click(other.customerSlotsWith('iron_ingot')[0], 'right')

  assert.equal(await balance(rcon, OTHER_BOT), before + 8 * 80)
})

test('/gs limits shows the full group', async () => {
  bot.closeWindow()
  bot.clearMessages()
  await bot.command(`gs limits ${SHOP}`)
  await bot.waitForMessage(/iron: sold [\d,.]+ \/ 25,?600, full/)
})
