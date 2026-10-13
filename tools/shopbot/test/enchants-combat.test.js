'use strict'
// ImusEnchants melee enchants on a survival bot hitting still mobs: Venom, Withering, Frost, Lifesteal,
// Execute, Cleave, Beheading, Scavenger, and Telekinesis with Wisdom on a sword. Chance enchants always
// trigger (testing.always-trigger).
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { sleep } = require('../lib/shopbot')
const f = require('../lib/enchant-fixture')

const BOT = 'CombatBot'
const X = 420
const Z = 300
const Y = f.Y
const MOB = '@e[tag=ie,limit=1,sort=nearest]'
// Zombies wear a helmet so the sun doesn't burn them
const HELMET = 'equipment:{head:{id:"minecraft:leather_helmet",count:1}},drop_chances:{head:0.0f}'

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await f.setUp(rcon, BOT, X, Z)
  await rcon.command('gamerule natural_health_regeneration false')
})

after(async () => {
  await rcon?.command('minecraft:kill @e[tag=ie]')
  await f.tearDown(rcon, bot)
  rcon?.close()
})

async function reset () {
  await rcon.command('minecraft:kill @e[tag=ie]')
  await rcon.command(`minecraft:clear ${BOT}`)
  await f.floor(rcon, X, Z)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await f.refresh(rcon, BOT)
}

/**
 * Hits the closest mob of the type with a fully charged swing.
 */
async function hit (type) {
  const target = await f.mob(bot, type)
  // A full swing: an axe takes over a second to charge
  await sleep(1800)
  await bot.bot.lookAt(target.position.offset(0, target.height / 2, 0), true)
  bot.bot.attack(target)
  await sleep(500)
}

/**
 * Has a still husk two blocks in front of the bot, takes a hit and gives back the effects it got.
 */
async function hitMob (weapon, enchants, type = 'husk') {
  await reset()
  await f.hold(rcon, bot, weapon, enchants)
  await f.summon(rcon, type, X + 2.5, Y, Z + 0.5)
  await hit(type)
  return await f.effects(rcon, MOB)
}

test('Venom poisons the target', async () => {
  // Poison doesn't work on undead, so a pig
  assert.ok((await hitMob('iron_sword', [['venom', 3]], 'pig')).includes('minecraft:poison'))
})

test('Withering withers the target', async () => {
  assert.ok((await hitMob('iron_sword', [['withering', 2]])).includes('minecraft:wither'))
})

test('Frost slows the target', async () => {
  assert.ok((await hitMob('iron_sword', [['frost', 2]])).includes('minecraft:slowness'))
})

test('a sword without enchants does nothing extra', async () => {
  assert.deepEqual(await hitMob('iron_sword', []), [])
})

test('Lifesteal heals the attacker', async () => {
  await reset()
  await f.hold(rcon, bot, 'iron_sword', [['lifesteal', 3]])
  await rcon.command(`minecraft:damage ${BOT} 10 minecraft:generic`)
  await sleep(300)
  const hurt = await f.health(rcon, BOT)
  await f.summon(rcon, 'husk', X + 2.5, Y, Z + 0.5)
  await hit('husk')
  assert.ok(await f.health(rcon, BOT) > hurt, 'the bot got health back')
})

/**
 * The damage one hit does to a husk at a quarter of its health (100 max, 25 now).
 */
async function hitWeakHusk (enchants) {
  await reset()
  await f.hold(rcon, bot, 'iron_sword', enchants)
  await f.summon(rcon, 'husk', X + 2.5, Y, Z + 0.5)
  await f.setMobHealth(rcon, MOB, 100, 25)
  await hit('husk')
  return 25 - await f.health(rcon, MOB)
}

test('Execute hits harder below 30% health', async () => {
  const plain = await hitWeakHusk([])
  const executed = await hitWeakHusk([['execute', 3]])
  assert.ok(plain > 0, 'the plain hit landed')
  assert.ok(executed >= plain * 1.25, `${executed} with Execute III, ${plain} without`)
})

test('Cleave on an axe also hurts the mob next to the target', async () => {
  await reset()
  await f.hold(rcon, bot, 'iron_axe', [['cleave', 3]])
  await f.summon(rcon, 'husk', X + 2.5, Y, Z + 0.5)
  await f.summon(rcon, 'husk', X + 2.5, Y, Z + 1.7, 'Tags:["ie","beside"]')
  await hit('husk')
  const beside = await f.health(rcon, '@e[tag=beside,limit=1]')
  assert.ok(beside < 20, `the husk beside took ${20 - beside}`)
})

/**
 * Kills a one health mob of the type with the held weapon.
 */
async function kill (type, extra = '') {
  await f.summon(rcon, type, X + 2.5, Y, Z + 0.5, 'Health:1f' + (extra ? ',' + extra : ''))
  await hit(type)
  await sleep(500)
}

test('Beheading drops the head', async () => {
  await reset()
  await f.hold(rcon, bot, 'iron_sword', [['beheading', 3]])
  await kill('zombie', `IsBaby:0b,${HELMET}`)
  assert.equal(await f.itemsOnGround(rcon, 'zombie_head', X + 2, Y, Z), 1)
})

test('Scavenger doubles a drop', async () => {
  await reset()
  // A cod always drops one cod
  await f.hold(rcon, bot, 'iron_sword', [['scavenger', 2]])
  await kill('cod')
  // The bot may pick one up already
  assert.equal(await f.itemsOnGround(rcon, 'cod', X + 2, Y, Z) + await f.count(rcon, BOT, 'cod'), 2)
})

test('Telekinesis puts the drops and Wisdom more XP straight to the player', async () => {
  await reset()
  await f.hold(rcon, bot, 'iron_sword', [['telekinesis', 1], ['wisdom', 3]])
  await kill('cod')
  assert.equal(await f.count(rcon, BOT, 'cod'), 1, 'the cod is in the inventory')
  assert.equal(await f.itemsOnGround(rcon, 'cod', X + 2, Y, Z), 0, 'nothing on the ground')
  const before = await f.xpPoints(rcon, BOT)
  await kill('zombie', `IsBaby:0b,${HELMET}`)
  // A zombie gives 5 XP; Wisdom III makes it 9
  const got = await f.xpPoints(rcon, BOT) - before
  assert.ok(got >= 9, `got ${got} XP`)
})

test('an enchant only goes on the items it is for', async () => {
  await reset()
  await rcon.command(`minecraft:item replace entity ${BOT} hotbar.0 with iron_pickaxe`)
  bot.bot.setQuickBarSlot(0)
  await sleep(300)
  assert.match((await rcon.command(`ien enchant venom 1 ${BOT}`)).replace(/§./g, ''), /doesn't go on iron_pickaxe/)
})
