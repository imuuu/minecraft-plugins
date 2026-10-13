'use strict'
// ImusEnchants fishing rod enchants at every level on a survival bot, with real catches: Auto Reel,
// Survivalist, Double Catch, Seasoned Angler and Long Cast. The rods also have vanilla Lure III
// so a bite comes soon, and Auto Reel so nobody has to reel in. Double Catch is checked at its chance: a roll
// just below it triggers, the chance itself doesn't (testing.fixed-roll).
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
  // A pool in front of the bot, open to the sky, and room beyond it to cast into
  await rcon.command(`fill ${X + 2} ${Y - 4} ${Z - 3} ${X + 7} ${Y - 1} ${Z + 3} water`)
  await rcon.command(`fill ${X + 8} ${Y - 1} ${Z - 3} ${X + 40} ${Y - 1} ${Z + 3} stone`)
  await rcon.command(`fill ${X + 8} ${Y} ${Z - 3} ${X + 40} ${Y + 15} ${Z + 3} air`)
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

/**
 * Everything the bot holds besides the rod, and the items lying near it.
 */
async function itemCount () {
  // Counted with clear: the inventory's data is too long for RCON
  const found = /(\d+)/.exec(await rcon.command(`minecraft:clear ${BOT} * 0`))
  const held = (found ? Number(found[1]) : 0) - 1
  const answer = await rcon.command(`execute as @e[type=item,x=${X},y=${Y},z=${Z},distance=..12] run data get entity @s Item.count`)
  return held + [...answer.matchAll(/data: (\d+)/g)].reduce((sum, m) => sum + Number(m[1]), 0)
}

/**
 * Casts into the pool and waits for Auto Reel to bring a catch in: how many items and how much XP it gave.
 */
async function catchOne () {
  const items = await itemCount()
  const xp = await f.xpPoints(rcon, BOT)
  // Lure III: a bite usually comes within 15 seconds. A cast that lands off the water is tried again
  for (let attempt = 0; attempt < 3; attempt++) {
    await rcon.command(`minecraft:kill @e[type=fishing_bobber,x=${X},y=${Y},z=${Z},distance=..60]`)
    await sleep(300)
    await cast(new Vec3(X + 5, Y - 1, Z + 0.5))
    const deadline = Date.now() + 40000
    while (await itemCount() === items && Date.now() < deadline)
      await sleep(500)
    if (await itemCount() !== items) {
      // The XP orb and a second item come right after
      await sleep(2000)
      return { items: await itemCount() - items, xp: await f.xpPoints(rcon, BOT) - xp }
    }
  }
  throw new Error('Nothing was caught in three casts: Auto Reel did not reel in')
}

async function fishCount (cooked) {
  let sum = 0
  for (const item of cooked ? ['cooked_cod', 'cooked_salmon'] : ['cod', 'salmon'])
    sum += await f.count(rcon, BOT, item) + await f.itemsOnGround(rcon, item, X, Y, Z, 12)
  return sum
}

test('Auto Reel reels a bite in, and Survivalist cooks the fish', async () => {
  await ready([['auto_reel', 1], ['survivalist', 1]])
  // Junk and treasure can come instead of fish; try a few catches
  for (let attempt = 0; attempt < 5; attempt++) {
    await catchOne()
    assert.equal(await fishCount(false), 0, 'no raw fish')
    if (await fishCount(true) > 0) return
  }
  assert.fail('Five catches and no cod or salmon')
})

test('Double Catch I-III: 15/25/35% chance to catch two', async () => {
  const chances = [0.15, 0.25, 0.35]
  try {
    for (let level = 1; level <= 3; level++) {
      await ready([['auto_reel', 1], ['double_catch', level]])
      await f.roll(rcon, chances[level - 1] - 0.001)
      assert.equal((await catchOne()).items, 2, `level ${level} with a roll under ${chances[level - 1]}`)
      await f.roll(rcon, chances[level - 1])
      assert.equal((await catchOne()).items, 1, `level ${level} with a roll of ${chances[level - 1]}`)
    }
  } finally {
    await f.roll(rcon, 0)
  }
})

test('Seasoned Angler I-III: +25/50/75% XP from fishing', async () => {
  // A catch gives 1-6 XP at random; the enchant makes it round(xp * (1 + bonus)), so every catch's XP is one
  // of these. The ones a plain rod can't give show the bonus works.
  const possible = [[1, 3, 4, 5, 6, 8], [2, 3, 5, 6, 8, 9], [2, 4, 5, 7, 9, 11]]
  for (let level = 1; level <= 3; level++) {
    await ready([['auto_reel', 1], ['seasoned_angler', level]])
    const seen = []
    for (let i = 0; i < 5; i++)
      seen.push((await catchOne()).xp)
    const odd = seen.filter(xp => !possible[level - 1].includes(xp))
    assert.deepEqual(odd, [], `Seasoned Angler ${level}: XP ${seen.join(', ')}`)
  }
})

/**
 * How fast the bobber flies right after the cast, in blocks a tick: the fastest of quick looks, which is its
 * speed before air slows it.
 */
async function castSpeed (enchants) {
  await ready(enchants)
  await bot.bot.lookAt(new Vec3(X + 20, Y + 8, Z + 0.5), true)
  await sleep(200)
  bot.bot.activateItem()
  let fastest = 0
  for (let i = 0; i < 20; i++) {
    const motion = /\[(-?[\d.E-]+)d, (-?[\d.E-]+)d, (-?[\d.E-]+)d\]/.exec(await f.data(rcon, BOBBER, 'Motion'))
    if (motion)
      fastest = Math.max(fastest, Math.hypot(Number(motion[1]), Number(motion[2]), Number(motion[3])))
  }
  bot.bot.deactivateItem()
  assert.ok(fastest > 0, 'No bobber')
  return fastest
}

test('Long Cast I-II casts 30/60% further', async () => {
  const plain = await castSpeed([])
  for (let level = 1; level <= 2; level++) {
    const speed = await castSpeed([['long_cast', level]])
    const expected = 1 + 0.3 * level
    assert.ok(Math.abs(speed / plain - expected) < 0.12, `Long Cast ${level}: ${speed / plain} times as fast, not ${expected}`)
  }
})
