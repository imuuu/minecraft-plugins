'use strict'
// ImusEnchants melee enchants at every level, on a survival bot hitting still mobs. Pigs have no armor, so
// the damage is exact: an iron sword does 6 and an iron axe 9. Chance enchants are checked at their
// chance: a roll just below it triggers, the chance itself doesn't (testing.fixed-roll).
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { sleep } = require('../lib/shopbot')
const f = require('../lib/enchant-fixture')

const BOT = 'CombatBot'
const X = 420
const Z = 300
const Y = f.Y
const MOB = '@e[tag=ie,limit=1,sort=nearest]'
// Zombies wear a helmet so the sun doesn't burn them
const HELMET = 'equipment:{head:{id:"minecraft:leather_helmet",count:1}},drop_chances:{head:0.0f}'

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await f.setUp(rcon, BOT, X, Z)
  await rcon.command('gamerule natural_health_regeneration false')
})

after(async () => {
  await rcon?.command('minecraft:kill @e[tag=ie]')
  await f.tearDown(rcon, bot)
  rcon?.close()
})

async function reset () {
  await rcon.command('minecraft:kill @e[tag=ie]')
  await rcon.command(`minecraft:clear ${BOT}`)
  await f.floor(rcon, X, Z)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await f.refresh(rcon, BOT)
}

/**
 * Hits the closest mob of the type with a fully charged swing.
 */
async function hit (type) {
  const target = await f.mob(bot, type)
  // A full swing: an axe takes over a second to charge
  await sleep(1800)
  await bot.bot.lookAt(target.position.offset(0, target.height / 2, 0), true)
  bot.bot.attack(target)
  await sleep(500)
}

/**
 * A still mob two blocks in front of the bot with 100 health, hit once with the weapon.
 */
async function hitMob (weapon, enchants, type = 'pig') {
  await reset()
  await f.hold(rcon, bot, weapon, enchants)
  await f.summon(rcon, type, X + 2.5, Y, Z + 0.5)
  await f.setMobHealth(rcon, MOB, 100, 100)
  await hit(type)
}

/**
 * For each level: a roll just below its chance triggers the enchant, a roll at its chance doesn't.
 * triggered(level) does one try and tells whether it triggered.
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

/**
 * Checks a hit effect at every level: its chance, strength and length.
 */
async function checkEffect (key, effect, chances, amplifier, ticks) {
  await checkChances(chances, async level => {
    await hitMob('iron_sword', [[key, level]])
    const got = await f.effect(rcon, MOB, effect)
    if (got) {
      assert.equal(got.amplifier, amplifier(level), `${key} ${level} ${effect} strength`)
      assert.ok(got.duration <= ticks(level) && got.duration > ticks(level) - 30, `${key} ${level}: ${got.duration} ticks of ${effect}, not ${ticks(level)}`)
    }
    return got !== null
  })
}

test('Venom I-III: 10/15/20% poison for 4/5/6 s, stronger at III', async () => {
  await checkEffect('venom', 'poison', [0.10, 0.15, 0.20], level => level >= 3 ? 1 : 0, level => 60 + 20 * level)
})

test('Withering I-II: 10/15% wither for 4/5 s', async () => {
  await checkEffect('withering', 'wither', [0.10, 0.15], () => 0, level => 60 + 20 * level)
})

test('Frost I-II: 15/25% slowness II for 3/4 s', async () => {
  await checkEffect('frost', 'slowness', [0.15, 0.25], () => 1, level => 40 + 20 * level)
})

test('a sword without enchants does 6 damage and nothing else', async () => {
  await hitMob('iron_sword', [])
  assert.deepEqual(await f.effects(rcon, MOB), [])
  assert.equal(100 - await f.health(rcon, MOB), 6)
})

test('Lifesteal I-III: 10/15/20% chance to heal 15/25/35% of the damage', async () => {
  const heal = [0.15, 0.25, 0.35]
  await checkChances([0.10, 0.15, 0.20], async level => {
    await reset()
    await f.hold(rcon, bot, 'iron_sword', [['lifesteal', level]])
    await rcon.command(`minecraft:damage ${BOT} 10 minecraft:generic`)
    await sleep(300)
    const hurt = await f.health(rcon, BOT)
    await f.summon(rcon, 'pig', X + 2.5, Y, Z + 0.5)
    await f.setMobHealth(rcon, MOB, 100, 100)
    await hit('pig')
    const healed = await f.health(rcon, BOT) - hurt
    if (healed > 0)
      assert.ok(Math.abs(healed - 6 * heal[level - 1]) < 0.01, `level ${level} healed ${healed}, not ${6 * heal[level - 1]}`)
    return healed > 0
  })
})

test('Execute I-III: +10/20/30% damage below 30% health, nothing above', async () => {
  for (let level = 0; level <= 3; level++) {
    for (const [now, weak] of [[25, true], [35, false]]) {
      await reset()
      await f.hold(rcon, bot, 'iron_sword', level ? [['execute', level]] : [])
      await f.summon(rcon, 'pig', X + 2.5, Y, Z + 0.5)
      await f.setMobHealth(rcon, MOB, 100, now)
      await hit('pig')
      const damage = now - await f.health(rcon, MOB)
      const expected = 6 * (weak && level ? 1 + 0.1 * level : 1)
      assert.ok(Math.abs(damage - expected) < 0.01, `Execute ${level} at ${now}%: ${damage}, not ${expected}`)
    }
  }
})

test('Cleave I-III: the mob beside takes 20/30/40% of an axe hit', async () => {
  for (let level = 1; level <= 3; level++) {
    await reset()
    await f.hold(rcon, bot, 'iron_axe', [['cleave', level]])
    await f.summon(rcon, 'pig', X + 2.5, Y, Z + 0.5)
    await f.summon(rcon, 'pig', X + 2.5, Y, Z + 1.7, 'Tags:["ie","beside"]')
    for (const target of [MOB, '@e[tag=beside,limit=1]'])
      await f.setMobHealth(rcon, target, 100, 100)
    await hit('pig')
    const beside = 100 - await f.health(rcon, '@e[tag=beside,limit=1]')
    const expected = 9 * [0.2, 0.3, 0.4][level - 1]
    assert.ok(Math.abs(beside - expected) < 0.01, `Cleave ${level}: ${beside}, not ${expected}`)
  }
})

/**
 * Kills a one health mob of the type with the held weapon.
 */
async function kill (type, extra = '') {
  await f.summon(rcon, type, X + 2.5, Y, Z + 0.5, 'Health:1f' + (extra ? ',' + extra : ''))
  await hit(type)
  await sleep(500)
}

test('Beheading I-III: 5/10/15% chance for the head', async () => {
  await checkChances([0.05, 0.10, 0.15], async level => {
    await reset()
    await f.hold(rcon, bot, 'iron_sword', [['beheading', level]])
    await kill('zombie', `IsBaby:0b,${HELMET}`)
    return await f.itemsOnGround(rcon, 'zombie_head', X + 2, Y, Z) + await f.count(rcon, BOT, 'zombie_head') > 0
  })
})

test('Scavenger I-II: 10/20% chance to double a drop', async () => {
  // A cod always drops one cod
  await checkChances([0.10, 0.20], async level => {
    await reset()
    await f.hold(rcon, bot, 'iron_sword', [['scavenger', level]])
    await kill('cod')
    const cod = await f.itemsOnGround(rcon, 'cod', X + 2, Y, Z) + await f.count(rcon, BOT, 'cod')
    assert.ok(cod === 1 || cod === 2, `${cod} cod`)
    return cod === 2
  })
})

test('Telekinesis puts the drops straight in the inventory', async () => {
  await reset()
  await f.hold(rcon, bot, 'iron_sword', [['telekinesis', 1]])
  await kill('cod')
  assert.equal(await f.count(rcon, BOT, 'cod'), 1, 'the cod is in the inventory')
  assert.equal(await f.itemsOnGround(rcon, 'cod', X + 2, Y, Z), 0, 'nothing on the ground')
})

test('Wisdom I-III on a sword: +25/50/75% XP from mobs', async () => {
  // A husk gives 5 XP (worn items would add a random bonus); Telekinesis collects it at once
  const expected = [5, 6, 8, 9]
  for (let level = 0; level <= 3; level++) {
    await reset()
    await f.hold(rcon, bot, 'iron_sword', level ? [['telekinesis', 1], ['wisdom', level]] : [['telekinesis', 1]])
    const before = await f.xpPoints(rcon, BOT)
    await kill('husk', 'IsBaby:0b')
    assert.equal(await f.xpPoints(rcon, BOT) - before, expected[level], `Wisdom ${level}`)
  }
})

test('an enchant only goes on the items it is for', async () => {
  await reset()
  await rcon.command(`minecraft:item replace entity ${BOT} hotbar.0 with iron_pickaxe`)
  bot.bot.setQuickBarSlot(0)
  await sleep(300)
  assert.match((await rcon.command(`ien enchant venom 1 ${BOT}`)).replace(/§./g, ''), /doesn't go on iron_pickaxe/)
})
