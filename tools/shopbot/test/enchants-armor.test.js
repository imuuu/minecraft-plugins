'use strict'
// ImusEnchants armor enchants at every level on a survival bot: Night Vision, Aquatic, Jump Boost, Saturation,
// Fall Guard, Magma Walker, Regrowth, Reflect and Stopping Force. Chance enchants are checked at their chance:
// a roll just below it triggers, the chance itself doesn't (testing.fixed-roll).
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { sleep } = require('../lib/shopbot')
const f = require('../lib/enchant-fixture')

const BOT = 'ArmorBot'
const X = 400
const Z = 300
const Y = f.Y

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await f.setUp(rcon, BOT, X, Z)
  await rcon.command('gamerule natural_health_regeneration false')
})

after(async () => {
  bot?.bot.clearControlStates()
  await rcon?.command('minecraft:kill @e[tag=ie]')
  await f.tearDown(rcon, bot)
  rcon?.close()
})

/**
 * Takes all armor off and puts the bot back in the middle of its floor, healthy and fed.
 */
async function reset () {
  for (const slot of ['head', 'chest', 'legs', 'feet'])
    await rcon.command(`minecraft:item replace entity ${BOT} armor.${slot} with air`)
  await rcon.command('minecraft:kill @e[tag=ie]')
  await f.floor(rcon, X, Z)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await f.refresh(rcon, BOT)
  await sleep(500)
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

test('Night Vision on a helmet gives night vision', async () => {
  await reset()
  await f.wear(rcon, bot, 'head', 'iron_helmet', [['night_vision', 1]])
  await f.waitForEffect(rcon, BOT, 'night_vision')
})

test('Aquatic on a helmet lets the bot breathe under water', async () => {
  await reset()
  await f.wear(rcon, bot, 'head', 'iron_helmet', [['aquatic', 1]])
  await sleep(1500)
  assert.ok(!(await f.effects(rcon, BOT)).includes('minecraft:water_breathing'), 'not on land')
  await rcon.command(`fill ${X - 1} ${Y} ${Z - 1} ${X + 1} ${Y + 2} ${Z + 1} water`)
  await f.waitForEffect(rcon, BOT, 'water_breathing')
})

test('Jump Boost I-II on boots gives jump boost I-II', async () => {
  for (let level = 1; level <= 2; level++) {
    await reset()
    await rcon.command(`minecraft:effect clear ${BOT}`)
    await f.wear(rcon, bot, 'feet', 'iron_boots', [['jump_boost', level]])
    await f.waitForEffect(rcon, BOT, 'jump_boost')
    assert.equal((await f.effect(rcon, BOT, 'jump_boost')).amplifier, level - 1)
  }
})

test('Saturation I-III: 25/40/55% chance to keep the hunger bar', async () => {
  // Without it, strong hunger empties the bar
  await reset()
  await rcon.command(`minecraft:effect give ${BOT} hunger 6 255 true`)
  await sleep(6500)
  assert.ok(await f.number(rcon, BOT, 'foodLevel') < 20, 'hunger works without the enchant')

  await checkChances([0.25, 0.40, 0.55], async level => {
    await reset()
    await f.wear(rcon, bot, 'head', 'iron_helmet', [['saturation', level]])
    const food = await f.number(rcon, BOT, 'foodLevel')
    await rcon.command(`minecraft:effect give ${BOT} hunger 6 255 true`)
    await sleep(6500)
    return await f.number(rcon, BOT, 'foodLevel') === food
  })
})

/**
 * Drops the bot from 11 blocks up and returns the damage it took.
 */
async function fall () {
  await f.refresh(rcon, BOT)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y + 11} ${Z + 0.5}`)
  // Wait until the server has the bot on the floor again
  const deadline = Date.now() + 8000
  await sleep(500)
  while (!/, 100\.0d,/.test(await f.data(rcon, BOT, 'Pos'))) {
    if (Date.now() > deadline) throw new Error('The bot did not land')
    await sleep(200)
  }
  await sleep(500)
  return 20 - await f.health(rcon, BOT)
}

test('Fall Guard I-II takes 25/50% of the fall damage off', async () => {
  await reset()
  const plain = await fall()
  assert.ok(plain >= 6, `a plain fall hurts: ${plain}`)
  for (let level = 1; level <= 2; level++) {
    await reset()
    await f.wear(rcon, bot, 'feet', 'iron_boots', [['fall_guard', level]])
    const guarded = await fall()
    const expected = plain * (1 - 0.25 * level)
    assert.ok(Math.abs(guarded - expected) < 0.01, `Fall Guard ${level}: ${guarded}, not ${expected}`)
  }
})

test('Magma Walker I-II turns lava into magma within 2/3 blocks, and magma doesn\'t burn', async () => {
  for (let level = 1; level <= 2; level++) {
    await reset()
    // The bot on a stone block in a pool of lava
    await rcon.command(`fill ${X - 6} ${Y - 1} ${Z - 6} ${X + 6} ${Y - 1} ${Z + 6} lava`)
    await rcon.command(`setblock ${X} ${Y - 1} ${Z} stone`)
    await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
    await f.wear(rcon, bot, 'feet', 'iron_boots', [['magma_walker', level]])
    await sleep(500)
    // One step east
    bot.bot.setControlState('forward', true)
    await sleep(250)
    bot.bot.setControlState('forward', false)
    await sleep(300)
    const pos = /\[(-?[\d.]+)d, [\d.]+d, (-?[\d.]+)d/.exec(await f.data(rcon, BOT, 'Pos'))
    const bx = Math.floor(Number(pos[1]))
    const bz = Math.floor(Number(pos[2]))
    const radius = level + 1
    // The last step's circle: its edge is magma, one further east and south is still lava
    assert.ok(await f.isBlock(rcon, bx + radius, Y - 1, bz, 'magma_block'), `level ${level}: ${radius} east of the bot is magma`)
    assert.ok(await f.isBlock(rcon, bx, Y - 1, bz + radius, 'magma_block'), `level ${level}: ${radius} south of the bot is magma`)
    assert.ok(await f.isBlock(rcon, bx + radius + 1, Y - 1, bz, 'lava'), `level ${level}: ${radius + 1} east is lava`)
    assert.ok(await f.isBlock(rcon, bx, Y - 1, bz + radius + 1, 'lava'), `level ${level}: ${radius + 1} south is lava`)

    // Standing on magma doesn't hurt
    await rcon.command(`fill ${X - 6} ${Y - 1} ${Z - 6} ${X + 6} ${Y - 1} ${Z + 6} magma_block`)
    await f.refresh(rcon, BOT)
    await sleep(2000)
    assert.equal(await f.health(rcon, BOT), 20, `level ${level}: magma didn't hurt`)
  }
})

test('Regrowth I-III heals 1/2/3 health every 5 seconds', async () => {
  for (let level = 1; level <= 3; level++) {
    await reset()
    await f.wear(rcon, bot, 'chest', 'iron_chestplate', [['regrowth', level]])
    await rcon.command(`minecraft:damage ${BOT} 10 minecraft:generic`)
    await sleep(300)
    // The first heal is the step
    const hurt = await f.health(rcon, BOT)
    let now = hurt
    const deadline = Date.now() + 7000
    while (now === hurt && Date.now() < deadline) {
      await sleep(200)
      now = await f.health(rcon, BOT)
    }
    assert.ok(Math.abs(now - hurt - level) < 0.01, `Regrowth ${level} healed ${now - hurt}`)
  }
})

test('Reflect I-III: 10/15/20% chance to hit the attacker with 30% of the damage', async () => {
  await checkChances([0.10, 0.15, 0.20], async level => {
    await reset()
    await f.wear(rcon, bot, 'chest', 'iron_chestplate', [['reflect', level]])
    // A husk that attacks: it doesn't burn in the sun
    await rcon.command(`summon husk ${X + 1.5} ${Y} ${Z + 0.5} {Tags:["ie"],PersistenceRequired:1b}`)
    await bot.until(() => bot.bot.health < 20, 8000, 'The husk did not hit the bot')
    await sleep(100)
    const taken = 20 - await f.health(rcon, BOT)
    await rcon.command('data merge entity @e[tag=ie,limit=1] {NoAI:1b}')
    await sleep(300)
    const husk = 20 - await f.health(rcon, '@e[tag=ie,limit=1]')
    // The husk's own armor takes a little off
    if (husk > 0)
      assert.ok(husk <= taken * 0.3 + 0.01 && husk >= taken * 0.3 * 0.85, `level ${level}: the husk took ${husk} of ${taken}`)
    return husk > 0
  })
})

/**
 * Has a husk hit the bot and returns how hard the first hit knocked it back (blocks/tick, flat).
 */
async function knockback () {
  await f.refresh(rcon, BOT)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  // Standing still, so nothing adds to the push
  await sleep(1500)
  let push = 0
  const onVelocity = packet => {
    if (packet.entityId === bot.bot.entity.id && push === 0) {
      const v = packet.velocity ?? { x: packet.velocityX / 8000, z: packet.velocityZ / 8000 }
      push = Math.hypot(v.x, v.z)
    }
  }
  bot.bot._client.on('entity_velocity', onVelocity)
  await rcon.command(`summon husk ${X + 1.5} ${Y} ${Z + 0.5} {Tags:["ie"],PersistenceRequired:1b}`)
  try {
    await bot.until(() => push > 0, 8000, 'The husk did not knock the bot back')
  } finally {
    bot.bot._client.removeListener('entity_velocity', onVelocity)
    await rcon.command('minecraft:kill @e[tag=ie]')
  }
  return push
}

test('Stopping Force I-III takes 20/40/60% of the knockback off', async () => {
  await reset()
  const plain = await knockback()
  for (let level = 1; level <= 3; level++) {
    await reset()
    await f.wear(rcon, bot, 'legs', 'iron_leggings', [['stopping_force', level]])
    const stopped = await knockback()
    const expected = plain * (1 - 0.2 * level)
    assert.ok(Math.abs(stopped - expected) < plain * 0.06, `Stopping Force ${level}: ${stopped}, not ${expected}`)
  }
})
