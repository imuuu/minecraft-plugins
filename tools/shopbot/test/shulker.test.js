'use strict'
// Selling a shulker box sells everything in it; the box stays with the player, empty.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer, balance, setHandPrice, openShop } = require('../lib/fixture')

const SHOP = 'BotShulker'
const BOT = 'ShulkerBot'
const SHULKER_TAB = 2

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT)
  await rcon.command(`give ${BOT} gold_ingot 1`)
  await sleep(300)
  await setHandPrice(bot, 4)
  await rcon.command(`clear ${BOT}`)
  await bot.command(`gs create shop ${SHOP}`)
  // Essentials replaces /give and doesn't understand item components; the vanilla one does
  await rcon.command(`minecraft:give ${BOT} shulker_box[container=[{slot:0,item:{id:"minecraft:gold_ingot",count:10}},{slot:1,item:{id:"minecraft:gold_ingot",count:5}}]]`)
  await sleep(300)
})

after(async () => {
  bot?.closeWindow()
  bot?.quit()
  rcon?.close()
})

test('the shulker box is listed on its own tab with the price of its contents', async () => {
  await openShop(rcon, bot, SHOP, { tab: SHULKER_TAB })
  const [slot] = bot.customerSlotsWith('shulker_box')
  assert.ok(slot !== undefined, 'The shulker box should be on the shulker tab')
  assert.match(bot.lore(slot).join('\n'), /Sell Content with 60\.0 \$/)
})

test('selling it pays for the contents, fills the shop and keeps the empty box', async () => {
  const before = await balance(rcon, BOT)
  await openShop(rcon, bot, SHOP, { tab: SHULKER_TAB })
  const goldBefore = bot.shopSlotsWith('gold_ingot').map(slot => bot.amount(slot))[0] ?? 0

  await bot.click(bot.customerSlotsWith('shulker_box')[0], 'left')

  assert.equal(await balance(rcon, BOT), before + 60)
  assert.equal(bot.count('shulker_box'), 1, 'The box stays with the player')
  assert.equal(bot.amount(bot.shopSlotsWith('gold_ingot')[0]), goldBefore + 15)
  assert.equal(bot.customerSlotsWith('shulker_box').length, 0, 'The empty box can no longer be sold')
})
