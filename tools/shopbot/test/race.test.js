'use strict'
// Two players click the last unit of a listing at the same time: only one may get it and pay.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer, balance, setHandPrice, openShop } = require('../lib/fixture')

const SHOP = 'BotRace'
const A = 'RaceBotA'
const B = 'RaceBotB'

let rcon
let botA
let botB

before(async () => {
  rcon = await Rcon.connect()
  botA = await joinServer(A)
  botB = await joinServer(B)
  await resetPlayer(rcon, A)
  await resetPlayer(rcon, B)
  await rcon.command(`give ${A} emerald 1`)
  await sleep(300)
  // Emeralds are worth 5, so one costs 25 to buy
  await setHandPrice(botA, 5)
  await botA.command(`gs create shop ${SHOP}`)
})

after(async () => {
  for (const bot of [botA, botB]) {
    bot?.closeWindow()
    bot?.quit()
  }
  rcon?.close()
})

test('only one of two players gets the last emerald', async () => {
  // A sells its emerald, then buys back anything over one so exactly one is left
  await openShop(rcon, botA, SHOP)
  await botA.click(await botA.waitForCustomerSlot('emerald'), 'left')
  let slot = await botA.waitForShopSlot('emerald')
  while (botA.amount(slot) > 1) {
    await botA.click(slot, 'left')
    slot = await botA.waitForShopSlot('emerald')
  }
  assert.equal(botA.amount(slot), 1)
  await resetPlayer(rcon, A)

  await openShop(rcon, botA, SHOP)
  await openShop(rcon, botB, SHOP)
  const slotA = await botA.waitForShopSlot('emerald')
  const slotB = await botB.waitForShopSlot('emerald')
  assert.ok(slotA !== undefined && slotB !== undefined, 'Both should see the emerald')

  await Promise.all([botA.click(slotA, 'left'), botB.click(slotB, 'left')])
  // The other bot's menu shows the sale when the shop redraws for its viewers
  await botA.until(() => botA.shopSlotsWith('emerald').length === 0, 3000, 'A should see it sold out')
  await botB.until(() => botB.shopSlotsWith('emerald').length === 0, 3000, 'B should see it sold out')

  const emeralds = botA.count('emerald') + botB.count('emerald')
  const spent = (1000 - await balance(rcon, A)) + (1000 - await balance(rcon, B))
  assert.equal(emeralds, 1, 'Exactly one emerald should have been bought')
  assert.equal(spent, 25, 'Only the buyer should have paid')
})
