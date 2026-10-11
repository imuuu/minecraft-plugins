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
  await botA.click(botA.customerSlotsWith('emerald')[0], 'left')
  let [slot] = botA.shopSlotsWith('emerald')
  while (botA.amount(slot) > 1) {
    await botA.click(slot, 'left')
    slot = botA.shopSlotsWith('emerald')[0]
  }
  assert.equal(botA.amount(slot), 1)
  await resetPlayer(rcon, A)

  await openShop(rcon, botA, SHOP)
  await openShop(rcon, botB, SHOP)
  const slotA = botA.shopSlotsWith('emerald')[0]
  const slotB = botB.shopSlotsWith('emerald')[0]
  assert.ok(slotA !== undefined && slotB !== undefined, 'Both should see the emerald')

  await Promise.all([botA.click(slotA, 'left'), botB.click(slotB, 'left')])
  await sleep(500)

  const emeralds = botA.count('emerald') + botB.count('emerald')
  const spent = (1000 - await balance(rcon, A)) + (1000 - await balance(rcon, B))
  assert.equal(emeralds, 1, 'Exactly one emerald should have been bought')
  assert.equal(spent, 25, 'Only the buyer should have paid')
  assert.equal(botA.shopSlotsWith('emerald').length, 0, 'A should see it sold out')
  assert.equal(botB.shopSlotsWith('emerald').length, 0, 'B should see it sold out')
})
