'use strict'
// Where ImusMiniQuests panels are found: killing hostile mobs, breaking the listed blocks and loot chests the
// first time they are opened. The chances are set to 1 for the test and back after.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Vec3 } = require('vec3')
const { Rcon } = require('../lib/rcon')
const { sleep } = require('../lib/shopbot')
const f = require('../lib/enchant-fixture')

const BOT = 'DropBot'
const X = 540
const Z = 300
const Y = f.Y
const set = (setting, value) => rcon.command(`ia config imusminiquests set ${setting} ${value}`)

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await f.setUp(rcon, BOT, X, Z)
  await rcon.command(`fill ${X - 8} ${Y + 6} ${Z - 8} ${X + 8} ${Y + 40} ${Z + 8} air`)
})

after(async () => {
  await set('drops.enabled', true)
  await set('drops.mob-kill-chance', 0.002)
  await set('drops.block-break-chance', 0.0)
  await set('drops.chest-chance', 0.25)
  await rcon?.command('minecraft:kill @e[tag=ie]')
  await f.tearDown(rcon, bot)
  rcon?.close()
})

async function reset (item) {
  await rcon.command('minecraft:kill @e[tag=ie]')
  await rcon.command(`minecraft:clear ${BOT}`)
  await f.floor(rcon, X, Z)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  if (item) {
    await rcon.command(`minecraft:item replace entity ${BOT} hotbar.0 with ${item}`)
    bot.bot.setQuickBarSlot(0)
  }
  await sleep(500)
  bot.clearMessages()
}

/**
 * Quest panels lying near x + 2 or in the bot's inventory.
 */
async function panels () {
  const answer = await rcon.command(`execute as @e[type=item,x=${X + 2},y=${Y},z=${Z},distance=..6] run data get entity @s Item.components."minecraft:custom_data".PublicBukkitValues."imusminiquests:quest_id"`)
  const held = await rcon.command(`data get entity ${BOT} Inventory[{components:{"minecraft:custom_data":{PublicBukkitValues:{}}}}].components."minecraft:custom_data".PublicBukkitValues."imusminiquests:quest_id"`)
  return (answer.match(/data: "/g) ?? []).length + (held.match(/data: "/g) ?? []).length
}

async function kill (type) {
  await f.summon(rcon, type, X + 2.5, Y, Z + 0.5, 'Health:1f')
  const mob = await f.mob(bot, type)
  await sleep(700)
  await bot.bot.lookAt(mob.position.offset(0, mob.height / 2, 0), true)
  bot.bot.attack(mob)
  await sleep(1000)
}

test('a hostile mob can drop a panel, an animal can\'t', async () => {
  await set('drops.mob-kill-chance', 1)
  await reset('iron_sword')
  await kill('pig')
  assert.equal(await panels(), 0, 'a pig dropped one')
  await kill('husk')
  await bot.waitForMessage(/A quest panel dropped/)
  assert.equal(await panels(), 1)
})

test('with drops off, nothing drops', async () => {
  await set('drops.enabled', false)
  await reset('iron_sword')
  await kill('husk')
  assert.equal(await panels(), 0)
  await set('drops.enabled', true)
})

test('breaking a listed block can drop a panel', async () => {
  await set('drops.block-break-chance', 1)
  await reset('diamond_axe')
  await rcon.command(`setblock ${X + 2} ${Y} ${Z} oak_log`)
  await bot.until(() => bot.bot.blockAt(new Vec3(X + 2, Y, Z))?.name === 'oak_log', 3000, 'No log')
  await bot.bot.dig(bot.bot.blockAt(new Vec3(X + 2, Y, Z)), true)
  await bot.waitForMessage(/A quest panel dropped/)
  await sleep(500)
  assert.equal(await panels(), 1)
  await set('drops.block-break-chance', 0.0)
})

test('a loot chest can have a panel the first time it is opened', async () => {
  await set('drops.chest-chance', 1)
  await reset()
  await rcon.command(`setblock ${X + 2} ${Y} ${Z} chest{LootTable:"minecraft:chests/simple_dungeon"}`)
  await bot.until(() => bot.bot.blockAt(new Vec3(X + 2, Y, Z))?.name === 'chest', 3000, 'No chest')
  const opened = bot.nextWindow(5000, 'The chest did not open')
  await bot.bot.activateBlock(bot.bot.blockAt(new Vec3(X + 2, Y, Z)))
  const window = await opened
  const names = window.slots.slice(0, 27).filter(Boolean).map(item => JSON.stringify(item.customName ?? '').replace(/§./g, ''))
  bot.closeWindow()
  assert.ok(names.some(name => /MiniQuest/.test(name)), `no panel among ${names.length} items`)
})
