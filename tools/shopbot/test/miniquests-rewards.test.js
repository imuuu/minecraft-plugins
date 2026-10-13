'use strict'
// ImusMiniQuests claiming and rewards, with the test quests of lib/miniquest-fixture.js: an unfinished panel
// shows its progress, a finished one opens into exact rewards (named and enchanted item, tool tier with slots,
// custom enchant book, Slot Cores, money, a command and a message), quest points and luck, rewards locked until
// someone finds them, random rewards, a rarer panel asking for more and giving more, a time limit, panels that
// can't be placed, and the quest menu.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Vec3 } = require('vec3')
const { Rcon } = require('../lib/rcon')
const { sleep } = require('../lib/shopbot')
const { balance } = require('../lib/fixture')
const f = require('../lib/enchant-fixture')
const q = require('../lib/miniquest-fixture')

const BOT = 'RewardBot'
const X = 500
const Z = 340
const Y = f.Y
const HELMET = 'equipment:{head:{id:"minecraft:leather_helmet",count:1}},drop_chances:{head:0.0f}'
const VALUES = 'components."minecraft:custom_data".PublicBukkitValues'

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  await q.addQuests(rcon)
  bot = await f.setUp(rcon, BOT, X, Z)
  await rcon.command('imq unlocks netherite lock')
})

after(async () => {
  await rcon?.command('imq unlocks netherite lock')
  await rcon?.command(`op ${BOT}`)
  await rcon?.command('minecraft:kill @e[tag=ie]')
  await q.removeQuests(rcon)
  await f.tearDown(rcon, bot)
  rcon?.close()
})

async function reset (quest, rarity = 'common') {
  await rcon.command('minecraft:kill @e[tag=ie]')
  await rcon.command(`minecraft:clear ${BOT}`)
  await f.floor(rcon, X, Z)
  await rcon.command(`fill ${X - 8} ${Y + 6} ${Z - 8} ${X + 8} ${Y + 40} ${Z + 8} air`)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  if (quest) await q.givePanel(rcon, bot, quest, rarity)
}

/**
 * Fills the panel in the hand and right-clicks it open.
 */
async function claim () {
  bot.bot.setQuickBarSlot(0)
  await bot.command('imq complete')
  bot.clearMessages()
  bot.bot.activateItem()
  await bot.waitForMessage(/You opened/)
  await sleep(500)
}

async function data (path) {
  return (await rcon.command(`data get entity ${BOT} ${path}`)).replace(/§./g, '')
}

async function questPoints () {
  const match = /data: (\d+)/.exec(await data(`BukkitValues."imusminiquests:quest_points"`))
  return match ? Number(match[1]) : 0
}

test('right-clicking an unfinished panel tells its progress', async () => {
  await reset('t_break')
  bot.clearMessages()
  bot.bot.activateItem()
  await bot.waitForMessage(/Test t_break: 0\/3/)
  assert.ok(await q.panel(rcon, BOT, 0), 'the panel stays')
})

test('a finished panel opens into its exact rewards', async () => {
  await reset('t_rewards')
  const money = await balance(rcon, BOT)
  await claim()
  assert.equal(await q.panel(rcon, BOT, 0), null, 'the panel is used up')
  await bot.waitForMessage(/Test reward message/)

  // Asked a piece at a time: RCON cuts long answers
  const sword = 'Inventory[{id:"minecraft:iron_sword"}].components'
  assert.match(await data(`${sword}."minecraft:custom_name".extra[0].text`), /Test Blade/, 'the sword has its name')
  assert.match(await data(`${sword}."minecraft:enchantments"`), /sharpness": 4/, 'and Sharpness IV')
  assert.match(await data(`Inventory[{id:"minecraft:diamond_pickaxe"}].${VALUES}`), /ie_pd_slots/, 'a diamond pickaxe with slots')
  assert.match(await data(`Inventory[{id:"minecraft:enchanted_book"}].${VALUES}."imusapi:ie_custom_book"`), /timber:2/, 'a Timber II book')
  assert.equal(await f.count(rcon, BOT, 'amethyst_shard'), 2, 'two Slot Cores')
  assert.equal(await f.count(rcon, BOT, 'apple'), 3, 'the command gave 3 apples')
  assert.equal(await balance(rcon, BOT) - money, 500, '500 money')
})

test('opening a quest gives a quest point, and points give luck', async () => {
  await reset('t_break')
  await bot.command(`imq points ${BOT} set 7`)
  await claim()
  assert.equal(await questPoints(), 8)
  await bot.waitForMessage(/Quest points: 8 \(luck 8%\)/)
  // Luck stops at 50%
  bot.clearMessages()
  await bot.command(`imq points ${BOT} set 80`)
  await bot.waitForMessage(/quest points: 80 \(luck 50%, most 50%\)/)
})

test('rewards stay locked until someone finds them; admins and ops never unlock them', async () => {
  // Locked: a pool of netherite and diamonds only gives diamonds
  await reset('t_locked')
  await claim()
  assert.equal(await f.count(rcon, BOT, 'netherite_ingot'), 0)
  assert.equal(await f.count(rcon, BOT, 'diamond'), 12)

  // An op with ancient debris doesn't unlock it
  await rcon.command(`minecraft:give ${BOT} ancient_debris`)
  await sleep(6000)
  assert.match((await rcon.command('imq unlocks')).replace(/§./g, ''), /netherite \(Netherite\): locked/)

  // A player does, and then the netherite shows up
  await rcon.command(`deop ${BOT}`)
  try {
    const deadline = Date.now() + 8000
    while (!/netherite \(Netherite\): unlocked by RewardBot/.test((await rcon.command('imq unlocks')).replace(/§./g, ''))) {
      if (Date.now() > deadline) throw new Error('Finding ancient debris did not unlock netherite')
      await sleep(500)
    }
  } finally {
    await rcon.command(`op ${BOT}`)
  }
  await reset('t_locked')
  await claim()
  // 12 rolls of two: all diamonds would be 1 in 4096
  assert.ok(await f.count(rcon, BOT, 'netherite_ingot') > 0, 'netherite comes once unlocked')
  await rcon.command('imq unlocks netherite lock')
})

test('random rewards come on top, each with a message', async () => {
  await reset('t_random')
  await claim()
  await sleep(500)
  assert.equal(bot.messages.filter(m => /Random reward/.test(m)).length, 3)
})

/**
 * How much the panel in the hand asks for, from what right-clicking it says.
 */
async function panelAmount () {
  bot.clearMessages()
  bot.bot.activateItem()
  const message = await bot.waitForMessage(/Test t_rare: \d+\/\d+/)
  return Number(/\/(\d+)/.exec(message)[1])
}

test('a legendary panel asks 2.5 times as much and gives two more random rewards', async () => {
  await reset('t_rare', 'common')
  assert.equal(await panelAmount(), 4)
  await reset('t_rare', 'legendary')
  assert.equal(await panelAmount(), 10)
  await claim()
  await sleep(500)
  assert.equal(bot.messages.filter(m => /Random reward/.test(m)).length, 2)
  assert.equal(await f.count(rcon, BOT, 'diamond'), 1)
})

test('a timed quest starts over when its time runs out', async () => {
  await reset('t_timed')
  await rcon.command(`minecraft:item replace entity ${BOT} hotbar.1 with iron_sword`)
  bot.bot.setQuickBarSlot(1)
  await sleep(400)
  const kill = async () => {
    await f.summon(rcon, 'zombie', X + 2.5, Y, Z + 0.5, `Health:1f,IsBaby:0b,${HELMET}`)
    const zombie = await f.mob(bot, 'zombie')
    await sleep(700)
    await bot.bot.lookAt(zombie.position.offset(0, 1, 0), true)
    bot.bot.attack(zombie)
    await sleep(1500)
  }
  await kill()
  await q.waitForProgress(rcon, BOT, 0, 1)
  // The limit is 3 seconds
  await sleep(3500)
  bot.clearMessages()
  await kill()
  await bot.waitForMessage(/ran out of time/)
  assert.equal((await q.panel(rcon, BOT, 0)).progress, 1, 'the count started over')
})

test('a quest panel can\'t be placed', async () => {
  await reset('t_break')
  await bot.until(() => bot.bot.blockAt(new Vec3(X + 2, Y - 1, Z))?.name === 'stone', 3000, 'No floor')
  await bot.bot.placeBlock(bot.bot.blockAt(new Vec3(X + 2, Y - 1, Z)), new Vec3(0, 1, 0)).catch(() => {})
  await sleep(500)
  assert.ok(await f.isBlock(rcon, X + 2, Y, Z, 'air'), 'nothing was placed')
  assert.ok(await q.panel(rcon, BOT, 0), 'the panel is still there')
})

test('the quest menu hands out panels', async () => {
  await reset()
  const menu = bot.nextWindow(5000, '/imq menu did not open')
  await bot.command('imq menu')
  await menu
  await bot.click(0)
  bot.closeWindow()
  await sleep(500)
  assert.ok(await q.panel(rcon, BOT, 0), 'a panel was given')
})
