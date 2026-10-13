'use strict'
// imusWaystones: building and confirming a waystone, room between a player's own waystones, finding one by
// touch, travelling, buying an upgrade, the cooldown, admin settings and breaking. Empties the waystone
// tables first, so the test server's waystones are gone after it.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Vec3 } = require('vec3')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer } = require('../lib/fixture')
const { sql } = require('../lib/mysql')

const OWNER = 'WayBot'
const FINDER = 'WayFinder'
const Y = 100
const Z = 260
const A = 290
const B = 296

let rcon
let owner
let finder

const set = (setting, value) => rcon.command(`ia config imuswaystones set ${setting} ${value}`)

function clearTables () {
  sql('imusWaystones', 'DELETE FROM waystones; DELETE FROM waystone_owners; DELETE FROM upgrades; DELETE FROM discovered;')
}

function count (statement) {
  return Number(sql('imusWaystones', statement).trim())
}

/**
 * An iron block and a wall at x, then the bot puts a slab on top, which finishes the waystone.
 */
async function build (bot, x) {
  await rcon.command(`setblock ${x} ${Y} ${Z} iron_block`)
  await rcon.command(`setblock ${x} ${Y + 1} ${Z} cobblestone_wall`)
  await rcon.command(`setblock ${x} ${Y + 2} ${Z} air`)
  await rcon.command(`minecraft:give ${bot.name} stone_slab`)
  await rcon.command(`tp ${bot.name} ${x + 0.5} ${Y} ${Z - 2.5} 0 20`)
  await sleep(1000)
  await bot.bot.equip(bot.bot.inventory.items().find(item => item.name === 'stone_slab'), 'hand')
  await bot.until(() => bot.bot.blockAt(new Vec3(x, Y + 1, Z))?.name === 'cobblestone_wall', 3000, 'The bot does not see the wall')
  bot.clearMessages()
  await bot.bot.placeBlock(bot.bot.blockAt(new Vec3(x, Y + 1, Z)), new Vec3(0, 1, 0))
  await bot.waitForMessage(/Waystone built!/)
}

async function rightClick (bot, x) {
  await rcon.command(`tp ${bot.name} ${x + 0.5} ${Y} ${Z - 2.5} 0 20`)
  await sleep(800)
  await bot.bot.activateBlock(bot.bot.blockAt(new Vec3(x, Y + 1, Z)))
}

async function openMenu (bot, x) {
  const window = bot.nextWindow(5000, `The waystone at ${x} opened no menu for ${bot.name}`)
  await rightClick(bot, x)
  return await window
}

function itemName (bot, slot) {
  return JSON.stringify(bot.slot(slot)?.customName ?? '').replace(/§./g, '')
}

function near (bot, x) {
  const p = bot.bot.entity.position
  return Math.hypot(p.x - (x + 0.5), p.z - (Z + 0.5)) < 3
}

before(async () => {
  rcon = await Rcon.connect()
  clearTables()
  await rcon.command('waystones reload')
  owner = await joinServer(OWNER)
  finder = await joinServer(FINDER)
  await resetPlayer(rcon, OWNER)
  await resetPlayer(rcon, FINDER)
  // The floor can only be made once the bots have loaded the chunks
  for (const name of [OWNER, FINDER])
    await rcon.command(`tp ${name} ${A} ${Y} ${Z - 3}`)
  await sleep(1500)
  await rcon.command(`fill ${A - 5} ${Y - 1} ${Z - 5} ${B + 5} ${Y - 1} ${Z + 5} stone`)
  await rcon.command(`fill ${A - 5} ${Y} ${Z - 5} ${B + 5} ${Y + 4} ${Z + 5} air`)
  await set('teleport.cast-seconds', 2)
  await set('min-distance-between-own', 300)
})

after(async () => {
  await set('teleport.cast-seconds', 30)
  await rcon?.command(`op ${OWNER}`)
  await rcon?.command(`op ${FINDER}`)
  await rcon?.command(`gamemode survival ${OWNER}`)
  owner?.quit()
  finder?.quit()
  clearTables()
  await rcon?.command('waystones reload')
  rcon?.close()
})

test('a finished waystone is confirmed with a click', async () => {
  await build(owner, A)
  await owner.command('waystones confirm')
  await owner.waitForMessage(/WayBot's Waystone ready!/)
  assert.equal(count('SELECT COUNT(*) FROM waystones'), 1)
  await sleep(500)
  assert.match(await rcon.command(`execute if entity @e[type=text_display,x=${A + 0.5},y=${Y + 3.2},z=${Z + 0.5},distance=..1]`),
    /passed/i, 'its name floats over it')
})

test('own waystones need room between them', async () => {
  await rcon.command(`deop ${OWNER}`)
  try {
    await build(owner, B)
    owner.clearMessages()
    await owner.command('waystones confirm')
    await owner.waitForMessage(/need 300 blocks between them; this one is 6/)
  } finally {
    await rcon.command(`op ${OWNER}`)
  }
  // Admins can build them anywhere
  await build(owner, B)
  await owner.command('waystones confirm')
  await owner.waitForMessage(/ready!/)
  assert.equal(count('SELECT COUNT(*) FROM waystones'), 2)
})

test('others find a waystone by touching it, then it is a destination', async () => {
  finder.clearMessages()
  await rightClick(finder, A)
  await finder.waitForMessage(/You found the waystone WayBot's Waystone/)
  await sleep(800)
  assert.equal(finder.bot.currentWindow, null, 'the first touch only finds it')

  await openMenu(finder, A)
  assert.equal(finder.slot(13)?.name, 'map', 'nowhere to go before finding another')
  finder.closeWindow()

  await rightClick(finder, B)
  await finder.waitForMessage(/You found the waystone/)
  await openMenu(finder, B)
  assert.equal(finder.slot(0)?.name, 'cobblestone_wall', 'the other waystone is listed')
  assert.equal(count(`SELECT COUNT(*) FROM discovered`), 4)
})

test('clicking a destination teleports there after the cast', async () => {
  await finder.waitForWindow()
  await finder.click(0)
  await finder.until(() => near(finder, A), 5000, () => `Not at the waystone: ${finder.bot.entity.position}`)
})

test('upgrades cost items and are the player\'s own', async () => {
  const openUpgrades = async () => {
    await openMenu(finder, A)
    const upgrades = finder.nextWindow(5000, 'No upgrade menu')
    await finder.click(31)
    await upgrades
  }
  await openUpgrades()
  finder.clearMessages()
  await finder.click(10)
  await finder.waitForMessage(/You don't have the items/)
  finder.closeWindow()

  // The bot's inventory only updates with no menu open
  await rcon.command(`minecraft:give ${FINDER} lapis_block 10`)
  await finder.until(() => finder.count('lapis_block') === 10, 3000, 'No lapis given')
  await openUpgrades()
  await finder.click(10)
  await finder.until(() => finder.lore(10).some(line => /Tier 1\/9/.test(line)), 3000, () => finder.lore(10).join('\n'))
  assert.deepEqual(finder.ownSlotsWith('lapis_block'), [], 'the lapis was paid')
  await sleep(500)
  assert.equal(count(`SELECT tier FROM upgrades WHERE upgrade_name = 'CAST_TIME'`), 1)
  finder.closeWindow()
})

test('players wait for the cooldown of the waystone they left from', async () => {
  await rcon.command(`deop ${FINDER}`)
  try {
    await openMenu(finder, A)
    await finder.click(0)
    // Cast Time tier 1 takes the 2 seconds down to the 1 second minimum
    await finder.until(() => near(finder, B), 5000, () => `Not at the other waystone: ${finder.bot.entity.position}`)
    await openMenu(finder, A)
    assert.match(itemName(finder, 13), /You can leave from here again in/)
    finder.closeWindow()
  } finally {
    await rcon.command(`op ${FINDER}`)
  }
})

test('admins set who sees a waystone', async () => {
  await openMenu(owner, A)
  await owner.click(28)
  await owner.until(() => owner.lore(28).some(line => /everyone/.test(line)), 3000, () => owner.lore(28).join('\n'))
  owner.closeWindow()
  await sleep(500)
  assert.equal(sql('imusWaystones', `SELECT visibility_type FROM waystones WHERE loc_x = ${A}`).trim(), 'TO_ALL')
})

test('breaking a waystone removes it, but not in creative', async () => {
  await rcon.command(`gamemode creative ${OWNER}`)
  owner.clearMessages()
  await rcon.command(`tp ${OWNER} ${B + 0.5} ${Y} ${Z - 2.5} 0 20`)
  await sleep(800)
  await owner.bot.dig(owner.bot.blockAt(new Vec3(B, Y + 1, Z)))
  await owner.waitForMessage(/In creative, remove a waystone from its menu/)
  assert.match(await rcon.command(`execute if block ${B} ${Y + 1} ${Z} cobblestone_wall`), /passed/i)

  await rcon.command(`gamemode survival ${OWNER}`)
  await rcon.command(`minecraft:give ${OWNER} iron_pickaxe`)
  await sleep(500)
  await owner.bot.equip(owner.bot.inventory.items().find(item => item.name === 'iron_pickaxe'), 'hand')
  owner.bot.digTime = () => 1500
  owner.clearMessages()
  await owner.bot.dig(owner.bot.blockAt(new Vec3(B, Y + 1, Z)))
  await owner.waitForMessage(/destroyed/)
  await sleep(500)
  assert.equal(count(`SELECT COUNT(*) FROM waystones WHERE loc_x = ${B}`), 0)
  assert.match(await rcon.command(`execute if entity @e[type=text_display,x=${B + 0.5},y=${Y + 3.2},z=${Z + 0.5},distance=..1]`),
    /failed/i, 'its name is gone')
})
