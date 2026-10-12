'use strict'
// Flash deals follow the server's progress: they offer only items and enchants that players who aren't ops
// have had, at most at the level they have had.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer, openShop } = require('../lib/fixture')

const SHOP = 'BotProgress'
const BOT = 'ProgressBot'
const FLASH_BUTTON = 34

let rcon
let bot

const plain = text => text.replace(/§./g, '')

/**
 * The item's enchants from its enchantments component: [{ name: 'sharpness', lvl: 3 }].
 */
function enchantsOf (item) {
  const component = (item.components ?? []).find(c => c.type === 'enchantments')
  if (!component) return []
  return component.data.enchantments.map(e => ({ name: bot.bot.registry.enchantments[e.id]?.name, lvl: e.level }))
}

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT)
  await bot.command('gs profile apply default 1000 confirm')
  await bot.waitForMessage(/Priced \d+ materials/)
  await bot.command(`gs create shop ${SHOP}`)
  await rcon.command(`gs flash end ${SHOP}`)
  await rcon.command('ia config imusgs set live.progress.flash-deals true')
  await rcon.command('gs progress reset')
})

after(async () => {
  await rcon?.command(`gs flash end ${SHOP}`)
  await rcon?.command('ia config imusgs set live.progress.flash-deals false')
  await rcon?.command('gs progress reset')
  await rcon?.command(`op ${BOT}`)
  bot?.closeWindow()
  bot?.quit()
  rcon?.close()
})

test('with nothing seen there is no flash deal', async () => {
  const text = plain(await rcon.command(`gs flash start ${SHOP}`))
  assert.match(text, /Nothing to offer/)
})

test('ops are not counted', async () => {
  await rcon.command(`minecraft:give ${BOT} netherite_sword 1`)
  await sleep(300)
  const text = plain(await rcon.command('gs progress scan'))
  assert.match(text, /had 0 kinds of items/)
})

test('what a player has is counted, with the enchant levels', async () => {
  await rcon.command(`clear ${BOT}`)
  await rcon.command(`deop ${BOT}`)
  await rcon.command(`minecraft:give ${BOT} diamond_sword[enchantments={sharpness:3}] 1`)
  await rcon.command(`minecraft:give ${BOT} oak_log 64`)
  await sleep(300)
  const text = plain(await rcon.command('gs progress scan'))
  assert.match(text, /had 2 kinds of items/)
  assert.match(text, /sharpness 3/)
})

test('a flash deal offers only what was seen', async () => {
  const text = plain(await rcon.command(`gs flash start ${SHOP}`))
  assert.match(text, /Started a flash deal/)

  await openShop(rcon, bot, SHOP)
  await bot.clickForNewWindow(FLASH_BUTTON)
  const offers = []
  for (let slot = 9; slot <= 17; slot++) {
    const item = bot.slot(slot)
    if (item && item.name !== 'black_stained_glass_pane') offers.push(item)
  }
  assert.ok(offers.length > 0)
  for (const item of offers) {
    assert.ok(['diamond_sword', 'oak_log'].includes(item.name), 'not seen: ' + item.name)
    for (const enchant of enchantsOf(item)) {
      assert.equal(enchant.name, 'sharpness', 'only a seen enchant')
      assert.ok(enchant.lvl <= 3, `sharpness ${enchant.lvl} is over the seen 3`)
    }
  }
})
