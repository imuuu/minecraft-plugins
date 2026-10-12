'use strict'
// Village shops from presets: a blacksmith with its NPC, buy only, its lasting stock and enchanted gear,
// its own flash deals; /gs village; removing a shop with its NPC.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer, openShop } = require('../lib/fixture')

const SHOP = 'BotSmith'
const BOT = 'VillageBot'

let rcon
let bot

const plain = text => text.replace(/§./g, '')

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT)
  await bot.command('gs profile apply default 1000 confirm')
  await bot.waitForMessage(/Priced \d+ materials/)
  await rcon.command(`gs remove shop ${SHOP} confirm`)
})

after(async () => {
  await rcon?.command(`gs flash end ${SHOP}`)
  bot?.closeWindow()
  bot?.quit()
  rcon?.close()
})

/**
 * Villagers within a few blocks of the bot.
 */
function villagersNear () {
  return Object.values(bot.bot.entities).filter(e => e.name === 'villager' && e.position.distanceTo(bot.bot.entity.position) < 4)
}

/**
 * Items of the shop's first page: [{ name, enchanted }].
 */
function shopItems () {
  const items = []
  for (let slot = 0; slot < 27; slot++) {
    const item = bot.slot(slot)
    if (item && !item.name.endsWith('glass_pane'))
      items.push({ name: item.name, enchanted: (item.components ?? []).some(c => c.type === 'enchantments') })
  }
  return items
}

test('a blacksmith shop is made with its NPC where the admin stands', async () => {
  const before = villagersNear().length
  bot.clearMessages()
  await bot.command(`gs create preset blacksmith ${SHOP}`)
  await bot.waitForMessage(/Made .*and its NPC/)
  await sleep(500)
  assert.equal(villagersNear().length, before + 1, 'a villager stands here now')
})

test('the blacksmith only sells, its lasting stock and enchanted gear', async () => {
  await openShop(rcon, bot, SHOP)
  assert.equal(bot.slot(36).name, 'blue_stained_glass_pane', 'players can not sell here')
  const names = shopItems().map(i => i.name)
  for (const item of ['iron_ingot', 'coal', 'iron_pickaxe', 'shield', 'anvil'])
    assert.ok(names.includes(item), `${item} in ${names}`)
  assert.ok(shopItems().some(i => i.enchanted), 'the first rotation brought enchanted gear')
})

test('the blacksmith flash deal is mostly enchanted gear', async () => {
  bot.closeWindow()
  const answer = plain(await rcon.command(`gs flash start ${SHOP}`))
  assert.match(answer, /Started a flash deal/)
  await openShop(rcon, bot, SHOP)
  await bot.clickForNewWindow(34)
  let gear = 0
  let offers = 0
  for (let slot = 9; slot <= 17; slot++) {
    const item = bot.slot(slot)
    if (!item || item.name === 'black_stained_glass_pane') continue
    offers++
    if ((item.components ?? []).some(c => c.type === 'enchantments')) gear++
  }
  assert.ok(offers > 0)
  assert.ok(gear >= offers / 2, `${gear} of ${offers} offers are enchanted gear`)
})

test('/gs village makes the presets that have no shop yet', async () => {
  bot.closeWindow()
  bot.clearMessages()
  await bot.command('gs village')
  await bot.waitForMessage(/The village has \d+ new shops|Every preset already has its shop/)
  bot.clearMessages()
  await bot.command('gs presets')
  for (const id of ['general', 'builder', 'farmer', 'blacksmith', 'redstone', 'fisherman', 'alchemist', 'librarian', 'decorator'])
    await bot.waitForMessage(new RegExp(`^  ${id} .* - made$`))
})

test('removing the shop removes its NPC too', async () => {
  const before = villagersNear().length
  const answer = plain(await rcon.command(`gs remove shop ${SHOP} confirm`))
  assert.match(answer, /Removed BotSmith and 1 NPC/)
  await sleep(500)
  assert.equal(villagersNear().length, before - 1)
})
