'use strict'
// Wanted requests and the day's mood: the announcement, the bonus when selling, and the info buttons.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer, balance, openShop, TAB_BUTTON } = require('../lib/fixture')

const SHOP = 'BotMarket'
const BOT = 'MarketBot'
const MARKET_BUTTON = 28
const WANTED_BUTTON = 29

let rcon
let bot
let wantedItem
let wantedBonus

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT)
  await bot.command('gs profile apply default 1000 confirm')
  await bot.waitForMessage(/Priced \d+ materials/)
  await bot.command(`gs create shop ${SHOP}`)
  await bot.command(`gs reset limits ${SHOP}`)
  await rcon.command(`gs wanted end ${SHOP}`)
})

after(async () => {
  await rcon?.command(`gs wanted end ${SHOP}`)
  bot?.closeWindow()
  bot?.quit()
  rcon?.close()
})

/**
 * Sells one of the wanted item and returns what it paid, and the lore it showed.
 */
async function sellOne () {
  await rcon.command(`clear ${BOT}`)
  await rcon.command(`minecraft:give ${BOT} ${wantedItem} 1`)
  await sleep(300)
  await openShop(rcon, bot, SHOP)
  let slots = bot.customerSlotsWith(wantedItem)
  if (slots.length === 0) {
    await bot.click(TAB_BUTTON)
    slots = bot.customerSlotsWith(wantedItem)
  }
  assert.ok(slots.length > 0, `${wantedItem} shows on the sell side`)
  const lore = bot.lore(slots[0]).join('\n')
  const before = await balance(rcon, BOT)
  await bot.click(slots[0])
  return { paid: await balance(rcon, BOT) - before, lore }
}

test('without the mood the shop has no market button', async () => {
  await openShop(rcon, bot, SHOP)
  assert.equal(bot.slot(MARKET_BUTTON).name, 'red_stained_glass_pane')
  assert.equal(bot.slot(WANTED_BUTTON).name, 'red_stained_glass_pane', 'nothing is wanted yet')
})

test('with the mood the shop shows the market of the day, and prices follow it', async () => {
  const text = (await rcon.command('ia config imusgs set live.mood.enabled true')).replace(/§./g, '')
  assert.match(text, /Market mood changed/)
  await openShop(rcon, bot, SHOP)
  assert.equal(bot.slot(MARKET_BUTTON).name, 'filled_map')
  const lore = bot.lore(MARKET_BUTTON)
  assert.ok(lore.some(line => /^ores /.test(line)), lore.join('\n'))

  // Diamond is in "minerals"; the shop pays 1000 x the minerals mood for it
  const minerals = lore.find(line => /^minerals /.test(line))
  const percent = /([+-]\d+)%/.exec(minerals)
  const factor = 1 + (percent ? Number(percent[1]) : 0) / 100
  await rcon.command(`clear ${BOT}`)
  await rcon.command(`give ${BOT} diamond 1`)
  await sleep(300)
  await openShop(rcon, bot, SHOP)
  const before = await balance(rcon, BOT)
  await bot.click(bot.customerSlotsWith('diamond')[0])
  assert.ok(Math.abs(await balance(rcon, BOT) - before - 1000 * factor) < 0.02, `diamond with ${minerals}`)

  await rcon.command('ia config imusgs set live.mood.enabled false')
})

test('a wanted request is told to everyone with its items', async () => {
  bot.closeWindow()
  bot.clearMessages()
  const answer = (await rcon.command(`gs wanted start ${SHOP}`)).replace(/§./g, '')
  const match = /now wants: (.+?) \+(\d+)%/.exec(answer)
  assert.ok(match, answer)
  wantedItem = match[1].toLowerCase().replace(/ /g, '_')
  wantedBonus = Number(match[2]) / 100
  await bot.waitForMessage(new RegExp(`\\[Wanted\\] ${SHOP} wants .*${match[1]} \\(\\+${match[2]}%\\).* for the next \\d+ h!`))
})

test('the wanted button lists what the shop wants', async () => {
  await openShop(rcon, bot, SHOP)
  assert.equal(bot.slot(WANTED_BUTTON).name, wantedItem)
  assert.match(bot.lore(WANTED_BUTTON).join('\n'), new RegExp(`\\+${Math.round(wantedBonus * 100)}%`))
})

test('a wanted item sells with the bonus', async () => {
  const wanted = await sellOne()
  assert.match(wanted.lore, /WANTED \+\d+%/)

  await rcon.command(`gs wanted end ${SHOP}`)
  const normal = await sellOne()
  assert.doesNotMatch(normal.lore, /WANTED/)

  assert.ok(normal.paid > 0, `${wantedItem} sells for something`)
  const expected = normal.paid * (1 + wantedBonus)
  assert.ok(Math.abs(wanted.paid - expected) <= 0.02 + expected * 0.01,
    `${wantedItem}: ${wanted.paid} with the bonus, ${normal.paid} without, ${wantedBonus * 100}% bonus`)
})
