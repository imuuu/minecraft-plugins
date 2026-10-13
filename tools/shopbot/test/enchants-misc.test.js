'use strict'
// ImusEnchants: a custom book clicked on a tool in the inventory gives the enchant, a Slot Core gives a tool
// slots and a book in the enchanting table's grid enchants it, Curse of Fragility wears a tool out twice as
// fast, and Soulbound keeps an item on death once.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Vec3 } = require('vec3')
const { Rcon } = require('../lib/rcon')
const { sleep } = require('../lib/shopbot')
const f = require('../lib/enchant-fixture')

const BOT = 'MiscBot'
const X = 470
const Z = 340
const Y = f.Y
// Slots of the bot's own inventory window: 9-35 the main inventory, 36-44 the hotbar
const HOTBAR = 36

let rcon
let bot

before(async () => {
  rcon = await Rcon.connect()
  bot = await f.setUp(rcon, BOT, X, Z)
})

after(async () => {
  await f.tearDown(rcon, bot)
  rcon?.close()
})

async function reset () {
  await f.floor(rcon, X, Z)
  await rcon.command(`minecraft:clear ${BOT}`)
  await rcon.command(`minecraft:tp ${BOT} ${X + 0.5} ${Y} ${Z + 0.5} -90 0`)
  await sleep(500)
}

/**
 * The custom enchants on the item in an inventory slot (0-8 hotbar, 9-35 the rest), as "key:level,...".
 */
async function customEnchants (slot) {
  const answer = await f.data(rcon, BOT, `Inventory[{Slot:${slot}b}].components."minecraft:custom_data".PublicBukkitValues."imusapi:ie_custom_enchants"`)
  const match = /data: "([^"]*)"/.exec(answer)
  return match ? match[1] : ''
}

test('a custom book clicked on a sword gives it the enchant', async () => {
  await reset()
  await rcon.command(`minecraft:item replace entity ${BOT} hotbar.0 with iron_sword`)
  await rcon.command(`ien book venom 1 ${BOT}`)
  await bot.until(() => bot.bot.inventory.items().some(item => item.name === 'enchanted_book'), 3000, 'No book given')
  const book = bot.bot.inventory.items().find(item => item.name === 'enchanted_book')
  bot.clearMessages()
  await bot.bot.clickWindow(book.slot, 0, 0)
  await sleep(300)
  await bot.bot.clickWindow(HOTBAR, 0, 0)
  await bot.waitForMessage(/Applied Venom/)
  assert.equal(await customEnchants(0), 'venom:1')
  assert.equal(await f.count(rcon, BOT, 'enchanted_book'), 0, 'the book is used up')
})

test('a Slot Core gives slots, and a book in the table grid enchants the tool', async () => {
  await reset()
  await rcon.command(`minecraft:item replace entity ${BOT} hotbar.0 with diamond_pickaxe`)
  bot.bot.setQuickBarSlot(0)
  await rcon.command(`ien core ${BOT}`)
  await rcon.command(`ien book tunnel 1 ${BOT}`)
  await bot.until(() => bot.bot.inventory.items().some(item => item.name === 'enchanted_book') &&
    bot.bot.inventory.items().length >= 3, 3000, 'No core and book given')

  // The core on the cursor, clicked on the pickaxe
  const core = bot.bot.inventory.items().find(item => item.name !== 'enchanted_book' && item.name !== 'diamond_pickaxe')
  bot.clearMessages()
  await bot.bot.clickWindow(core.slot, 0, 0)
  await sleep(300)
  await bot.bot.clickWindow(HOTBAR, 0, 0)
  await bot.waitForMessage(/given slots/)

  // Right-clicking a table with the slotted pickaxe opens the grid with the pickaxe in it
  await rcon.command(`setblock ${X + 2} ${Y} ${Z} enchanting_table`)
  await bot.until(() => bot.bot.blockAt(new Vec3(X + 2, Y, Z))?.name === 'enchanting_table', 3000, 'The bot does not see the table')
  const grid = bot.nextWindow(5000, 'The table opened no grid')
  await bot.bot.activateBlock(bot.bot.blockAt(new Vec3(X + 2, Y, Z)))
  const window = await grid
  // Open slots are the empty ones; closed ones are black glass
  const open = [...Array(45).keys()].find(slot => !window.slots[slot])
  assert.ok(open !== undefined, 'the pickaxe has an open slot')
  const book = await bot.waitForOwnSlot('enchanted_book')
  await bot.click(book)
  await bot.click(open)
  // The enchanting table button
  await bot.click(50)
  bot.closeWindow()
  await sleep(500)
  const answer = await f.data(rcon, BOT, 'Inventory[{id:"minecraft:diamond_pickaxe"}].components."minecraft:custom_data".PublicBukkitValues."imusapi:ie_custom_enchants"')
  assert.match(answer, /tunnel:1/)
})

/**
 * Mines two stone blocks with a fresh pickaxe and returns its damage.
 */
async function wear (enchants) {
  await reset()
  await f.hold(rcon, bot, 'iron_pickaxe', enchants)
  for (const z of [Z, Z + 1]) {
    await rcon.command(`setblock ${X + 2} ${Y} ${z} stone`)
    await bot.until(() => bot.bot.blockAt(new Vec3(X + 2, Y, z))?.name === 'stone', 3000, 'The bot does not see the stone')
    await bot.bot.dig(bot.bot.blockAt(new Vec3(X + 2, Y, z)), true)
    await sleep(300)
  }
  const answer = await f.data(rcon, BOT, 'Inventory[{Slot:0b}].components."minecraft:damage"')
  const match = /data: (\d+)/.exec(answer)
  return match ? Number(match[1]) : 0
}

test('Curse of Fragility wears a tool out twice as fast', async () => {
  assert.equal(await wear([]), 2)
  assert.equal(await wear([['curse_of_fragility', 1]]), 4)
})

test('Soulbound keeps the item on death, once', async () => {
  await reset()
  // The hotbar is kept anyway (ImusDontLoseItems), so the sword goes in the main inventory
  await f.hold(rcon, bot, 'iron_sword', [['soulbound', 1]])
  await rcon.command(`minecraft:item replace entity ${BOT} inventory.0 from entity ${BOT} hotbar.0`)
  await rcon.command(`minecraft:item replace entity ${BOT} hotbar.0 with air`)
  await rcon.command(`minecraft:item replace entity ${BOT} inventory.1 with iron_ingot`)
  await sleep(300)
  await rcon.command(`minecraft:kill ${BOT}`)
  // Mineflayer respawns by itself
  await sleep(4000)
  assert.equal(await f.count(rcon, BOT, 'iron_sword'), 1, 'the sword came back')
  assert.equal(await f.count(rcon, BOT, 'iron_ingot'), 0, 'the ingot without Soulbound was dropped')
  const answer = await f.data(rcon, BOT, 'Inventory[{id:"minecraft:iron_sword"}].components."minecraft:custom_data"')
  assert.ok(!/soulbound/.test(answer), 'Soulbound is used up: ' + answer)
})
