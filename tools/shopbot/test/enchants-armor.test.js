'use strict'
// ImusEnchants armor enchants on a survival bot: Night Vision, Aquatic, Saturation, Jump Boost, Fall Guard,
// Magma Walker, Regrowth, Reflect and Stopping Force. Chance enchants always trigger (testing.always-trigger).
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
  await f.tearDown(rcon, bot)
  rcon?.close()
})

/**
 * Takes all armor off and puts the bot back in the middle of its floor, healthy and fed.
 */
async function reset () {
  for (const slot of ['head', 'chest', 'legs', 'feet'])
    await rcon.command(`minecraft:item replace entity ${BOT} armor.${slot} with air`)
  await f.floor(rcon, X, Z)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5}`)
  await f.refresh(rcon, BOT)
  await sleep(500)
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

test('Jump Boost on boots gives jump boost', async () => {
  await reset()
  await f.wear(rcon, bot, 'feet', 'iron_boots', [['jump_boost', 2]])
  await f.waitForEffect(rcon, BOT, 'jump_boost')
})

test('Saturation on a helmet keeps the hunger bar full', async () => {
  await reset()
  // Without it, strong hunger empties the bar fast
  await rcon.command(`minecraft:effect give ${BOT} hunger 6 255 true`)
  await sleep(6500)
  assert.ok(await f.number(rcon, BOT, 'foodLevel') < 20, 'hunger works without the enchant')

  await reset()
  await f.wear(rcon, bot, 'head', 'iron_helmet', [['saturation', 3]])
  const food = await f.number(rcon, BOT, 'foodLevel')
  await rcon.command(`minecraft:effect give ${BOT} hunger 6 255 true`)
  await sleep(6500)
  assert.equal(await f.number(rcon, BOT, 'foodLevel'), food)
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

test('Fall Guard on boots takes half the fall damage off', async () => {
  await reset()
  const plain = await fall()
  assert.ok(plain >= 6, `a plain fall hurts: ${plain}`)
  await f.wear(rcon, bot, 'feet', 'iron_boots', [['fall_guard', 2]])
  const guarded = await fall()
  assert.ok(Math.abs(guarded - plain / 2) <= 1, `half of ${plain}, got ${guarded}`)
})

test('Magma Walker turns lava into magma and magma doesn\'t burn', async () => {
  await reset()
  // The bot on a stone block in a pool of lava
  await rcon.command(`fill ${X - 4} ${Y - 1} ${Z - 4} ${X + 4} ${Y - 1} ${Z + 4} lava`)
  await rcon.command(`setblock ${X} ${Y - 1} ${Z} stone`)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5}`)
  await f.wear(rcon, bot, 'feet', 'iron_boots', [['magma_walker', 2]])
  await sleep(500)
  bot.bot.setControlState('forward', true)
  await sleep(250)
  bot.bot.setControlState('forward', false)
  await sleep(500)
  let magma = 0
  for (const [dx, dz] of [[1, 0], [-1, 0], [0, 1], [0, -1], [1, 1], [-1, -1], [2, 0], [0, 2]])
    if (await f.isBlock(rcon, X + dx, Y - 1, Z + dz, 'magma_block')) magma++
  assert.ok(magma >= 3, `only ${magma} lava blocks became magma`)
  // Standing on magma doesn't hurt
  await rcon.command(`setblock ${X} ${Y - 1} ${Z} magma_block`)
  await f.refresh(rcon, BOT)
  await sleep(2000)
  assert.equal(await f.health(rcon, BOT), 20)
})

test('Regrowth on a chestplate heals over time', async () => {
  await reset()
  await f.wear(rcon, bot, 'chest', 'iron_chestplate', [['regrowth', 3]])
  await rcon.command(`minecraft:damage ${BOT} 10 minecraft:generic`)
  await sleep(300)
  const hurt = await f.health(rcon, BOT)
  await sleep(6000)
  assert.ok(await f.health(rcon, BOT) >= hurt + 3, 'healed 1.5 hearts in 5 seconds')
})

test('Reflect on a chestplate hurts the attacker back', async () => {
  await reset()
  await f.wear(rcon, bot, 'chest', 'iron_chestplate', [['reflect', 3]])
  // A husk that attacks: it doesn't burn in the sun
  await rcon.command(`summon husk ${X + 1.5} ${Y} ${Z + 0.5} {Tags:["ie"],PersistenceRequired:1b}`)
  await bot.until(() => bot.bot.health < 20, 8000, 'The husk did not hit the bot')
  await sleep(300)
  const husk = await f.health(rcon, '@e[tag=ie,limit=1]')
  assert.ok(husk < 20, `the husk took ${20 - husk} back`)
  await rcon.command('minecraft:kill @e[tag=ie]')
})

/**
 * Has a husk hit the bot and returns how hard the bot was knocked back (blocks/tick, flat).
 */
async function knockback () {
  await f.refresh(rcon, BOT)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5}`)
  await sleep(500)
  let push = 0
  const onVelocity = packet => {
    if (packet.entityId === bot.bot.entity.id) {
      const v = packet.velocity ?? { x: packet.velocityX / 8000, z: packet.velocityZ / 8000 }
      push = Math.max(push, Math.hypot(v.x, v.z))
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

test('Stopping Force on leggings takes knockback off', async () => {
  await reset()
  const plain = await knockback()
  await f.wear(rcon, bot, 'legs', 'iron_leggings', [['stopping_force', 3]])
  const stopped = await knockback()
  assert.ok(stopped < plain * 0.7, `knockback ${stopped} with it, ${plain} without`)
})
