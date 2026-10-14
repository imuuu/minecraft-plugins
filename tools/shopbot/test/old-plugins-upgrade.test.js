'use strict'
// What the old imusWaystones and ImusSpawners left behind still works with the new ones: a waystone in the
// database with its upgrades, the old floating name (an armor stand) going away, a spawner listed in
// spawnerData.yml, a player's raised chance in playerData.yml and the old settings.yml. Stops the server to
// write the old files, so it only runs with SHOPBOT_RESTART=1.
const { test, after } = require('node:test')
const assert = require('node:assert/strict')
const fs = require('fs')
const path = require('path')
const crypto = require('crypto')
const { Vec3 } = require('vec3')
const { Rcon, SERVER_DIR } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { offlineUuid } = require('../lib/hometele-fixture')
const { sql } = require('../lib/db')
const server = require('../lib/server')

const BOT = 'OldStoneBot'
const run = process.env.SHOPBOT_RESTART === '1'
const Y = 100
const Z = 230
const WAYSTONE_X = 280
const SPAWNER_X = 286
const SPAWNERS_DIR = path.join(SERVER_DIR, 'plugins', 'ImusSpawners')

let rcon
let bot

after(async () => {
  await rcon?.command('ia config imusspawners set silk-touch-chance 14')
  bot?.quit()
  sql('imusWaystones', 'DELETE FROM waystones; DELETE FROM waystone_owners; DELETE FROM upgrades; DELETE FROM discovered;')
  await rcon?.command('waystones reload')
  rcon?.close()
})

/**
 * Builds a waystone and a spawner with commands only, so the new plugins don't know them, and leaves an old
 * armor stand name over the waystone.
 */
async function buildOldWorld () {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await rcon.command(`tp ${BOT} ${WAYSTONE_X} ${Y} ${Z - 3}`)
  await sleep(1500)
  await rcon.command(`fill ${WAYSTONE_X - 3} ${Y - 1} ${Z - 4} ${SPAWNER_X + 3} ${Y - 1} ${Z + 3} stone`)
  await rcon.command(`fill ${WAYSTONE_X - 3} ${Y} ${Z - 4} ${SPAWNER_X + 3} ${Y + 4} ${Z + 3} air`)
  await rcon.command(`setblock ${WAYSTONE_X} ${Y} ${Z} gold_block`)
  await rcon.command(`setblock ${WAYSTONE_X} ${Y + 1} ${Z} mossy_cobblestone_wall`)
  await rcon.command(`setblock ${WAYSTONE_X} ${Y + 2} ${Z} oak_slab`)
  await rcon.command(`setblock ${SPAWNER_X} ${Y} ${Z} spawner{SpawnData:{entity:{id:"minecraft:pig"}}}`)
  await rcon.command(`minecraft:kill @e[type=armor_stand,x=${WAYSTONE_X + 0.5},y=${Y + 0.5},z=${Z + 0.5},distance=..2]`)
  await rcon.command(`summon armor_stand ${WAYSTONE_X + 0.5} ${Y + 0.5} ${Z + 0.5} {Invisible:1b,NoGravity:1b,CustomName:"old",` +
    `BukkitValues:{"imusapi:iw.holo":"0"}}`)
  assert.match(await rcon.command(`execute if entity @e[type=armor_stand,x=${WAYSTONE_X + 0.5},y=${Y + 0.5},z=${Z + 0.5},distance=..1]`), /passed/i)
  bot.quit()
  bot = null
}

function writeOldData () {
  const player = offlineUuid(BOT)
  const waystone = crypto.randomUUID()
  sql('imusWaystones', [
    'DELETE FROM waystones; DELETE FROM waystone_owners; DELETE FROM upgrades; DELETE FROM discovered;',
    `INSERT INTO waystones (uuid, name, loc_world, loc_x, loc_y, loc_z, display_item, visibility_type) VALUES ('${waystone}', '&5Old Stone', 'world', ${WAYSTONE_X}, ${Y}, ${Z}, NULL, 'BY_TOUCH');`,
    // The old plugin wrote the owner and the discovery again on every save
    `INSERT INTO waystone_owners (uuid_player, player_name, uuid_ws) VALUES ('${player}', '${BOT}', '${waystone}'), ('${player}', '${BOT}', '${waystone}');`,
    `INSERT INTO discovered (uuid_player, uuid_ws) VALUES ('${player}', '${waystone}'), ('${player}', '${waystone}');`,
    `INSERT INTO upgrades (uuid_ws, uuid_player, upgrade_name, tier) VALUES ('${waystone}', '${player}', 'CAST_TIME', 3), ('${waystone}', '${player}', 'RENAME', 0);`
  ].join(' '))

  fs.writeFileSync(path.join(SPAWNERS_DIR, 'spawnerData.yml'), [
    `world;${SPAWNER_X};${Y};${Z}:`,
    '  location:',
    '    ==: org.bukkit.Location',
    '    world: world',
    `    x: ${SPAWNER_X}.0`,
    `    y: ${Y}.0`,
    `    z: ${Z}.0`,
    '    pitch: 0.0',
    '    yaw: 0.0',
    `  ownerUUID: ${player}`,
    '  type: PIG',
    ''
  ].join('\n'))
  fs.writeFileSync(path.join(SPAWNERS_DIR, 'playerData.yml'), [
    'playerData:',
    `  ${player}:`,
    `    name: ${BOT}`,
    '    currentChanceBonus: 26',
    '    totalMinedSpawners: 2',
    '    totalSpawnerGot: 0',
    ''
  ].join('\n'))
  fs.writeFileSync(path.join(SPAWNERS_DIR, 'settings.yml'), [
    'settings:',
    '  spawnerDropChanceSilkTouch: 20.0',
    '  spawnerRandomEntityChance: 5.0',
    '  playerBonusChanceIncrease: 13.0',
    ''
  ].join('\n'))
  // Let the settings move again, as on the first start after the update
  const config = path.join(SPAWNERS_DIR, 'config.yml')
  fs.writeFileSync(config, fs.readFileSync(config, 'utf8').replace(/^migrated-old-settings: true\r?\n/m, ''))
}

test('old waystones and spawners carry over', { skip: !run && 'set SHOPBOT_RESTART=1 to restart the server' }, async () => {
  await buildOldWorld()
  rcon.close()
  await server.stop()
  writeOldData()
  await server.start()

  rcon = await Rcon.connect()
  assert.match((await rcon.command('ia config imusspawners list')).replace(/§./g, ''), /silk-touch-chance 20\b/)
  bot = await joinServer(BOT)
  await rcon.command(`op ${BOT}`)
  await rcon.command(`gamemode survival ${BOT}`)
  await rcon.command(`clear ${BOT}`)
  await rcon.command(`tp ${BOT} ${WAYSTONE_X + 0.5} ${Y} ${Z - 2.5} 0 20`)
  await sleep(2000)

  assert.match(await rcon.command(`execute if entity @e[type=armor_stand,x=${WAYSTONE_X + 0.5},y=${Y + 0.5},z=${Z + 0.5},distance=..1]`),
    /failed/i, 'the old armor stand name is gone')
  assert.match(await rcon.command(`execute if entity @e[type=text_display,x=${WAYSTONE_X + 0.5},y=${Y + 3.2},z=${Z + 0.5},distance=..1]`),
    /passed/i, 'the new name floats there')

  // The owner had found it already, so the first click opens the menu; the upgrade from the database shows
  const menu = bot.nextWindow(5000, 'The old waystone opened no menu')
  await bot.bot.activateBlock(bot.bot.blockAt(new Vec3(WAYSTONE_X, Y + 1, Z)))
  await menu
  const upgrades = bot.nextWindow(5000, 'No upgrade menu')
  await bot.click(31)
  await upgrades
  assert.ok(bot.lore(10).some(line => /Tier 3\/9/.test(line)), bot.lore(10).join('\n'))
  bot.closeWindow()

  bot.clearMessages()
  await bot.command('spawners')
  await bot.waitForMessage(/OldStoneBot's spawner chance: 46%/)

  // The spawner in spawnerData.yml counts as placed: it comes back with no chance at all
  await rcon.command('ia config imusspawners set silk-touch-chance 0')
  await rcon.command(`minecraft:give ${BOT} netherite_pickaxe[enchantments={silk_touch:1,efficiency:5}]`)
  await rcon.command(`tp ${BOT} ${SPAWNER_X + 0.5} ${Y} ${Z - 1.5} 0 40`)
  await sleep(1000)
  bot.bot.digTime = () => 1000
  await bot.bot.equip(bot.bot.inventory.items().find(item => item.name === 'netherite_pickaxe'), 'hand')
  await bot.bot.dig(bot.bot.blockAt(new Vec3(SPAWNER_X, Y, Z)))
  await sleep(1000)
  // On the ground, or picked up already
  const mob = 'components."minecraft:block_entity_data".SpawnData.entity.id'
  assert.match(await rcon.command(`execute as @e[type=item,x=${SPAWNER_X},y=${Y},z=${Z},distance=..5] run data get entity @s Item.${mob}`) +
    await rcon.command(`data get entity ${BOT} Inventory[{id:"minecraft:spawner"}].${mob}`), /pig/, 'the old placed spawner came back')
  assert.ok(!fs.existsSync(path.join(SPAWNERS_DIR, 'spawnerData.yml')), 'the old list is gone once empty')
})
