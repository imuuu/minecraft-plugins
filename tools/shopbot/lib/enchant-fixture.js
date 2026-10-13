'use strict'
// Setup for the ImusEnchants tests: a survival bot on a floor of its own, items with custom enchants put
// straight in its hands or on its body, and server-side checks of health, effects, items and blocks.
// Custom enchants only work in survival, so the bots play in survival.
const { joinServer, sleep } = require('./shopbot')
const { resetPlayer } = require('./fixture')

const Y = 100

/**
 * Joins the bot and gives it a stone floor around x, z at y 99 with air above.
 */
async function setUp (rcon, name, x, z, radius = 8) {
  const bot = await joinServer(name)
  // Mineflayer can't read the enchantments of 26.x items to time digging
  bot.bot.digTime = () => 600
  await resetPlayer(rcon, name)
  await rcon.command('ia config imusenchants set testing.always-trigger true')
  await rcon.command(`minecraft:tp ${name} ${x + 0.5} ${Y} ${z + 0.5}`)
  await sleep(1500)
  await floor(rcon, x, z, radius)
  await rcon.command(`minecraft:tp ${name} ${x + 0.5} ${Y} ${z + 0.5}`)
  await sleep(800)
  return bot
}

async function floor (rcon, x, z, radius = 8) {
  await rcon.command(`fill ${x - radius} ${Y - 1} ${z - radius} ${x + radius} ${Y - 1} ${z + radius} stone`)
  await rcon.command(`fill ${x - radius} ${Y} ${z - radius} ${x + radius} ${Y + 5} ${z + radius} air`)
  await rcon.command(`minecraft:kill @e[type=!player,x=${x},y=${Y},z=${z},distance=..${radius + 6}]`)
}

async function tearDown (rcon, bot) {
  await rcon?.command('ia config imusenchants set testing.always-trigger false')
  await rcon?.command('gamerule natural_health_regeneration true')
  if (bot) {
    await rcon?.command(`minecraft:effect clear ${bot.name}`)
    await rcon?.command(`minecraft:clear ${bot.name}`)
    bot.quit()
  }
}

/**
 * Puts the custom enchants on the item in the bot's main hand: [['venom', 3], ...].
 */
async function enchantHeld (rcon, name, enchants) {
  for (const [key, level] of enchants) {
    const answer = await rcon.command(`ien enchant ${key} ${level} ${name}`)
    if (!/Put /.test(answer.replace(/§./g, ''))) throw new Error(`Couldn't put ${key} ${level} on ${name}'s item: ${answer}`)
  }
}

/**
 * The item in the first hotbar slot, selected, with the custom enchants.
 */
async function hold (rcon, bot, item, enchants = []) {
  await rcon.command(`minecraft:item replace entity ${bot.name} hotbar.0 with ${item}`)
  bot.bot.setQuickBarSlot(0)
  await sleep(300)
  await enchantHeld(rcon, bot.name, enchants)
  // A new item in the hand starts the attack cooldown again
  await sleep(300)
}

/**
 * Puts the item with the custom enchants on the bot: slot is head, chest, legs or feet.
 */
async function wear (rcon, bot, slot, item, enchants = []) {
  await hold(rcon, bot, item, enchants)
  await rcon.command(`minecraft:item replace entity ${bot.name} armor.${slot} from entity ${bot.name} hotbar.0`)
  await rcon.command(`minecraft:item replace entity ${bot.name} hotbar.0 with air`)
  await sleep(300)
}

async function data (rcon, target, path) {
  return await rcon.command(`data get entity ${target} ${path}`)
}

/**
 * A number the entity has, e.g. Health or foodLevel.
 */
async function number (rcon, target, path) {
  const answer = await data(rcon, target, path)
  const match = /data: (-?[\d.]+)/.exec(answer)
  if (!match) throw new Error(`No ${path} of ${target}: ${answer}`)
  return Number(match[1])
}

const health = (rcon, target) => number(rcon, target, 'Health')

/**
 * The ids of the target's potion effects, e.g. ['minecraft:poison'].
 */
async function effects (rcon, target) {
  const answer = await data(rcon, target, 'active_effects')
  return [...answer.matchAll(/id: "([a-z_:]+)"/g)].map(m => m[1])
}

/**
 * Waits until the target has the effect.
 */
async function waitForEffect (rcon, target, effect, timeoutMs = 3000) {
  const deadline = Date.now() + timeoutMs
  for (;;) {
    const now = await effects(rcon, target)
    if (now.includes('minecraft:' + effect)) return
    if (Date.now() > deadline) throw new Error(`${target} has no ${effect}, only ${now.join(', ') || 'nothing'}`)
    await sleep(200)
  }
}

/**
 * How many of the item the player has, counted on the server.
 */
async function count (rcon, name, item) {
  const answer = await rcon.command(`minecraft:clear ${name} ${item} 0`)
  const match = /(\d+)/.exec(answer)
  return /No items/i.test(answer) || !match ? 0 : Number(match[1])
}

/**
 * How many dropped items of the kind lie within the distance of x, y, z.
 */
async function itemsOnGround (rcon, item, x, y, z, distance = 6) {
  const answer = await rcon.command(`execute as @e[type=item,x=${x},y=${y},z=${z},distance=..${distance}] ` +
    `if data entity @s Item{id:"minecraft:${item}"} run data get entity @s Item.count`)
  return [...answer.matchAll(/data: (\d+)/g)].reduce((sum, m) => sum + Number(m[1]), 0)
}

async function isBlock (rcon, x, y, z, block) {
  return /passed/i.test(await rcon.command(`execute if block ${x} ${y} ${z} ${block}`))
}

/**
 * All the XP points the player has got, over all levels.
 */
async function xpPoints (rcon, name) {
  return await number(rcon, name, 'XpTotal')
}

/**
 * Heals the player fully and fills the hunger bar.
 */
async function refresh (rcon, name) {
  await rcon.command(`minecraft:effect give ${name} instant_health 1 10 true`)
  await rcon.command(`minecraft:effect give ${name} saturation 2 20 true`)
  await sleep(1000)
  await rcon.command(`minecraft:effect clear ${name}`)
}

/**
 * A mob for the tests at x, y, z, tagged "ie". extra is more NBT, e.g. 'Health:5f'.
 */
async function summon (rcon, type, x, y, z, extra = '') {
  await rcon.command(`summon ${type} ${x} ${y} ${z} {Tags:["ie"],NoAI:1b,PersistenceRequired:1b,Silent:1b${extra ? ',' + extra : ''}}`)
}

/**
 * Gives the tagged mob this much health, raising its max health to fit.
 */
async function setMobHealth (rcon, selector, max, now) {
  await rcon.command(`attribute ${selector} minecraft:max_health base set ${max}`)
  await rcon.command(`data merge entity ${selector} {Health:${now}f}`)
}

/**
 * The mineflayer entity of the closest mob of the type.
 */
async function mob (bot, type) {
  return await bot.until(() => bot.bot.nearestEntity(e => e.name === type), 3000, `The bot sees no ${type}`)
}

module.exports = {
  Y, setUp, floor, tearDown, enchantHeld, hold, wear, data, number, health, effects, waitForEffect, count, itemsOnGround,
  isBlock, xpPoints, refresh, summon, setMobHealth, mob
}
