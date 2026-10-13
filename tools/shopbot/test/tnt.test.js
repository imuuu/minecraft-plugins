'use strict'
// imusTNT: a Chunk TNT lit with flint and steel clears its layer of the chunk but leaves chests, a Chunk Stone
// TNT fills the layer with stone, special TNT drops itself when broken, and admins take them from /imustnt.
// The bot plays in creative, so the blasts can't hurt it.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Vec3 } = require('vec3')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer } = require('../lib/fixture')

const BOT = 'TntBot'
// A chunk of its own: x and z 336-351, the layer at y 100
const MIN = 336
const MAX = 351
const Y = 100

let rcon
let bot

/**
 * A fresh layer of dirt on stone, with a chest in it and room above.
 */
async function buildLayer (block = 'dirt') {
  await rcon.command(`fill ${MIN} ${Y - 1} ${MIN} ${MAX} ${Y - 1} ${MAX} stone`)
  await rcon.command(`fill ${MIN} ${Y} ${MIN} ${MAX} ${Y} ${MAX} ${block}`)
  await rcon.command(`fill ${MIN} ${Y + 1} ${MIN} ${MAX} ${Y + 4} ${MAX} air`)
  await rcon.command(`setblock 340 ${Y} 340 chest`)
}

async function isBlock (x, y, z, block) {
  return /passed/i.test(await rcon.command(`execute if block ${x} ${y} ${z} ${block}`))
}

/**
 * Waits until the block at x, y, z is the given one.
 */
async function waitForBlock (x, y, z, block, message, timeoutMs = 3000) {
  const deadline = Date.now() + timeoutMs
  while (!await isBlock(x, y, z, block)) {
    if (Date.now() > deadline) throw new Error(message)
    await sleep(200)
  }
}

/**
 * Gives the bot the special TNT, places it in the layer at x, z and lights it.
 */
async function placeAndLight (type, x, z) {
  await rcon.command(`setblock ${x} ${Y} ${z} air`)
  await rcon.command(`clear ${BOT}`)
  await rcon.command(`imustnt give ${type} ${BOT} 1`)
  await rcon.command(`give ${BOT} flint_and_steel`)
  await rcon.command(`tp ${BOT} ${x + 0.5} ${Y + 1} ${z - 2.5} 0 30`)
  await sleep(1500)
  const tnt = await bot.until(() => bot.bot.inventory.items().find(item => item.name === 'tnt'), 3000, 'No TNT given')
  await bot.bot.equip(tnt, 'hand')
  await bot.until(() => bot.bot.blockAt(new Vec3(x, Y - 1, z))?.name === 'stone', 3000, 'The bot does not see the layer')
  await bot.bot.placeBlock(bot.bot.blockAt(new Vec3(x, Y - 1, z)), new Vec3(0, 1, 0))
  await waitForBlock(x, Y, z, 'tnt', 'The TNT was not placed')
  const flint = bot.bot.inventory.items().find(item => item.name === 'flint_and_steel')
  await bot.bot.equip(flint, 'hand')
  await bot.bot.activateBlock(bot.bot.blockAt(new Vec3(x, Y, z)))
}

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT)
  await rcon.command(`gamemode creative ${BOT}`)
  await rcon.command('ia config imustnt set fuse-ticks 20')
  await rcon.command('ia config imustnt set layers 1')
})

after(async () => {
  await rcon?.command('ia config imustnt set fuse-ticks 80')
  await rcon?.command(`gamemode survival ${BOT}`)
  bot?.quit()
  rcon?.close()
})

test('Chunk TNT clears its layer of the chunk but leaves chests', async () => {
  await buildLayer('dirt')
  await placeAndLight('chunk-tnt', 344, 344)
  // A lit special TNT isn't a block any more
  await waitForBlock(344, Y, 344, 'air', 'The TNT was not lit')
  await sleep(2500)
  assert.ok(await isBlock(MIN, Y, MIN, 'air'), 'a corner of the layer is cleared')
  assert.ok(await isBlock(MAX, Y, MAX, 'air'), 'the other corner too')
  assert.ok(await isBlock(340, Y, 340, 'chest'), 'the chest stays')
  assert.ok(await isBlock(MIN, Y - 1, MIN, 'stone'), 'the layer below stays')
})

test('Chunk Stone TNT fills its layer with stone', async () => {
  await buildLayer('dirt')
  await rcon.command(`setblock 338 ${Y} 338 air`)
  await rcon.command(`setblock 339 ${Y} 338 water`)
  await placeAndLight('chunk-stone-tnt', 346, 346)
  await sleep(3000)
  assert.ok(await isBlock(338, Y, 338, 'stone'), 'a hole is filled')
  assert.ok(await isBlock(339, Y, 338, 'stone'), 'water is replaced')
  assert.ok(await isBlock(MAX, Y, MAX, 'stone'))
  assert.ok(await isBlock(340, Y, 340, 'chest'), 'the chest stays')
})

test('special TNT drops itself when broken, and only flint and steel lights it', async () => {
  await buildLayer('dirt')
  await rcon.command(`setblock 344 ${Y} 344 air`)
  await rcon.command(`clear ${BOT}`)
  await rcon.command(`imustnt give chunk-tnt ${BOT} 1`)
  await rcon.command(`tp ${BOT} 344.5 ${Y + 1} 341.5 0 30`)
  await sleep(1500)
  await bot.bot.equip(bot.bot.inventory.items().find(item => item.name === 'tnt'), 'hand')
  await bot.until(() => bot.bot.blockAt(new Vec3(344, Y - 1, 344))?.name === 'stone', 3000, 'The bot does not see the layer')
  await bot.bot.placeBlock(bot.bot.blockAt(new Vec3(344, Y - 1, 344)), new Vec3(0, 1, 0))
  await waitForBlock(344, Y, 344, 'tnt', 'The TNT was not placed')

  // Redstone doesn't light it
  await rcon.command(`setblock 345 ${Y} 344 redstone_block`)
  await sleep(1500)
  assert.ok(await isBlock(344, Y, 344, 'tnt'), 'redstone left it alone')
  await rcon.command(`setblock 345 ${Y} 344 dirt`)

  await rcon.command(`gamemode survival ${BOT}`)
  await rcon.command(`clear ${BOT}`)
  await sleep(500)
  await bot.bot.dig(bot.bot.blockAt(new Vec3(344, Y, 344)))
  // Walk over the drop to pick it up
  await rcon.command(`tp ${BOT} 344.5 ${Y} 344.5`)
  const tnt = await bot.until(() => bot.bot.inventory.items().find(item => item.name === 'tnt'), 4000, 'The TNT did not drop')
  assert.match(JSON.stringify(tnt.customName ?? tnt), /Chunk TNT/, 'it is still the special TNT')
  await rcon.command(`gamemode creative ${BOT}`)
})

test('admins take special TNT from the menu', async () => {
  await rcon.command(`clear ${BOT}`)
  const window = bot.nextWindow(5000, '/imustnt did not open the menu')
  await bot.command('imustnt')
  await window
  await bot.click(1, 'right')
  bot.closeWindow()
  await bot.until(() => bot.count('tnt') === 64, 3000, () => `Got ${bot.count('tnt')} TNT`)
})
