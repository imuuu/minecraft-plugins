'use strict'
// ImusEnchants tool enchants on a survival bot: Tunnel, Vein Miner, Timber, Smelting, Telekinesis, Glass
// Breaker, Haste, Harvest and Replanter.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Vec3 } = require('vec3')
const { Rcon } = require('../lib/rcon')
const { sleep } = require('../lib/shopbot')
const f = require('../lib/enchant-fixture')

const BOT = 'ToolBot'
const X = 400
const Z = 340
const Y = f.Y

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await f.setUp(rcon, BOT, X, Z)
})

after(async () => {
  bot?.bot.setControlState('sneak', false)
  await f.tearDown(rcon, bot)
  rcon?.close()
})

async function reset () {
  await f.floor(rcon, X, Z)
  await rcon.command(`minecraft:clear ${BOT}`)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await sleep(500)
}

async function dig (x, y, z) {
  await bot.until(() => bot.bot.blockAt(new Vec3(x, y, z))?.name !== 'air', 3000, `The bot does not see the block at ${x} ${y} ${z}`)
  await bot.bot.dig(bot.bot.blockAt(new Vec3(x, y, z)), true)
  await sleep(500)
}

async function allAir (blocks) {
  const left = []
  for (const [x, y, z] of blocks)
    if (!await f.isBlock(rcon, x, y, z, 'air')) left.push(`${x} ${y} ${z}`)
  return left
}

test('Tunnel III mines a 3x3 square', async () => {
  await reset()
  await rcon.command(`fill ${X + 2} ${Y} ${Z - 1} ${X + 2} ${Y + 2} ${Z + 1} stone`)
  await f.hold(rcon, bot, 'diamond_pickaxe', [['tunnel', 3]])
  await dig(X + 2, Y + 1, Z)
  const square = []
  for (let y = Y; y <= Y + 2; y++)
    for (let z = Z - 1; z <= Z + 1; z++) square.push([X + 2, y, z])
  assert.deepEqual(await allAir(square), [])
})

test('Vein Miner mines the connected ores when sneaking', async () => {
  await reset()
  await rcon.command(`fill ${X + 2} ${Y} ${Z - 2} ${X + 2} ${Y} ${Z + 2} coal_ore`)
  await rcon.command(`setblock ${X + 2} ${Y + 1} ${Z + 2} coal_ore`)
  await f.hold(rcon, bot, 'diamond_pickaxe', [['vein_miner', 1]])
  bot.bot.setControlState('sneak', true)
  try {
    await sleep(300)
    await dig(X + 2, Y, Z)
  } finally {
    bot.bot.setControlState('sneak', false)
  }
  const vein = [[X + 2, Y + 1, Z + 2]]
  for (let z = Z - 2; z <= Z + 2; z++) vein.push([X + 2, Y, z])
  assert.deepEqual(await allAir(vein), [])
})

test('Timber fells a whole tree', async () => {
  await reset()
  await rcon.command(`fill ${X + 2} ${Y} ${Z} ${X + 2} ${Y + 4} ${Z} oak_log`)
  await rcon.command(`fill ${X + 1} ${Y + 3} ${Z - 1} ${X + 3} ${Y + 5} ${Z + 1} oak_leaves[persistent=false] replace air`)
  await f.hold(rcon, bot, 'diamond_axe', [['timber', 1]])
  await dig(X + 2, Y, Z)
  const logs = []
  for (let y = Y; y <= Y + 4; y++) logs.push([X + 2, y, Z])
  assert.deepEqual(await allAir(logs), [])
})

test('Smelting drops ingots instead of raw ore', async () => {
  await reset()
  await rcon.command(`setblock ${X + 2} ${Y} ${Z} iron_ore`)
  await f.hold(rcon, bot, 'diamond_pickaxe', [['smelting', 1]])
  await dig(X + 2, Y, Z)
  const ingots = await f.itemsOnGround(rcon, 'iron_ingot', X + 2, Y, Z) + await f.count(rcon, BOT, 'iron_ingot')
  const raw = await f.itemsOnGround(rcon, 'raw_iron', X + 2, Y, Z) + await f.count(rcon, BOT, 'raw_iron')
  assert.equal(ingots, 1)
  assert.equal(raw, 0)
})

test('Telekinesis puts the mined block in the inventory', async () => {
  await reset()
  await rcon.command(`setblock ${X + 2} ${Y} ${Z} stone`)
  await f.hold(rcon, bot, 'diamond_pickaxe', [['telekinesis', 1]])
  await dig(X + 2, Y, Z)
  assert.equal(await f.count(rcon, BOT, 'cobblestone'), 1)
  assert.equal(await f.itemsOnGround(rcon, 'cobblestone', X + 2, Y, Z), 0)
})

/**
 * Starts mining glass at x + 2 with a slow dig and tells whether it broke within half a second.
 */
async function glassBreaksAtOnce (enchants) {
  await reset()
  await rcon.command(`setblock ${X + 2} ${Y} ${Z} glass`)
  await f.hold(rcon, bot, 'diamond_pickaxe', enchants)
  await bot.until(() => bot.bot.blockAt(new Vec3(X + 2, Y, Z))?.name === 'glass', 3000, 'The bot does not see the glass')
  const slow = bot.bot.digTime
  bot.bot.digTime = () => 5000
  const digging = bot.bot.dig(bot.bot.blockAt(new Vec3(X + 2, Y, Z)), true).catch(() => {})
  await sleep(600)
  const broke = await f.isBlock(rcon, X + 2, Y, Z, 'air')
  try {
    bot.bot.stopDigging()
  } catch {}
  await digging
  bot.bot.digTime = slow
  return broke
}

test('Glass Breaker breaks glass at once', async () => {
  assert.equal(await glassBreaksAtOnce([]), false, 'without it the glass takes time')
  assert.equal(await glassBreaksAtOnce([['glass_breaker', 1]]), true)
})

test('Haste on a held tool gives haste', async () => {
  await reset()
  await f.hold(rcon, bot, 'diamond_pickaxe', [['haste', 2]])
  await f.waitForEffect(rcon, BOT, 'haste')
})

/**
 * Grown wheat on farmland, 3x3 around x + 3, z.
 */
async function field () {
  await rcon.command(`fill ${X + 2} ${Y - 1} ${Z - 1} ${X + 4} ${Y - 1} ${Z + 1} farmland`)
  await rcon.command(`fill ${X + 2} ${Y} ${Z - 1} ${X + 4} ${Y} ${Z + 1} wheat[age=7]`)
}

test('Harvest harvests the grown crops around', async () => {
  await reset()
  await field()
  await f.hold(rcon, bot, 'diamond_hoe', [['harvest', 1]])
  await dig(X + 3, Y, Z)
  const grown = []
  for (let x = X + 2; x <= X + 4; x++)
    for (let z = Z - 1; z <= Z + 1; z++)
      if (await f.isBlock(rcon, x, Y, z, 'wheat[age=7]')) grown.push(`${x} ${z}`)
  assert.deepEqual(grown, [])
})

test('Replanter plants the crop again', async () => {
  await reset()
  await field()
  await f.hold(rcon, bot, 'diamond_hoe', [['replanter', 1]])
  // A crop that drops no seed (about 1 in 12 for wheat) can't be planted again, so try three
  let planted = 0
  for (const z of [Z - 1, Z, Z + 1]) {
    await dig(X + 2, Y, z)
    if (await f.isBlock(rcon, X + 2, Y, z, 'wheat[age=0]')) planted++
  }
  assert.ok(planted >= 2, `${planted} of 3 planted again`)
})
