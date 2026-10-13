'use strict'
// ImusEnchants bow enchants at every level, on a survival bot. Hunter, Poison Arrow and Retrieval are checked
// with arrows summoned as the bot's, with a set speed and no critical hit, so the damage is exact; Sniper and
// Ender Arrow with real shots. Chance enchants are checked at their chance: a roll just below it triggers, the
// chance itself doesn't (testing.fixed-roll).
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Vec3 } = require('vec3')
const { Rcon } = require('../lib/rcon')
const { sleep } = require('../lib/shopbot')
const f = require('../lib/enchant-fixture')

const BOT = 'BowBot'
const X = 440
const Z = 300
const Y = f.Y
const COW = '@e[tag=ie,type=cow,limit=1,sort=nearest]'
const ARROWS = `@e[type=arrow,x=${X},y=${Y},z=${Z},distance=..80]`

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await f.setUp(rcon, BOT, X, Z)
  // A lane to shoot down
  await rcon.command(`fill ${X} ${Y - 1} ${Z - 2} ${X + 60} ${Y - 1} ${Z + 2} stone`)
  await rcon.command(`fill ${X} ${Y} ${Z - 2} ${X + 60} ${Y + 12} ${Z + 2} air`)
})

after(async () => {
  bot?.bot.clearControlStates()
  await rcon?.command('minecraft:kill @e[tag=ie]')
  await f.tearDown(rcon, bot)
  rcon?.close()
})

/**
 * The bot in its spot facing down the lane, with the bow and arrows.
 */
async function ready (enchants) {
  await rcon.command('minecraft:kill @e[tag=ie]')
  await rcon.command(`minecraft:kill ${ARROWS}`)
  await rcon.command(`minecraft:clear ${BOT}`)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await f.refresh(rcon, BOT)
  await f.hold(rcon, bot, 'bow', enchants)
  await rcon.command(`minecraft:item replace entity ${BOT} hotbar.1 with arrow 16`)
  await sleep(500)
}

/**
 * Draws the bow for ms and lets go, looking at the point.
 */
async function shoot (ms, at) {
  await bot.bot.lookAt(at, true)
  await sleep(200)
  bot.bot.activateItem()
  await sleep(ms)
  bot.bot.deactivateItem()
}

/**
 * An arrow of the bot's with the bow enchants on it, flying east from x at 2 blocks a tick. Its first hit
 * does 4 damage without enchants.
 */
async function arrow (x, y, enchants) {
  const data = enchants.map(([key, level]) => `${key}:${level}`).join(',')
  await rcon.command(`summon arrow ${x} ${y} ${Z + 0.5} {Motion:[2.0d,0.0d,0.0d],damage:2.0d,crit:0b,pickup:1b,` +
    `Owner:${f.uuidInts(BOT)},BukkitValues:{"imusenchants:ie_projectile_enchants":"${data}"}}`)
  await sleep(800)
}

/**
 * A still cow with 100 health six blocks down the lane, hit by an arrow with the enchants.
 */
async function shootCow (enchants) {
  await ready([])
  await f.summon(rcon, 'cow', X + 6.5, Y, Z + 0.5)
  await f.setMobHealth(rcon, COW, 100, 100)
  await sleep(300)
  await arrow(X + 4.5, Y + 0.7, enchants)
  return 100 - await f.health(rcon, COW)
}

/**
 * For each level: a roll just below its chance triggers the enchant, a roll at its chance doesn't.
 */
async function checkChances (chances, triggered) {
  try {
    for (let level = 1; level <= chances.length; level++) {
      const chance = chances[level - 1]
      await f.roll(rcon, chance - 0.001)
      assert.equal(await triggered(level), true, `level ${level} with a roll under ${chance}`)
      await f.roll(rcon, chance)
      assert.equal(await triggered(level), false, `level ${level} with a roll of ${chance}`)
    }
  } finally {
    await f.roll(rcon, 0)
  }
}

test('a real shot with a Poison Arrow bow poisons what it hits', async () => {
  await ready([['poison_arrow', 3]])
  await f.summon(rcon, 'cow', X + 6.5, Y, Z + 0.5)
  await f.setMobHealth(rcon, COW, 100, 100)
  await shoot(1200, new Vec3(X + 6.5, Y + 0.9, Z + 0.5))
  await sleep(1500)
  assert.ok((await f.effects(rcon, COW)).includes('minecraft:poison'))
})

test('Hunter I-III: arrows do +15/30/45% to animals', async () => {
  assert.equal(await shootCow([]), 4, 'a plain arrow does 4')
  for (let level = 1; level <= 3; level++) {
    const damage = await shootCow([['hunter', level]])
    const expected = 4 * (1 + 0.15 * level)
    assert.ok(Math.abs(damage - expected) < 0.01, `Hunter ${level}: ${damage}, not ${expected}`)
  }
})

test('Poison Arrow I-III: 30/45/60% poison for 4/5/6 s, stronger at III', async () => {
  await checkChances([0.30, 0.45, 0.60], async level => {
    await shootCow([['poison_arrow', level]])
    const got = await f.effect(rcon, COW, 'poison')
    if (got) {
      const ticks = 60 + 20 * level
      assert.equal(got.amplifier, level >= 3 ? 1 : 0, `level ${level} strength`)
      assert.ok(got.duration <= ticks && got.duration > ticks - 30, `level ${level}: ${got.duration} ticks, not ${ticks}`)
    }
    return got !== null
  })
})

test('Retrieval I-III: 20/35/50% chance to get the arrow back', async () => {
  await checkChances([0.20, 0.35, 0.50], async level => {
    await ready([])
    await rcon.command(`fill ${X + 6} ${Y} ${Z - 1} ${X + 6} ${Y + 2} ${Z + 1} stone`)
    const arrows = await f.count(rcon, BOT, 'arrow')
    await arrow(X + 4.5, Y + 1.5, [['retrieval', level]])
    await rcon.command(`fill ${X + 6} ${Y} ${Z - 1} ${X + 6} ${Y + 2} ${Z + 1} air`)
    const now = await f.count(rcon, BOT, 'arrow')
    assert.ok(now === arrows || now === arrows + 1, `${now - arrows} arrows back`)
    return now === arrows + 1
  })
})

/**
 * Shoots a full arrow up the lane and returns its damage value and speed right after.
 */
async function fullShot (enchants) {
  await ready(enchants)
  await shoot(1300, new Vec3(X + 30, Y + 12, Z + 0.5))
  await sleep(100)
  const damage = /data: ([\d.]+)d/.exec(await f.data(rcon, `${ARROWS.slice(0, -1)},limit=1]`, 'damage'))
  const motion = /\[(-?[\d.E-]+)d, (-?[\d.E-]+)d, (-?[\d.E-]+)d\]/.exec(await f.data(rcon, `${ARROWS.slice(0, -1)},limit=1]`, 'Motion'))
  assert.ok(damage && motion, 'No arrow found')
  return { damage: Number(damage[1]), speed: Math.hypot(Number(motion[1]), Number(motion[2]), Number(motion[3])) }
}

test('Sniper I-III: arrows fly 20/35/50% faster, with the damage value lowered as much', async () => {
  const plain = await fullShot([])
  assert.equal(plain.damage, 2)
  for (let level = 1; level <= 3; level++) {
    const shot = await fullShot([['sniper', level]])
    const speed = [1.2, 1.35, 1.5][level - 1]
    assert.ok(Math.abs(shot.damage - 2 / speed) < 0.001, `Sniper ${level} damage value ${shot.damage}, not ${2 / speed}`)
    assert.ok(Math.abs(shot.speed / plain.speed - speed) < 0.06, `Sniper ${level} speed ${shot.speed}, ${plain.speed} without`)
  }
})

test('Ender Arrow shot while sneaking takes the shooter where it lands', async () => {
  await ready([['ender_arrow', 1]])
  bot.bot.setControlState('sneak', true)
  try {
    await shoot(1200, new Vec3(X + 15, Y, Z + 0.5))
    await sleep(1500)
  } finally {
    bot.bot.setControlState('sneak', false)
  }
  await sleep(500)
  const answer = await f.data(rcon, BOT, 'Pos')
  const x = Number(/\[(-?[\d.]+)d/.exec(answer)[1])
  assert.ok(x > X + 8, `the bot is at x ${x}`)
})
