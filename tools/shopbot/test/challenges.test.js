'use strict'
// ImusChallenges with two survival bots that aren't ops (ops don't take part): the point commands, the first
// player on the server to pick up a material and to complete an advancement, advancement points by frame,
// creative players not taking part,
// the leader announcement, the scoreboard, and the challenge shop (buying an item with points, not enough
// points, buying a slot with money). Firsts stay in the database, so each run picks new ones
// (lib/challenge-fixture.js).
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { sleep, joinServer } = require('../lib/shopbot')
const { resetPlayer, balance } = require('../lib/fixture')
const f = require('../lib/enchant-fixture')
const c = require('../lib/challenge-fixture')

const BOT = 'ChalBot'
const RIVAL = 'ChalRival'
const X = 560
const Z = 300
const Y = f.Y

// A shop buyer new every run: what a player bought stays bought until the shop refreshes
const BUYER = 'Shop' + Date.now().toString(36).slice(-8)

// Advancements by frame, with their titles; each run uses ones nobody has been first to
const ADVANCEMENTS = {
  task: [['story/mine_stone', 'Stone Age'], ['story/smelt_iron', 'Acquire Hardware'], ['story/obtain_armor', 'Suit Up'],
    ['story/lava_bucket', 'Hot Stuff'], ['story/form_obsidian', 'Ice Bucket Challenge'], ['story/mine_diamond', 'Diamonds!'],
    ['husbandry/plant_seed', 'A Seedy Place'], ['husbandry/fishy_business', 'Fishy Business'], ['adventure/trade', 'What a Deal!'],
    ['adventure/sleep_in_bed', 'Sweet Dreams'], ['adventure/shoot_arrow', 'Take Aim'], ['story/enchant_item', 'Enchanter']],
  goal: [['story/cure_zombie_villager', 'Zombie Doctor'], ['adventure/totem_of_undying', 'Postmortal'],
    ['adventure/summon_iron_golem', 'Hired Help']],
  challenge: [['adventure/kill_all_mobs', 'Monsters Hunted'], ['adventure/sniper_duel', 'Sniper Duel'],
    ['adventure/two_birds_one_arrow', 'Two Birds, One Arrow'], ['nether/return_to_sender', 'Return to Sender'],
    ['adventure/arbalistic', 'Arbalistic']]
}
const FRAME_POINTS = { task: 3, goal: 5, challenge: 10 }

function fresh (frame) {
  const found = ADVANCEMENTS[frame].find(([, title]) => !c.advancementTaken(title))
  if (!found) throw new Error(`Every ${frame} advancement in the list has a first already; add more`)
  return found
}

let rcon
let bot
let rival

before(async () => {
  rcon = await Rcon.connect()
  bot = await f.setUp(rcon, BOT, X, Z)
  rival = await joinServer(RIVAL)
  await resetPlayer(rcon, RIVAL)
  await rcon.command(`fill ${X - 8} ${Y + 6} ${Z - 8} ${X + 8} ${Y + 40} ${Z + 8} air`)
  for (const name of [BOT, RIVAL]) {
    await rcon.command(`minecraft:tp ${name} ${X + 0.5} ${Y} ${Z + 0.5}`)
    await rcon.command(`ic reset points ${name}`)
  }
  for (const name of [BOT, RIVAL])
    await rcon.command(`deop ${name}`)
  await rcon.command('ia config imuschallenges set leader-broadcast.cooldown-minutes 0')
  await sleep(1000)
})

after(async () => {
  await rcon?.command('ia config imuschallenges set leader-broadcast.cooldown-minutes 5')
  await rcon?.command('ia config imuschallenges set points.first-advancement-everyone false')
  for (const name of [BOT, RIVAL])
    await rcon?.command(`op ${name}`)
  for (const name of [BOT, RIVAL, BUYER])
    await rcon?.command(`ic reset points ${name}`)
  rival?.quit()
  await f.tearDown(rcon, bot)
  rcon?.close()
})

/**
 * Waits until the database has the player's points.
 */
async function waitForPoints (name, expected, timeoutMs = 8000) {
  const deadline = Date.now() + timeoutMs
  let now
  for (;;) {
    now = c.points(name)
    if (now.points === expected.points && now.lifetime === expected.lifetime) return now
    if (Date.now() > deadline) throw new Error(`${name} has ${JSON.stringify(now)}, not ${JSON.stringify(expected)}`)
    await sleep(300)
  }
}

/**
 * Drops the material in front of the player and has them walk over it.
 */
async function pickUp (player, material) {
  await rcon.command(`minecraft:tp ${player.name} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await sleep(500)
  await rcon.command(`summon item ${X + 2.5} ${Y} ${Z + 0.5} {Item:{id:"minecraft:${material.toLowerCase()}",count:1},PickupDelay:0}`)
  await sleep(300)
  await player.bot.look(-Math.PI / 2, 0, true)
  player.bot.setControlState('forward', true)
  await sleep(700)
  player.bot.setControlState('forward', false)
  await player.until(() => player.bot.inventory.items().some(item => item.name === material.toLowerCase()), 4000,
    `${player.name} did not pick up ${material}`)
  // Out of the way, so the next drop is for whoever it is meant for
  await rcon.command(`minecraft:tp ${player.name} ${X - 4.5} ${Y} ${Z + 0.5}`)
}

test('admins add, set and reset challenge points, also from the console', async () => {
  await rcon.command(`ic add points CHALLENGE_POINT 5 ${BOT}`)
  await waitForPoints(BOT, { points: 5, lifetime: 5 })
  await rcon.command(`ic set points 2 ${BOT}`)
  await waitForPoints(BOT, { points: 2, lifetime: 5 })
  await rcon.command(`ic set lifetime 9 ${BOT}`)
  await waitForPoints(BOT, { points: 2, lifetime: 9 })
  assert.match(await rcon.command(`scoreboard players get ${BOT} ic_points`), /has 9 /, 'the scoreboard shows the lifetime points')
  await rcon.command(`ic reset points ${BOT}`)
  await waitForPoints(BOT, { points: 0, lifetime: 0 })
})

test('the first player to pick up a material gets a point, the next one doesn\'t', async () => {
  const [material] = c.uncollected(1)
  bot.clearMessages()
  rival.clearMessages()
  await pickUp(bot, material)
  await waitForPoints(BOT, { points: 1, lifetime: 1 })
  await rival.waitForMessage(new RegExp(`ChalBot has Researched ${material}`))
  // The database gets it within a few seconds
  await sleep(6000)
  assert.ok(c.collected().has(material), 'saved as collected')

  await pickUp(rival, material)
  await sleep(1500)
  assert.deepEqual(c.points(RIVAL), { points: 0, lifetime: 0 })
})

test('a player in creative doesn\'t collect firsts', async () => {
  const [material] = c.uncollected(1)
  await rcon.command(`gamemode creative ${RIVAL}`)
  try {
    await pickUp(rival, material)
    await sleep(1500)
    assert.deepEqual(c.points(RIVAL), { points: 0, lifetime: 0 })
  } finally {
    await rcon.command(`gamemode survival ${RIVAL}`)
  }
  // It is still free for a survival player
  await rcon.command(`minecraft:clear ${BOT}`)
  await pickUp(bot, material)
  await waitForPoints(BOT, { points: 2, lifetime: 2 })
})

test('by default nobody gets the first advancement bonus', async () => {
  await rcon.command('ia config imuschallenges set points.first-advancement-everyone false')
  const [key, title] = fresh('task')
  await rcon.command(`advancement revoke ${BOT} only minecraft:${key}`)
  const before = c.points(BOT)
  await rcon.command(`advancement grant ${BOT} only minecraft:${key}`)
  await waitForPoints(BOT, { points: before.points + 3, lifetime: before.lifetime + 3 })
  await sleep(6000)
  assert.equal(c.advancementTaken(title), false)
  await rcon.command(`advancement revoke ${BOT} only minecraft:${key}`)
})

test('advancements give 3/5/10 points by frame, and with the bonus on the first player 10 more', async () => {
  await rcon.command('ia config imuschallenges set points.first-advancement-everyone true')
  let expected = c.points(BOT)
  for (const frame of ['task', 'goal', 'challenge']) {
    const [key, title] = fresh(frame)
    await rcon.command(`advancement revoke ${BOT} only minecraft:${key}`)
    await rcon.command(`advancement revoke ${RIVAL} only minecraft:${key}`)
    rival.clearMessages()
    await rcon.command(`advancement grant ${BOT} only minecraft:${key}`)
    const gain = FRAME_POINTS[frame] + 10
    expected = { points: expected.points + gain, lifetime: expected.lifetime + gain }
    await waitForPoints(BOT, expected)
    await rival.waitForMessage(new RegExp(`ChalBot is FIRST to complete the \\[${title.replace(/[.!?]/g, '\\$&')}\\]`), 8000)
    // The first is saved within a few seconds; after that nobody gets the bonus
    await sleep(6000)
    const before = c.points(RIVAL)
    await rcon.command(`advancement grant ${RIVAL} only minecraft:${key}`)
    await waitForPoints(RIVAL, { points: before.points + FRAME_POINTS[frame], lifetime: before.lifetime + FRAME_POINTS[frame] })
  }
})

test('a player in creative gets the frame points but not the first bonus', async () => {
  await rcon.command('ia config imuschallenges set points.first-advancement-everyone true')
  const [key, title] = fresh('task')
  await rcon.command(`advancement revoke ${RIVAL} only minecraft:${key}`)
  await rcon.command(`gamemode creative ${RIVAL}`)
  try {
    const before = c.points(RIVAL)
    await rcon.command(`advancement grant ${RIVAL} only minecraft:${key}`)
    await waitForPoints(RIVAL, { points: before.points + 3, lifetime: before.lifetime + 3 })
    await sleep(6000)
    assert.equal(c.advancementTaken(title), false, 'the first is still free')
  } finally {
    await rcon.command(`gamemode survival ${RIVAL}`)
    await rcon.command(`advancement revoke ${RIVAL} only minecraft:${key}`)
  }
})

test('taking the lead is announced', async () => {
  const lead = c.topLifetime() + 5
  const now = c.points(BOT)
  bot.clearMessages()
  rival.clearMessages()
  await rcon.command(`ic add points CHALLENGE_POINT ${lead - now.lifetime} ${BOT}`)
  await rival.waitForMessage(new RegExp(`ChalBot has taken the lead with ${lead} challenge points`), 8000)
  await rcon.command(`ic reset points ${BOT}`)
})

test('the challenge shop sells for points and slots for money', async () => {
  const buyer = await joinServer(BUYER)
  try {
    await resetPlayer(rcon, BUYER, 50000)
    await rcon.command(`minecraft:tp ${BUYER} ${X + 0.5} ${Y} ${Z + 0.5}`)
    await rcon.command(`ic set points 0 ${BUYER}`)
    await sleep(1000)

    const openShop = async () => {
      const shop = buyer.nextWindow(5000, '/ic shop did not open')
      await buyer.command('ic shop')
      await shop
      // The slots come from the database a moment later
      await buyer.until(() => buyer.slot(21)?.name && !/stained_glass_pane/.test(buyer.slot(21).name), 5000, 'No item in the first normal slot')
    }
    const cost = () => Number(/Cost: (\d+)/.exec(buyer.lore(21).join(' '))[1])

    // Without points nothing is bought
    await openShop()
    buyer.clearMessages()
    await buyer.click(21)
    await buyer.waitForMessage(/You don't have enough points!/)

    // With them the item comes and the points go
    await rcon.command(`ic set points 100 ${BUYER}`)
    await sleep(1000)
    await openShop()
    const price = cost()
    const item = buyer.slot(21).name
    const lifetime = c.points(BUYER).lifetime
    buyer.clearMessages()
    await buyer.click(21)
    await buyer.waitForMessage(new RegExp(`You bought an item\\(s\\) with ${price} challenge points`))
    // Spending doesn't take lifetime points
    await waitForPoints(BUYER, { points: 100 - price, lifetime })
    await buyer.until(() => buyer.bot.inventory.items().some(i => i.name === item), 4000, `No ${item} in the inventory`)

    // One item per refresh: the normal side is closed now
    const shop = buyer.nextWindow(5000, '/ic shop did not open')
    await buyer.command('ic shop')
    await shop
    await buyer.until(() => buyer.slot(21)?.name === 'red_stained_glass_pane', 5000, 'The normal side is still open')
    buyer.closeWindow()

    // A slot of the special side costs money: click, then right-click to confirm. Its first slot is the
    // default one, the next is for sale.
    const special = buyer.nextWindow(5000, '/ic shop did not open')
    await buyer.command('ic shop')
    await special
    await buyer.until(() => /Buy/.test(JSON.stringify(buyer.slot(19)?.customName ?? '')), 5000, 'No slot for sale on the special side')
    const slotPrice = Number(/Cost: (\d+)/.exec(buyer.lore(19).join(' '))[1])
    buyer.clearMessages()
    await buyer.click(19)
    await buyer.click(19, 'right')
    await buyer.waitForMessage(/You don't have enough money!/)
    // With the money it goes
    await rcon.command(`eco set ${BUYER} ${slotPrice + 1000}`)
    await sleep(300)
    const before = await balance(rcon, BUYER)
    await buyer.click(19)
    await buyer.click(19, 'right')
    await buyer.waitForMessage(/You bought a slot!/)
    assert.equal(before - await balance(rcon, BUYER), slotPrice)
  } finally {
    buyer.quit()
  }
})

test('the collection view opens', async () => {
  await rcon.command(`op ${BOT}`)
  const view = bot.nextWindow(5000, '/ic view materials did not open')
  await bot.command('ic view materials')
  await view
  bot.closeWindow()
  await rcon.command(`deop ${BOT}`)
})
