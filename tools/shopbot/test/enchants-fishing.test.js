'use strict'
// ImusEnchants fishing rod enchants on a survival bot: Auto Reel, Survivalist and Double Catch on a real
// catch, and Long Cast. Waiting for a bite takes a while, so the rod also has vanilla Lure III.
// Seasoned Angler isn't tested: a catch gives 1-6 XP at random, too spread out to see +75% in a few catches.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Vec3 } = require('vec3')
const { Rcon } = require('../lib/rcon')
const { sleep } = require('../lib/shopbot')
const f = require('../lib/enchant-fixture')

const BOT = 'FishBot'
const X = 440
const Z = 340
const Y = f.Y
const BOBBER = `@e[type=fishing_bobber,x=${X},y=${Y},z=${Z},distance=..60,limit=1]`

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await f.setUp(rcon, BOT, X, Z)
  // A pool in front of the bot, open to the sky
  await rcon.command(`fill ${X + 2} ${Y - 4} ${Z - 3} ${X + 7} ${Y - 1} ${Z + 3} water`)
})

after(async () => {
  await f.tearDown(rcon, bot)
  rcon?.close()
})

async function ready (enchants) {
  await rcon.command(`minecraft:kill @e[type=fishing_bobber,x=${X},y=${Y},z=${Z},distance=..60]`)
  await rcon.command(`minecraft:kill @e[type=item,x=${X},y=${Y},z=${Z},distance=..20]`)
  await rcon.command(`minecraft:clear ${BOT}`)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await f.hold(rcon, bot, 'fishing_rod[enchantments={lure:3}]', enchants)
  await sleep(500)
}

async function cast (at) {
  await bot.bot.lookAt(at, true)
  await sleep(200)
  bot.bot.activateItem()
  await sleep(300)
  bot.bot.deactivateItem()
}

const ITEMS = ['cod', 'salmon', 'cooked_cod', 'cooked_salmon', 'pufferfish', 'tropical_fish']

/**
 * What the bot has and what lies near it of the possible catches, plus everything else it holds.
 */
async function catches () {
  const result = {}
  for (const item of ITEMS)
    result[item] = await f.count(rcon, BOT, item) + await f.itemsOnGround(rcon, item, X, Y, Z, 12)
  const inventory = await f.data(rcon, BOT, 'Inventory')
  result.total = [...inventory.matchAll(/count: (\d+)/g)].reduce((sum, m) => sum + Number(m[1]), 0) - 1
  return result
}

test('Auto Reel reels a bite in, Survivalist cooks the fish and Double Catch doubles it', async () => {
  await ready([['auto_reel', 1], ['survivalist', 1], ['double_catch', 3]])
  // Junk and treasure can come instead of fish; try a few catches
  for (let attempt = 0; attempt < 4; attempt++) {
    const before = await catches()
    await cast(new Vec3(X + 5, Y - 1, Z + 0.5))
    // Lure III: a bite comes within about 15 seconds, and nobody reels in but the enchant
    let after = before
    const deadline = Date.now() + 40000
    while (JSON.stringify(after) === JSON.stringify(before)) {
      if (Date.now() > deadline) throw new Error('Nothing was caught: Auto Reel did not reel in')
      await sleep(1000)
      after = await catches()
    }
    await sleep(1500)
    after = await catches()
    assert.equal(after.cod + after.salmon, 0, 'no raw fish: Survivalist cooks them')
    const cooked = after.cooked_cod + after.cooked_salmon - before.cooked_cod - before.cooked_salmon
    if (cooked > 0) {
      assert.equal(cooked, 2, 'Double Catch gives two')
      return
    }
  }
  assert.fail('Four catches and no cod or salmon')
})

/**
 * How far the bobber has flown 0.6 seconds after the cast.
 */
async function castDistance (enchants) {
  await ready(enchants)
  await rcon.command(`fill ${X + 8} ${Y - 1} ${Z - 3} ${X + 40} ${Y - 1} ${Z + 3} stone`)
  await rcon.command(`fill ${X + 8} ${Y} ${Z - 3} ${X + 40} ${Y + 15} ${Z + 3} air`)
  await cast(new Vec3(X + 20, Y + 8, Z + 0.5))
  await sleep(300)
  const answer = await rcon.command(`data get entity ${BOBBER} Pos`)
  const match = /\[(-?[\d.]+)d/.exec(answer)
  assert.ok(match, 'No bobber: ' + answer)
  return Number(match[1]) - (X + 0.5)
}

test('Long Cast casts further', async () => {
  const plain = await castDistance([])
  const long = await castDistance([['long_cast', 2]])
  assert.ok(long >= plain * 1.25, `${long} blocks with Long Cast II, ${plain} without`)
})
