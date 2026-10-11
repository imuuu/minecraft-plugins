'use strict'
// /gs add makes a stockable listing that never runs out.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer, balance, setHandPrice, openShop } = require('../lib/fixture')

const SHOP = 'BotStock'
const BOT = 'StockBot'

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT)
  await rcon.command(`give ${BOT} bread 16`)
  await sleep(300)
  await setHandPrice(bot, 2)
  await bot.command(`gs create shop ${SHOP}`)
  bot.clearMessages()
  await bot.command(`gs add hand ${SHOP}`)
  await rcon.command(`clear ${BOT}`)
  await sleep(300)
})

after(async () => {
  bot?.closeWindow()
  bot?.quit()
  rcon?.close()
})

test('/gs add lists the stack and the player keeps it', async () => {
  await bot.waitForMessage(/has been added to/)
  await openShop(rcon, bot, SHOP)
  const [slot] = bot.shopSlotsWith('bread')
  assert.ok(slot !== undefined, 'The bread should be listed')
  assert.equal(bot.amount(slot), 16)
})

test('buying from it never lowers its stock', async () => {
  const before = await balance(rcon, BOT)
  await openShop(rcon, bot, SHOP)
  const [slot] = bot.shopSlotsWith('bread')

  await bot.click(slot, 'right')
  await bot.click(bot.shopSlotsWith('bread')[0], 'shiftRight')

  assert.equal(bot.count('bread'), 8 + 16)
  assert.equal(await balance(rcon, BOT), before - 24 * 10)
  assert.equal(bot.amount(bot.shopSlotsWith('bread')[0]), 16)
})
