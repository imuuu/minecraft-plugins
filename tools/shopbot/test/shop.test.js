'use strict'
// Plays an imusGS shop like a player on the local test server (run/), which must be running.
// node --test test/
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')

const SHOP = 'BotTest'
const BOT = 'ShopBot'

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await rcon.command(`op ${BOT}`)
  await rcon.command(`gamemode survival ${BOT}`)
  await rcon.command(`clear ${BOT}`)
  await rcon.command(`eco set ${BOT} 1000`)
  await rcon.command(`give ${BOT} diamond 64`)
  await sleep(500)
  // Diamonds are worth 10; a new shop sells for x5 and buys for x1
  bot.bot.setQuickBarSlot(0)
  await bot.command('gs set material price hand 10')
  await bot.command(`gs create shop ${SHOP}`)
})

after(async () => {
  bot?.closeWindow()
  bot?.quit()
  rcon?.close()
})

async function balance () {
  // Essentials: "Pelaajan ShopBot saldo: $1,000.50"
  const text = await rcon.command(`money ${BOT}`)
  const match = /\$\s*(-?\d[\d,]*(?:\.\d+)?)/.exec(text)
  assert.ok(match, 'Balance not found in: ' + text)
  return Number(match[1].replace(/,/g, ''))
}

const HOTBAR_TOGGLE = 33

/**
 * Opens the shop for the bot. /give puts the diamonds in the hotbar, which the shop leaves out until the
 * hotbar button is clicked.
 */
async function openShop ({ includeHotbar = true } = {}) {
  bot.closeWindow()
  await sleep(300)
  await rcon.command(`gsopenfor ${BOT} ${SHOP}`)
  const window = await bot.waitForWindow()
  if (includeHotbar)
    await bot.click(HOTBAR_TOGGLE)
  return window
}

test('the hotbar is left out until its button is clicked', async () => {
  await openShop({ includeHotbar: false })
  assert.equal(bot.customerSlotsWith('diamond').length, 0)
  await bot.click(HOTBAR_TOGGLE)
  assert.equal(bot.customerSlotsWith('diamond').length, 1)
})

test('the shop opens with its name as the title', async () => {
  await openShop()
  assert.match(bot.windowTitle(), new RegExp(SHOP))
})

test('selling one diamond pays 10 and puts it in the shop', async () => {
  const before = await balance()
  await openShop()
  const [slot] = bot.customerSlotsWith('diamond')
  assert.ok(slot !== undefined, 'The diamonds should be on the customer side')

  await bot.click(slot, 'left')

  assert.equal(bot.count('diamond'), 63)
  assert.equal(await balance(), before + 10)
  assert.equal(bot.shopSlotsWith('diamond').length, 1, 'The shop should list the diamond')
})

test('buying one diamond back costs 50', async () => {
  const before = await balance()
  await openShop()
  const [slot] = bot.shopSlotsWith('diamond')
  assert.ok(slot !== undefined, 'The shop should have a diamond')

  await bot.click(slot, 'left')

  assert.equal(bot.count('diamond'), 64)
  assert.equal(await balance(), before - 50)
})

test('selling 8 with a right click', async () => {
  const before = await balance()
  await openShop()
  const [slot] = bot.customerSlotsWith('diamond')

  await bot.click(slot, 'right')

  assert.equal(bot.count('diamond'), 56)
  assert.equal(await balance(), before + 80)
})

test('buying with too little money is refused and changes nothing', async () => {
  await rcon.command(`eco set ${BOT} 10`)
  await openShop()
  const [slot] = bot.shopSlotsWith('diamond')
  bot.clearMessages()

  await bot.click(slot, 'left')

  await bot.waitForMessage(/Balance isn't enough/)
  assert.equal(bot.count('diamond'), 56)
  assert.equal(await balance(), 10)
})
