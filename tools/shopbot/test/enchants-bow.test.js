'use strict'
// ImusEnchants bow enchants on a survival bot shooting at still cows and down a lane: Poison Arrow, Hunter,
// Retrieval, Sniper and Ender Arrow. Chance enchants always trigger (testing.always-trigger).
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

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await f.setUp(rcon, BOT, X, Z)
  // A lane to shoot down
  await rcon.command(`fill ${X} ${Y - 1} ${Z - 2} ${X + 60} ${Y - 1} ${Z + 2} stone`)
  await rcon.command(`fill ${X} ${Y} ${Z - 2} ${X + 60} ${Y + 6} ${Z + 2} air`)
})

after(async () => {
  bot?.bot.setControlState('sneak', false)
  await rcon?.command('minecraft:kill @e[tag=ie]')
  await f.tearDown(rcon, bot)
  rcon?.close()
})

/**
 * The bot in its spot facing down the lane, with the bow and arrows.
 */
async function ready (enchants) {
  await rcon.command('minecraft:kill @e[tag=ie]')
  await rcon.command(`minecraft:kill @e[type=arrow,x=${X},y=${Y},z=${Z},distance=..80]`)
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
  await sleep(1500)
}

/**
 * A still cow with 100 health six blocks down the lane.
 */
async function cow () {
  await f.summon(rcon, 'cow', X + 6.5, Y, Z + 0.5)
  await f.setMobHealth(rcon, COW, 100, 100)
  await sleep(300)
}

const cowCenter = () => new Vec3(X + 6.5, Y + 0.9, Z + 0.5)

test('Poison Arrow poisons what it hits', async () => {
  await ready([['poison_arrow', 3]])
  await cow()
  await shoot(1200, cowCenter())
  assert.ok((await f.effects(rcon, COW)).includes('minecraft:poison'))
})

/**
 * The damage a half drawn arrow does to a cow; a half drawn arrow is never a critical hit.
 */
async function arrowDamage (enchants) {
  await ready(enchants)
  await cow()
  await shoot(600, cowCenter())
  return 100 - await f.health(rcon, COW)
}

test('Hunter makes arrows hurt animals more', async () => {
  const plain = await arrowDamage([])
  const hunter = await arrowDamage([['hunter', 3]])
  assert.ok(plain > 0, 'the plain arrow hit')
  assert.ok(hunter >= plain * 1.25, `${hunter} with Hunter III, ${plain} without`)
})

test('Retrieval gives the arrow back', async () => {
  await ready([['retrieval', 3]])
  await rcon.command(`fill ${X + 6} ${Y} ${Z - 1} ${X + 6} ${Y + 2} ${Z + 1} stone`)
  const arrows = await f.count(rcon, BOT, 'arrow')
  await shoot(800, new Vec3(X + 6, Y + 1.5, Z + 0.5))
  assert.equal(await f.count(rcon, BOT, 'arrow'), arrows)
  await rcon.command(`fill ${X + 6} ${Y} ${Z - 1} ${X + 6} ${Y + 2} ${Z + 1} air`)
})

/**
 * How far down the lane a half drawn arrow shot straight ahead lands.
 */
async function range (enchants) {
  await ready(enchants)
  await shoot(500, new Vec3(X + 60, Y + 1.62, Z + 0.5))
  await sleep(1500)
  const answer = await rcon.command(`data get entity @e[type=arrow,x=${X},y=${Y},z=${Z},distance=..80,limit=1,sort=nearest] Pos`)
  const match = /\[(-?[\d.]+)d/.exec(answer)
  assert.ok(match, 'No arrow found: ' + answer)
  return Number(match[1]) - X
}

test('Sniper arrows fly further', async () => {
  const plain = await range([])
  const sniper = await range([['sniper', 3]])
  assert.ok(sniper >= plain * 1.25, `${sniper} blocks with Sniper III, ${plain} without`)
})

test('Ender Arrow shot while sneaking takes the shooter where it lands', async () => {
  await ready([['ender_arrow', 1]])
  bot.bot.setControlState('sneak', true)
  try {
    await shoot(1200, new Vec3(X + 15, Y, Z + 0.5))
  } finally {
    bot.bot.setControlState('sneak', false)
  }
  await sleep(500)
  const answer = await f.data(rcon, BOT, 'Pos')
  const x = Number(/\[(-?[\d.]+)d/.exec(answer)[1])
  assert.ok(x > X + 8, `the bot is at x ${x}`)
})
