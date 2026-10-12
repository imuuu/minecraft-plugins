'use strict'
// Custom enchants and items of other plugins through ImusAPI: ImusEnchants' enchants and boosters and imusTNT's
// TNTs are priced by their tier, sold by the village shops, tracked by the server's progress, and an admin can
// set their prices.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer, balance, openShop } = require('../lib/fixture')

const REDSTONE = 'BotRedstone'
const ENCHANTER = 'BotEnchanter'
const BOT = 'CustomBot'

let rcon
let bot

const plain = text => text.replace(/§./g, '')

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT, 10_000_000)
  await bot.command('gs profile apply default 1000 confirm')
  await bot.waitForMessage(/Priced \d+ materials/)
  await rcon.command(`gs remove shop ${REDSTONE} confirm`)
  await rcon.command(`gs remove shop ${ENCHANTER} confirm`)
  await rcon.command('gs set custom price imustnt:chunk_stone_tnt default')
  await bot.command(`gs create preset redstone ${REDSTONE}`)
  await bot.command(`gs create preset enchanter ${ENCHANTER}`)
})

after(async () => {
  await rcon?.command('gs set custom price imustnt:chunk_stone_tnt default')
  await rcon?.command(`gs remove shop ${REDSTONE} confirm`)
  await rcon?.command(`gs remove shop ${ENCHANTER} confirm`)
  await rcon?.command(`op ${BOT}`)
  bot?.closeWindow()
  bot?.quit()
  rcon?.close()
})

/**
 * Shop slots whose item has this lore text or name.
 */
function shopSlotsWhere (test) {
  const slots = []
  for (let slot = 0; slot < 27; slot++) {
    const item = bot.slot(slot)
    if (item && !item.name.endsWith('glass_pane') && test(item, bot.lore(slot).join('\n')))
      slots.push(slot)
  }
  return slots
}

/**
 * What /g getprice says the item in the first hotbar slot is worth.
 */
async function priceInHand () {
  bot.bot.setQuickBarSlot(0)
  await sleep(200)
  bot.clearMessages()
  await bot.command('g getprice hand')
  const line = await bot.waitForMessage(/price is:/)
  return Number(/price is:\s*([\d.]+)/.exec(line)[1])
}

test('the other plugins custom enchants and items are known', async () => {
  const text = plain(await rcon.command('gs customprices'))
  assert.match(text, /imusenchants:tunnel/)
  assert.match(text, /imusenchants:booster_1/)
  assert.match(text, /imustnt:chunk_tnt/)
})

test('the redstone shop sells special TNT at its tier price', async () => {
  await rcon.command(`clear ${BOT}`)
  await openShop(rcon, bot, REDSTONE)
  const tnt = shopSlotsWhere((item, lore) => item.name === 'tnt' && /Chunk|chunk/.test(JSON.stringify(item.components ?? [])))
  assert.ok(tnt.length > 0, 'a Chunk Stone TNT listing')
  await bot.click(tnt[0])
  bot.closeWindow()
  await sleep(300)
  // Tier 2: 1.5 diamonds
  assert.equal(await priceInHand(), 1500)
})

test('an admin can set a custom item price', async () => {
  const text = plain(await rcon.command('gs set custom price imustnt:chunk_stone_tnt 777'))
  assert.match(text, /now costs 777/)
  assert.equal(await priceInHand(), 777)
})

test('the enchanter sells boosters and custom enchant books', async () => {
  await openShop(rcon, bot, ENCHANTER)
  const boosters = shopSlotsWhere(item => item.name === 'netherite_upgrade_smithing_template')
  assert.equal(boosters.length, 3, 'three tiers of boosters')
  // A rotation brings 2-4 books and each is a custom enchant's half the time, so about one rotation in seven
  // has none; another rotation brings more
  const customBooks = () => shopSlotsWhere((item, lore) => item.name === 'enchanted_book' && /Custom Enchant/.test(lore))
  let custom = customBooks()
  for (let i = 0; i < 5 && custom.length === 0; i++) {
    assert.match(plain(await rcon.command(`gs restock ${ENCHANTER}`)), /kinds of items came/)
    await openShop(rcon, bot, ENCHANTER)
    custom = customBooks()
  }
  assert.ok(custom.length > 0, 'a custom enchant book from the rotations')

  await rcon.command(`clear ${BOT}`)
  await sleep(200)
  const before = await balance(rcon, BOT)
  await bot.click(custom[0])
  assert.ok(await balance(rcon, BOT) < before, 'the book cost money')
})

test('the server progress sees custom enchants', async () => {
  bot.closeWindow()
  await rcon.command('gs progress reset')
  await rcon.command(`deop ${BOT}`)
  await sleep(300)
  const text = plain(await rcon.command('gs progress scan'))
  assert.match(text, /imusenchants:\w+ 1/)
})
