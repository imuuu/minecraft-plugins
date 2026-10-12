'use strict'
// Buying and selling with every click type. The local test server (run/) must be running.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer, balance, setHandPrice, openShop, HOTBAR_TOGGLE } = require('../lib/fixture')

const SHOP = 'BotTest'
const BOT = 'ShopBot'

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT)
  await rcon.command(`give ${BOT} diamond 64`)
  await sleep(300)
  // Diamonds are worth 10; a new shop sells for x5 and buys for x1
  await setHandPrice(bot, 10)
  await bot.command(`gs create shop ${SHOP}`)
})

after(async () => {
  bot?.closeWindow()
  bot?.quit()
  rcon?.close()
})

const open = options => openShop(rcon, bot, SHOP, options)
const money = () => balance(rcon, BOT)

test('the shop opens with its name as the title', async () => {
  await open()
  assert.match(bot.windowTitle(), new RegExp(SHOP))
})

test('the hotbar is left out until its button is clicked', async () => {
  await open({ includeHotbar: false })
  assert.equal(bot.customerSlotsWith('diamond').length, 0)
  await bot.click(HOTBAR_TOGGLE)
  assert.equal(bot.customerSlotsWith('diamond').length, 1)
})

test('left click sells one diamond for 10 and the shop lists it', async () => {
  const before = await money()
  await open()
  const shopAmountBefore = bot.shopSlotsWith('diamond').map(slot => bot.amount(slot))[0] ?? 0

  await bot.click(await bot.waitForCustomerSlot('diamond'), 'left')

  assert.equal(bot.count('diamond'), 63)
  assert.equal(await money(), before + 10)
  const shopSlot = await bot.waitForShopSlot('diamond')
  assert.equal(bot.amount(shopSlot), shopAmountBefore + 1)
})

test('the lore shows the buy prices', async () => {
  await open()
  const lore = bot.lore(await bot.waitForShopSlot('diamond')).join('\n')
  assert.match(lore, /BUY\s+:\s+1\s+:\s+50\.0 \$/)
})

test('left click buys one diamond for 50', async () => {
  const before = await money()
  await open()

  await bot.click(await bot.waitForShopSlot('diamond'), 'left')

  assert.equal(bot.count('diamond'), 64)
  assert.equal(await money(), before - 50)
})

test('right click sells 8', async () => {
  const before = await money()
  await open()

  await bot.click(await bot.waitForCustomerSlot('diamond'), 'right')

  assert.equal(bot.count('diamond'), 56)
  assert.equal(await money(), before + 80)
})

test('shift + right click sells everything', async () => {
  const before = await money()
  await open()

  await bot.click(await bot.waitForCustomerSlot('diamond'), 'shiftRight')

  assert.equal(bot.count('diamond'), 0)
  assert.equal(await money(), before + 560)
})

test('shift + left click buys 64', async () => {
  await rcon.command(`eco set ${BOT} 5000`)
  await open()
  const slot = await bot.waitForShopSlot('diamond')
  assert.ok(bot.amount(slot) >= 64, 'The shop should have at least 64 diamonds by now')

  await bot.click(slot, 'shiftLeft')

  assert.equal(bot.count('diamond'), 64)
  assert.equal(await money(), 5000 - 64 * 50)
})

test('buying with too little money is refused and changes nothing', async () => {
  await rcon.command(`eco set ${BOT} 10`)
  await open()
  bot.clearMessages()

  await bot.click(await bot.waitForShopSlot('diamond'), 'left')

  await bot.waitForMessage(/Balance isn't enough/)
  assert.equal(bot.count('diamond'), 64)
  assert.equal(await money(), 10)
})
