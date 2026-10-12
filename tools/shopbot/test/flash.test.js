'use strict'
// Flash deals: the announcement, a peek from anywhere, the blinking button in the shop, buying with a per player
// limit, and that bought items can't be sold back. Also /gs restock.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer, balance, openShop, TAB_BUTTON } = require('../lib/fixture')

const SHOP = 'BotFlash'
const BOT = 'FlashBot'
const FLASH_BUTTON = 34
const FLASH_FRAMES = ['nether_star', 'yellow_stained_glass_pane', 'gold_block', 'orange_stained_glass_pane']

let rcon
let bot
let offerSlot
let offerName
let offerPrice

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT, 10_000_000)
  await bot.command('gs profile apply default 1000 confirm')
  await bot.waitForMessage(/Priced \d+ materials/)
  await bot.command(`gs create shop ${SHOP}`)
  await rcon.command(`gs flash end ${SHOP}`)
})

after(async () => {
  await rcon?.command(`gs flash end ${SHOP}`)
  bot?.closeWindow()
  bot?.quit()
  rcon?.close()
})

/**
 * Menu slots 9-17 that hold offers (not the black filler).
 */
function offerSlots () {
  const slots = []
  for (let slot = 9; slot <= 17; slot++) {
    const item = bot.slot(slot)
    if (item && item.name !== 'black_stained_glass_pane') slots.push(slot)
  }
  return slots
}

async function openDeals () {
  await openShop(rcon, bot, SHOP)
  return await bot.clickForNewWindow(FLASH_BUTTON)
}

test('without a deal the shop has no flash button', async () => {
  await openShop(rcon, bot, SHOP)
  assert.equal(bot.slot(FLASH_BUTTON).name, 'red_stained_glass_pane')
})

test('starting a deal tells everyone, without naming the items', async () => {
  bot.closeWindow()
  bot.clearMessages()
  await rcon.command(`gs flash start ${SHOP}`)
  const message = await bot.waitForMessage(/Flash deals are on/)
  assert.match(message, new RegExp(`${SHOP}! \\d+ min left\\. \\[Peek\\]`))
})

test('a peek shows a couple of offers and hides the rest', async () => {
  await bot.command(`gs peek ${SHOP}`)
  await bot.waitForWindow()
  assert.match(bot.windowTitle(), /Peek/)

  const slots = offerSlots()
  const hidden = slots.filter(slot => bot.slot(slot).name === 'gray_dye')
  assert.ok(slots.length >= 4, `at least 4 offers, got ${slots.length}`)
  assert.equal(slots.length - hidden.length, 2, 'two offers are shown')

  // Nothing can be bought in a peek
  const before = await balance(rcon, BOT)
  await bot.click(slots.find(slot => bot.slot(slot).name !== 'gray_dye'))
  assert.equal(await balance(rcon, BOT), before)
})

test('the blinking button in the shop opens the deals', async () => {
  await openShop(rcon, bot, SHOP)
  const seen = new Set()
  for (let i = 0; i < 6; i++) {
    seen.add(bot.slot(FLASH_BUTTON).name)
    await sleep(250)
  }
  assert.ok([...seen].every(name => FLASH_FRAMES.includes(name)), 'flash frames: ' + [...seen])
  assert.ok(seen.size > 1, 'the button changes')

  await bot.clickForNewWindow(FLASH_BUTTON)
  assert.match(bot.windowTitle(), /Flash Deals/)
  assert.doesNotMatch(bot.windowTitle(), /Peek/)
})

test('left click buys one at the flash price', async () => {
  await openDeals()
  offerSlot = offerSlots().find(slot => bot.lore(slot).some(line => /You can buy: [1-9]/.test(line)))
  offerName = bot.slot(offerSlot).name
  offerPrice = Number(/Flash price: ([\d,.]+)/.exec(bot.lore(offerSlot).join('\n'))[1].replace(/,/g, ''))
  const before = await balance(rcon, BOT)

  await bot.click(offerSlot)

  assert.ok(Math.abs(await balance(rcon, BOT) - (before - offerPrice)) < 0.02, `paid ${offerPrice}`)
  const own = bot.ownSlotsWith(offerName)
  assert.ok(own.length > 0, 'the bot got the item')
  assert.match(bot.lore(own[0]).join('\n'), /Flash deal/)
})

test('a flash item can not be sold back', async () => {
  await openShop(rcon, bot, SHOP)
  let slots = bot.customerSlotsWith(offerName)
  if (slots.length === 0) {
    await bot.click(TAB_BUTTON)
    slots = bot.customerSlotsWith(offerName)
  }
  assert.ok(slots.length > 0, 'the item shows on the sell side')
  assert.match(bot.lore(slots[0]).join('\n'), /doesn't buy/)

  const count = bot.count(offerName)
  const before = await balance(rcon, BOT)
  await bot.click(slots[0], 'shiftRight')
  assert.equal(bot.count(offerName), count)
  assert.equal(await balance(rcon, BOT), before)
})

test('a player can buy only up to their limit', async () => {
  await openDeals()
  await bot.click(offerSlot, 'shiftRight')
  assert.match(bot.lore(offerSlot).join('\n'), /You can buy: 0/)

  const before = await balance(rcon, BOT)
  await bot.click(offerSlot)
  assert.equal(await balance(rcon, BOT), before)
})

test('ending the deal takes the button away', async () => {
  bot.closeWindow()
  await rcon.command(`gs flash end ${SHOP}`)
  await openShop(rcon, bot, SHOP)
  assert.equal(bot.slot(FLASH_BUTTON).name, 'red_stained_glass_pane')
})

test('/gs restock brings rotating stock', async () => {
  const text = await rcon.command(`gs restock ${SHOP}`)
  assert.match(text, /\d+ kinds of items came/)
  await openShop(rcon, bot, SHOP)
  const listed = []
  for (let slot = 0; slot < 27; slot++) {
    const item = bot.slot(slot)
    if (item && !item.name.endsWith('glass_pane')) listed.push(item.name)
  }
  assert.ok(listed.length >= 1, 'the shop has items: ' + listed)
})
