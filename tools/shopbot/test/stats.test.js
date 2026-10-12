'use strict'
// /gs stats and the language of the texts players see.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer, setHandPrice, openShop } = require('../lib/fixture')

const SHOP = 'BotStats'
const BOT = 'StatsBot'

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT)
  await bot.command(`gs create shop ${SHOP}`)
  await rcon.command(`give ${BOT} diamond 10`)
  await sleep(300)
  await setHandPrice(bot, 10)
})

after(async () => {
  await rcon?.command('ia config imusgs set language en')
  bot?.closeWindow()
  bot?.quit()
  rcon?.close()
})

/**
 * What /gs stats says the shop paid players today: [money, sells, items].
 */
async function paidToday () {
  bot.clearMessages()
  await bot.command(`gs stats 1 ${SHOP}`)
  await bot.waitForMessage(/imusGS stats - last 1 day - BotStats/)
  const line = await bot.waitForMessage(/Shops paid players/)
  const match = /Shops paid players ([\d,]+) \$ \((\d+) sells, (\d+) items\)/.exec(line)
  assert.ok(match, line)
  return match.slice(1).map(n => Number(n.replace(/,/g, '')))
}

test('stats show what the shop paid after a sale', async () => {
  const before = await paidToday()
  await openShop(rcon, bot, SHOP)
  await bot.click(bot.customerSlotsWith('diamond')[0], 'right')
  bot.closeWindow()

  const after = await paidToday()
  assert.deepEqual(after, [before[0] + 80, before[1] + 1, before[2] + 8])
  await bot.waitForMessage(/Net: .*(taken out of|put into) the economy/)
  await bot.waitForMessage(/Sold to shops most: Diamond/)
})

test('stats of every shop show the live events', async () => {
  bot.clearMessages()
  await bot.command('gs stats')
  await bot.waitForMessage(/last 7 days - every shop/)
  await bot.waitForMessage(/Next: /)
})

test('the texts follow the language setting', async () => {
  const answer = (await rcon.command('ia config imusgs set language fi')).replace(/§./g, '')
  assert.match(answer, /Language changed/)
  await openShop(rcon, bot, SHOP)
  const lore = bot.lore(bot.customerSlotsWith('diamond')[0]).join('\n')
  assert.match(lore, /Määrä: 2/)
  assert.match(lore, /MYY/)

  await rcon.command('ia config imusgs set language en')
  await openShop(rcon, bot, SHOP)
  assert.match(bot.lore(bot.customerSlotsWith('diamond')[0]).join('\n'), /Amount: 2/)
})
