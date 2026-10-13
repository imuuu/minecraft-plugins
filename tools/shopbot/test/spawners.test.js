'use strict'
// ImusSpawners: a natural spawner drops with silk touch on a lucky roll, a miss raises the player's chance,
// a placed spawner keeps its mob and always comes back, and admins give spawners.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Vec3 } = require('vec3')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer } = require('../lib/fixture')

const BOT = 'SpawnBot'
const X = 320
const Y = 100
const Z = 320

let rcon
let bot

const set = (setting, value) => rcon.command(`ia config imusspawners set ${setting} ${value}`)

async function naturalSpawner (mob) {
  await rcon.command(`fill ${X - 3} ${Y - 1} ${Z - 3} ${X + 3} ${Y - 1} ${Z + 3} stone`)
  await rcon.command(`fill ${X - 3} ${Y} ${Z - 3} ${X + 3} ${Y + 3} ${Z + 3} air`)
  await rcon.command(`setblock ${X} ${Y} ${Z} spawner{SpawnData:{entity:{id:"minecraft:${mob}"}}}`)
}

/**
 * Breaks the block at the spawner's spot with the silk touch pickaxe.
 */
async function mine () {
  // Drops left from an earlier run would count too
  await rcon.command(`minecraft:kill @e[type=item,x=${X},y=${Y},z=${Z},distance=..8]`)
  await rcon.command(`minecraft:clear ${BOT} spawner`)
  await rcon.command(`tp ${BOT} ${X + 0.5} ${Y} ${Z - 1.5} 0 40`)
  await sleep(1000)
  const pickaxe = bot.bot.inventory.items().find(item => item.name === 'netherite_pickaxe')
  await bot.bot.equip(pickaxe, 'hand')
  await bot.until(() => bot.bot.blockAt(new Vec3(X, Y, Z))?.name === 'spawner', 3000, 'The bot does not see the spawner')
  await bot.bot.dig(bot.bot.blockAt(new Vec3(X, Y, Z)))
  await sleep(500)
}

/**
 * The mob of the spawner the bot got from the block, or null: the item lies on the ground or the bot already
 * picked it up. Checked on the server, as the bot doesn't always see its pickups at once.
 */
async function droppedSpawner () {
  const path = 'components."minecraft:block_entity_data".SpawnData.entity.id'
  const onGround = await rcon.command(`execute as @e[type=item,x=${X},y=${Y},z=${Z},distance=..5] run data get entity @s Item.${path}`)
  const picked = await rcon.command(`data get entity ${BOT} Inventory[{id:"minecraft:spawner"}].${path}`)
  const match = /"minecraft:([a-z_]+)"/.exec(onGround + picked)
  return match ? match[1] : null
}

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  // Mineflayer can't read the enchantments of 26.x items to time the digging; a second is plenty for a
  // netherite pickaxe on a spawner
  bot.bot.digTime = () => 1000
  await resetPlayer(rcon, BOT)
  await rcon.command(`minecraft:give ${BOT} netherite_pickaxe[enchantments={silk_touch:1,efficiency:5}]`)
  await set('random-type-chance', 0)
  await set('chance-increase-per-miss', 13)
  await set('max-chance', 100)
})

after(async () => {
  await set('silk-touch-chance', 14)
  await set('random-type-chance', 5)
  bot?.quit()
  rcon?.close()
})

test('a lucky roll drops a natural spawner as its own mob', async () => {
  await set('silk-touch-chance', 100)
  await naturalSpawner('zombie')
  bot.clearMessages()
  await mine()
  await bot.waitForMessage(/the spawner dropped/)
  assert.equal(await droppedSpawner(), 'zombie')
})

test('a placed spawner keeps its mob and always comes back', async () => {
  await set('silk-touch-chance', 0)
  await rcon.command(`setblock ${X} ${Y} ${Z} air`)
  await rcon.command(`spawners give zombie ${BOT} 1`)
  await bot.until(() => bot.count('spawner') === 1, 3000, 'No spawner given')
  await rcon.command(`tp ${BOT} ${X + 0.5} ${Y} ${Z - 1.5} 0 40`)
  await sleep(1000)
  await bot.bot.equip(bot.bot.inventory.items().find(item => item.name === 'spawner'), 'hand')
  await bot.until(() => bot.bot.blockAt(new Vec3(X, Y - 1, Z))?.name === 'stone', 3000, 'The bot does not see the floor')
  await bot.bot.placeBlock(bot.bot.blockAt(new Vec3(X, Y - 1, Z)), new Vec3(0, 1, 0))
  await sleep(500)
  assert.match(await rcon.command(`data get block ${X} ${Y} ${Z} SpawnData.entity.id`), /zombie/)

  // With no chance at all, a placed one still comes back
  bot.clearMessages()
  await mine()
  assert.equal(await droppedSpawner(), 'zombie', 'the placed spawner came back')
  assert.ok(!bot.messages.some(m => /rolled/.test(m)), 'no roll for a placed spawner')
})

test('a miss raises the player\'s chance, and the chance shows with /spawners', async () => {
  // The first test's hit set this player's bonus back to 0
  await set('silk-touch-chance', 0)
  await naturalSpawner('skeleton')
  bot.clearMessages()
  await mine()
  await bot.waitForMessage(/your chance was 0%\. Next time it is 13%/)
  bot.clearMessages()
  await bot.command('spawners')
  await bot.waitForMessage(/SpawnBot's spawner chance: 13%/)
})

test('admins give spawners of any mob', async () => {
  await rcon.command(`clear ${BOT}`)
  await sleep(300)
  await rcon.command(`spawners give cave_spider ${BOT} 2`)
  await bot.until(() => bot.count('spawner') === 2, 3000, 'No spawners given')
})
