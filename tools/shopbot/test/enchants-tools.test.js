'use strict'
// ImusEnchants tool enchants at every level on a survival bot: Tunnel, Vein Miner, Timber, Harvest, Haste and
// Wisdom, and Smelting, Telekinesis, Glass Breaker and Replanter.
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
  bot?.bot.clearControlStates()
  await f.tearDown(rcon, bot)
  rcon?.close()
})

async function reset () {
  await f.floor(rcon, X, Z)
  await rcon.command(`fill ${X + 1} ${Y} ${Z - 8} ${X + 8} ${Y + 150} ${Z + 8} air`)
  await rcon.command(`minecraft:clear ${BOT}`)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await sleep(500)
}

async function dig (x, y, z) {
  await bot.until(() => bot.bot.blockAt(new Vec3(x, y, z))?.name !== 'air', 3000, `The bot does not see the block at ${x} ${y} ${z}`)
  await bot.bot.dig(bot.bot.blockAt(new Vec3(x, y, z)), true)
  await sleep(500)
}

/**
 * The blocks of the list that aren't air any more, as "x y z".
 */
async function notAir (blocks) {
  const left = []
  for (const [x, y, z] of blocks)
    if (!await f.isBlock(rcon, x, y, z, 'air')) left.push(`${x} ${y} ${z}`)
  return left
}

test('Tunnel I-III mines 1x2, 2x2 and 3x3', async () => {
  // Mining the middle of a 3x3 wall at eye height, looking east: down is y - 1, right is south (z + 1)
  const shapes = [
    [[0, 0], [-1, 0]],
    [[0, 0], [-1, 0], [0, 1], [-1, 1]],
    [[-1, -1], [-1, 0], [-1, 1], [0, -1], [0, 0], [0, 1], [1, -1], [1, 0], [1, 1]]
  ]
  for (let level = 1; level <= 3; level++) {
    await reset()
    await rcon.command(`fill ${X + 2} ${Y} ${Z - 1} ${X + 2} ${Y + 2} ${Z + 1} stone`)
    await f.hold(rcon, bot, 'diamond_pickaxe', [['tunnel', level]])
    await dig(X + 2, Y + 1, Z)
    const mined = []
    const kept = []
    for (let dy = -1; dy <= 1; dy++) {
      for (let dz = -1; dz <= 1; dz++) {
        const inShape = shapes[level - 1].some(([y, z]) => y === dy && z === dz)
        const air = await f.isBlock(rcon, X + 2, Y + 1 + dy, Z + dz, 'air')
        if (air !== inShape) (inShape ? kept : mined).push(`${dy} ${dz}`)
      }
    }
    assert.deepEqual({ notMined: kept, minedOutside: mined }, { notMined: [], minedOutside: [] }, `Tunnel ${level}`)
  }
})

test('Vein Miner I-III mines 8/16/32 connected ores when sneaking', async () => {
  for (let level = 1; level <= 3; level++) {
    await reset()
    // 60 ores in a block
    await rcon.command(`fill ${X + 2} ${Y} ${Z - 2} ${X + 4} ${Y + 3} ${Z + 2} coal_ore`)
    await f.hold(rcon, bot, 'diamond_pickaxe', [['vein_miner', level]])
    bot.bot.setControlState('sneak', true)
    try {
      await sleep(300)
      await dig(X + 2, Y, Z)
    } finally {
      bot.bot.setControlState('sneak', false)
    }
    const left = await f.takeBlocks(rcon, X + 2, Y, Z - 2, X + 4, Y + 3, Z + 2, 'coal_ore')
    assert.equal(60 - left, [8, 16, 32][level - 1], `Vein Miner ${level}`)
  }
})

test('Vein Miner does nothing without sneaking', async () => {
  await reset()
  await rcon.command(`fill ${X + 2} ${Y} ${Z - 2} ${X + 2} ${Y} ${Z + 2} coal_ore`)
  await f.hold(rcon, bot, 'diamond_pickaxe', [['vein_miner', 3]])
  await dig(X + 2, Y, Z)
  assert.equal(await f.takeBlocks(rcon, X + 2, Y, Z - 2, X + 2, Y, Z + 2, 'coal_ore'), 4)
})

test('Timber I-III fells up to 32/64/128 more logs', async () => {
  for (let level = 1; level <= 3; level++) {
    await reset()
    // A trunk of 140 logs with natural leaves at its foot
    await rcon.command(`fill ${X + 2} ${Y} ${Z} ${X + 2} ${Y + 139} ${Z} oak_log`)
    await rcon.command(`fill ${X + 1} ${Y} ${Z - 1} ${X + 3} ${Y + 2} ${Z + 1} oak_leaves[persistent=false] replace air`)
    await f.hold(rcon, bot, 'diamond_axe', [['timber', level]])
    await dig(X + 2, Y, Z)
    await sleep(500)
    const left = await f.takeBlocks(rcon, X + 2, Y, Z, X + 2, Y + 139, Z, 'oak_log')
    assert.equal(140 - left, 1 + [32, 64, 128][level - 1], `Timber ${level}`)
  }
})

test('Timber leaves a trunk without natural leaves alone', async () => {
  await reset()
  await rcon.command(`fill ${X + 2} ${Y} ${Z} ${X + 2} ${Y + 4} ${Z} oak_log`)
  await f.hold(rcon, bot, 'diamond_axe', [['timber', 3]])
  await dig(X + 2, Y, Z)
  assert.equal(await f.takeBlocks(rcon, X + 2, Y, Z, X + 2, Y + 4, Z, 'oak_log'), 4)
})

test('Harvest I-II harvests the grown crops in 3x3 and 5x5', async () => {
  for (let level = 1; level <= 2; level++) {
    await reset()
    // 7x7 grown wheat around x + 4
    await rcon.command(`fill ${X + 1} ${Y - 1} ${Z - 3} ${X + 7} ${Y - 1} ${Z + 3} farmland`)
    await rcon.command(`fill ${X + 1} ${Y} ${Z - 3} ${X + 7} ${Y} ${Z + 3} wheat[age=7]`)
    await f.hold(rcon, bot, 'diamond_hoe', [['harvest', level]])
    await dig(X + 4, Y, Z)
    const wrong = []
    for (let dx = -3; dx <= 3; dx++) {
      for (let dz = -3; dz <= 3; dz++) {
        const inside = Math.abs(dx) <= level && Math.abs(dz) <= level
        const grown = await f.isBlock(rcon, X + 4 + dx, Y, Z + dz, 'wheat[age=7]')
        if (grown === inside) wrong.push(`${dx} ${dz}`)
      }
    }
    assert.deepEqual(wrong, [], `Harvest ${level}: wrong crops`)
  }
})

test('Replanter plants the crop again', async () => {
  await reset()
  await rcon.command(`fill ${X + 2} ${Y - 1} ${Z - 1} ${X + 2} ${Y - 1} ${Z + 1} farmland`)
  await rcon.command(`fill ${X + 2} ${Y} ${Z - 1} ${X + 2} ${Y} ${Z + 1} wheat[age=7]`)
  await f.hold(rcon, bot, 'diamond_hoe', [['replanter', 1]])
  // A crop that drops no seed (about 1 in 12 for wheat) can't be planted again, so try three
  let planted = 0
  for (const z of [Z - 1, Z, Z + 1]) {
    await dig(X + 2, Y, z)
    if (await f.isBlock(rcon, X + 2, Y, z, 'wheat[age=0]')) planted++
  }
  assert.ok(planted >= 2, `${planted} of 3 planted again`)
})

test('Haste I-II on a held tool gives haste I-II', async () => {
  for (let level = 1; level <= 2; level++) {
    await reset()
    await rcon.command(`minecraft:effect clear ${BOT}`)
    await f.hold(rcon, bot, 'diamond_pickaxe', [['haste', level]])
    await f.waitForEffect(rcon, BOT, 'haste')
    assert.equal((await f.effect(rcon, BOT, 'haste')).amplifier, level - 1)
  }
})

test('Wisdom I-III on a pickaxe: +25/50/75% XP from blocks', async () => {
  // Coal ore gives 0-2 XP at random; Wisdom makes it round(xp * (1 + bonus)), so every ore's XP is one of these
  const possible = [[0, 1, 2], [0, 1, 3], [0, 2, 3], [0, 2, 4]]
  for (let level = 0; level <= 3; level++) {
    await reset()
    await f.hold(rcon, bot, 'diamond_pickaxe', level ? [['telekinesis', 1], ['wisdom', level]] : [['telekinesis', 1]])
    const seen = new Set()
    for (let i = 0; i < 15; i++) {
      await rcon.command(`setblock ${X + 2} ${Y} ${Z} coal_ore`)
      const before = await f.xpPoints(rcon, BOT)
      await dig(X + 2, Y, Z)
      seen.add(await f.xpPoints(rcon, BOT) - before)
    }
    const odd = [...seen].filter(xp => !possible[level].includes(xp))
    assert.deepEqual(odd, [], `Wisdom ${level}: XP ${[...seen].join(', ')}`)
    // The highest one shows within 15 ores (each ore has a 1 in 3 chance)
    assert.ok(seen.has(possible[level][2]), `Wisdom ${level}: never got ${possible[level][2]} XP (got ${[...seen].join(', ')})`)
  }
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
