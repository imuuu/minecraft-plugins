'use strict'
// ImusMiniQuests objectives, done for real by a survival bot with a test quest panel (lib/miniquest-fixture.js):
// breaking blocks and grown crops, killing mobs, fishing, crafting, smelting, enchanting, breeding, trading,
// eating, taming, shearing and walking. Also what doesn't count: wrong targets, unripe crops, blocks the
// player placed, creative mode; and the FIRST and ALL progress modes.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Vec3 } = require('vec3')
const { Rcon } = require('../lib/rcon')
const { sleep } = require('../lib/shopbot')
const f = require('../lib/enchant-fixture')
const q = require('../lib/miniquest-fixture')

const BOT = 'QuestBot'
const X = 500
const Z = 300
const Y = f.Y
// Zombies wear a helmet so the sun doesn't burn them
const HELMET = 'equipment:{head:{id:"minecraft:leather_helmet",count:1}},drop_chances:{head:0.0f}'

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  await q.addQuests(rcon)
  bot = await f.setUp(rcon, BOT, X, Z)
})

after(async () => {
  await rcon?.command('ia config imusminiquests set progress.mode FIRST')
  await rcon?.command('minecraft:kill @e[tag=ie]')
  await q.removeQuests(rcon)
  await f.tearDown(rcon, bot)
  rcon?.close()
})

async function reset (quest) {
  await rcon.command('minecraft:kill @e[tag=ie]')
  await rcon.command(`minecraft:clear ${BOT}`)
  await rcon.command(`gamemode survival ${BOT}`)
  await f.floor(rcon, X, Z)
  // Open sky: the area is inside a hill, and crops in the dark break
  await rcon.command(`fill ${X - 8} ${Y + 6} ${Z - 8} ${X + 8} ${Y + 40} ${Z + 8} air`)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await f.refresh(rcon, BOT)
  if (quest) await q.givePanel(rcon, bot, quest)
}

/**
 * Puts the item in the second hotbar slot and selects it, the panel staying in the first.
 */
async function holdSecond (item) {
  await rcon.command(`minecraft:item replace entity ${BOT} hotbar.1 with ${item}`)
  bot.bot.setQuickBarSlot(1)
  await sleep(400)
}

async function dig (x, y, z) {
  await bot.until(() => bot.bot.blockAt(new Vec3(x, y, z))?.name !== 'air', 3000, `The bot does not see the block at ${x} ${y} ${z}`)
  await bot.bot.dig(bot.bot.blockAt(new Vec3(x, y, z)), true)
  await sleep(300)
}

async function hit (type) {
  const target = await f.mob(bot, type)
  await sleep(700)
  await bot.bot.lookAt(target.position.offset(0, target.height / 2, 0), true)
  bot.bot.attack(target)
  await sleep(500)
}

test('breaking the target blocks counts and completes the quest', async () => {
  await reset('t_break')
  await rcon.command(`fill ${X + 2} ${Y} ${Z - 1} ${X + 2} ${Y} ${Z + 1} oak_log`)
  await rcon.command(`setblock ${X + 2} ${Y + 1} ${Z} oak_planks`)
  await holdSecond('diamond_axe')
  bot.clearMessages()
  await dig(X + 2, Y + 1, Z)
  assert.equal((await q.panel(rcon, BOT, 0)).progress, 0, 'planks aren\'t a target')
  for (const z of [Z - 1, Z, Z + 1]) await dig(X + 2, Y, z)
  await q.waitForProgress(rcon, BOT, 0, 3)
  await bot.waitForMessage(/Quest complete!/)
})

test('a block the player placed doesn\'t count', async () => {
  await reset('t_break')
  await holdSecond('oak_log 1')
  await bot.until(() => bot.bot.blockAt(new Vec3(X + 2, Y - 1, Z))?.name === 'stone', 3000, 'No floor')
  await bot.bot.placeBlock(bot.bot.blockAt(new Vec3(X + 2, Y - 1, Z)), new Vec3(0, 1, 0))
  await sleep(300)
  await holdSecond('diamond_axe')
  await dig(X + 2, Y, Z)
  assert.equal((await q.panel(rcon, BOT, 0)).progress, 0)
})

test('crops count only when fully grown', async () => {
  await reset('t_crop')
  await rcon.command(`fill ${X + 2} ${Y - 1} ${Z - 1} ${X + 2} ${Y - 1} ${Z + 1} farmland`)
  await rcon.command(`setblock ${X + 2} ${Y} ${Z - 1} wheat[age=3]`)
  await rcon.command(`setblock ${X + 2} ${Y} ${Z} wheat[age=7]`)
  await sleep(500)
  await dig(X + 2, Y, Z - 1)
  assert.equal((await q.panel(rcon, BOT, 0)).progress, 0, 'unripe wheat')
  await dig(X + 2, Y, Z)
  await q.waitForProgress(rcon, BOT, 0, 1)
})

test('killing the target mobs counts, other mobs don\'t', async () => {
  await reset('t_kill')
  await holdSecond('iron_sword')
  await f.summon(rcon, 'pig', X + 2.5, Y, Z + 0.5, 'Health:1f')
  await hit('pig')
  assert.equal((await q.panel(rcon, BOT, 0)).progress, 0, 'a pig isn\'t a zombie')
  for (let i = 1; i <= 2; i++) {
    await f.summon(rcon, 'zombie', X + 2.5, Y, Z + 0.5, `Health:1f,IsBaby:0b,${HELMET}`)
    await hit('zombie')
    await q.waitForProgress(rcon, BOT, 0, i)
    // The dead one lies around for a moment
    await sleep(1500)
  }
})

test('@hostile counts any hostile mob', async () => {
  await reset('t_hostile')
  await holdSecond('iron_sword')
  await f.summon(rcon, 'husk', X + 2.5, Y, Z + 0.5, 'Health:1f')
  await hit('husk')
  await q.waitForProgress(rcon, BOT, 0, 1)
})

test('in creative nothing counts', async () => {
  await reset('t_hostile')
  await rcon.command(`gamemode creative ${BOT}`)
  await holdSecond('iron_sword')
  await f.summon(rcon, 'husk', X + 2.5, Y, Z + 0.5, 'Health:1f')
  await hit('husk')
  assert.equal((await q.panel(rcon, BOT, 0)).progress, 0)
  await rcon.command(`gamemode survival ${BOT}`)
})

test('a catch counts', async () => {
  await reset('t_fish')
  await rcon.command(`fill ${X + 2} ${Y - 4} ${Z - 3} ${X + 7} ${Y - 1} ${Z + 3} water`)
  await holdSecond('fishing_rod[enchantments={lure:3}]')
  // Auto Reel (ImusEnchants) reels the bite in
  await f.enchantHeld(rcon, BOT, [['auto_reel', 1]])
  await bot.bot.lookAt(new Vec3(X + 5, Y - 1, Z + 0.5), true)
  for (let attempt = 0; attempt < 3; attempt++) {
    await rcon.command(`minecraft:kill @e[type=fishing_bobber,x=${X},y=${Y},z=${Z},distance=..30]`)
    bot.bot.activateItem()
    await sleep(300)
    bot.bot.deactivateItem()
    const deadline = Date.now() + 40000
    while (Date.now() < deadline) {
      if ((await q.panel(rcon, BOT, 0)).progress === 1) return
      await sleep(1000)
    }
  }
  assert.fail('No catch counted')
})

test('crafting counts every item made', async () => {
  await reset('t_craft')
  await rcon.command(`minecraft:item replace entity ${BOT} hotbar.1 with oak_planks 4`)
  await sleep(500)
  const stick = bot.bot.registry.itemsByName.stick.id
  // Two crafts of 4 sticks
  for (let i = 1; i <= 2; i++) {
    const recipe = await bot.until(() => bot.bot.recipesFor(stick, null, 1, null)[0], 3000, 'No stick recipe')
    await bot.bot.craft(recipe, 1, null)
    await q.waitForProgress(rcon, BOT, 0, 4 * i)
  }
})

test('taking smelted items out of a furnace counts them', async () => {
  await reset('t_smelt')
  await rcon.command(`setblock ${X + 2} ${Y} ${Z} furnace{Items:[{Slot:2b,id:"minecraft:iron_ingot",count:5}],` +
    'RecipesUsed:{"minecraft:iron_ingot_from_smelting_raw_iron":5}}')
  await bot.until(() => bot.bot.blockAt(new Vec3(X + 2, Y, Z))?.name === 'furnace', 3000, 'No furnace')
  // A quest panel in the hand doesn't open blocks
  bot.bot.setQuickBarSlot(3)
  const furnace = await bot.bot.openFurnace(bot.bot.blockAt(new Vec3(X + 2, Y, Z)))
  await furnace.takeOutput()
  furnace.close()
  await q.waitForProgress(rcon, BOT, 0, 5)
})

test('enchanting at a table counts', async () => {
  await reset('t_enchant')
  await rcon.command(`xp set ${BOT} 30 levels`)
  await rcon.command(`minecraft:item replace entity ${BOT} hotbar.1 with iron_pickaxe`)
  await rcon.command(`minecraft:item replace entity ${BOT} hotbar.2 with lapis_lazuli 10`)
  await rcon.command(`setblock ${X + 2} ${Y} ${Z} enchanting_table`)
  await bot.until(() => bot.bot.blockAt(new Vec3(X + 2, Y, Z))?.name === 'enchanting_table', 3000, 'No table')
  bot.bot.setQuickBarSlot(3)
  await sleep(500)
  const table = await bot.bot.openEnchantmentTable(bot.bot.blockAt(new Vec3(X + 2, Y, Z)))
  // The table's own slot numbers
  await table.putTargetItem(table.findInventoryItem(bot.bot.registry.itemsByName.iron_pickaxe.id))
  await table.putLapis(table.findInventoryItem(bot.bot.registry.itemsByName.lapis_lazuli.id))
  await bot.until(() => table.enchantments[0]?.level > 0, 5000, 'The table offers nothing')
  await table.enchant(0)
  table.close()
  await q.waitForProgress(rcon, BOT, 0, 1)
})

test('breeding counts', async () => {
  await reset('t_breed')
  await holdSecond('wheat 4')
  for (const dz of [0, 1])
    await rcon.command(`summon cow ${X + 2.5} ${Y} ${Z + 0.5 + dz} {Tags:["ie"],PersistenceRequired:1b,Age:0}`)
  await sleep(800)
  for (const cow of Object.values(bot.bot.entities).filter(e => e.name === 'cow')) {
    await bot.bot.activateEntity(cow)
    await sleep(300)
  }
  await q.waitForProgress(rcon, BOT, 0, 1, 15000)
})

test('trading with a villager counts', async () => {
  await reset('t_trade')
  await holdSecond('emerald 5')
  await rcon.command(`summon villager ${X + 2.5} ${Y} ${Z + 0.5} {Tags:["ie"],NoAI:1b,PersistenceRequired:1b,` +
    'VillagerData:{profession:"minecraft:farmer",level:2,type:"minecraft:plains"},' +
    'Offers:{Recipes:[{buy:{id:"minecraft:emerald",count:1},sell:{id:"minecraft:bread",count:2},maxUses:99}]}}')
  const villager = await f.mob(bot, 'villager')
  await sleep(500)
  const window = await bot.bot.openVillager(villager)
  await bot.until(() => window.trades?.length > 0, 5000, 'The villager offers nothing')
  await bot.bot.trade(window, 0, 1)
  window.close()
  await q.waitForProgress(rcon, BOT, 0, 1)
})

test('eating counts', async () => {
  await reset('t_eat')
  await holdSecond('golden_apple 2')
  for (let i = 1; i <= 2; i++) {
    await bot.bot.consume()
    await q.waitForProgress(rcon, BOT, 0, i)
  }
})

test('taming counts', async () => {
  await reset('t_tame')
  await holdSecond('bone 30')
  await f.summon(rcon, 'wolf', X + 2.5, Y, Z + 0.5)
  const wolf = await f.mob(bot, 'wolf')
  // A bone tames a wolf one time in three
  for (let i = 0; i < 30; i++) {
    await bot.bot.activateEntity(wolf)
    await sleep(300)
    if ((await q.panel(rcon, BOT, 0)).progress === 1) return
  }
  assert.fail('The wolf was not tamed, or it did not count')
})

test('shearing counts', async () => {
  await reset('t_shear')
  await holdSecond('shears')
  await f.summon(rcon, 'sheep', X + 2.5, Y, Z + 0.5)
  await bot.bot.activateEntity(await f.mob(bot, 'sheep'))
  await q.waitForProgress(rcon, BOT, 0, 1)
})

test('walking on the target blocks counts every block', async () => {
  await reset('t_walk')
  // A lane of sand east of the bot
  // Stone under the sand, so it doesn't fall
  await rcon.command(`fill ${X} ${Y - 2} ${Z} ${X + 20} ${Y - 2} ${Z} stone`)
  await rcon.command(`fill ${X} ${Y - 1} ${Z} ${X + 20} ${Y - 1} ${Z} sand`)
  await rcon.command(`fill ${X} ${Y} ${Z - 1} ${X + 20} ${Y + 2} ${Z + 1} air`)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await sleep(500)
  await bot.bot.look(-Math.PI / 2, 0, true)
  // Walking counts only while a movement key is held, which the server learns from this packet; mineflayer
  // doesn't send it for walking
  bot.bot._client.write('player_input', { inputs: { forward: true } })
  bot.bot.setControlState('forward', true)
  try {
    await q.waitForProgress(rcon, BOT, 0, 10, 15000)
  } finally {
    bot.bot.setControlState('forward', false)
    bot.bot._client.write('player_input', { inputs: {} })
  }
})

/**
 * Two panels of t_break, one log broken: how far each one got.
 */
async function twoPanels () {
  await reset('t_break')
  await rcon.command(`imq give ${BOT} t_break 1 common`)
  await sleep(500)
  await rcon.command(`setblock ${X + 2} ${Y} ${Z} oak_log`)
  // The axe in the third slot: the second panel is in the second
  await rcon.command(`minecraft:item replace entity ${BOT} hotbar.2 with diamond_axe`)
  bot.bot.setQuickBarSlot(2)
  await sleep(400)
  await dig(X + 2, Y, Z)
  await sleep(300)
  return [(await q.panel(rcon, BOT, 0)).progress, (await q.panel(rcon, BOT, 1)).progress]
}

test('progress mode FIRST moves only the first panel, ALL every one', async () => {
  await rcon.command('ia config imusminiquests set progress.mode FIRST')
  assert.deepEqual(await twoPanels(), [1, 0])
  await rcon.command('ia config imusminiquests set progress.mode ALL')
  assert.deepEqual(await twoPanels(), [1, 1])
  await rcon.command('ia config imusminiquests set progress.mode FIRST')
})
