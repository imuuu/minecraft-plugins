'use strict'
// Setup shared by the test files: test players, money and opening shops.
const assert = require('node:assert/strict')
const { sleep } = require('./shopbot')

// Shop menu buttons
const TAB_BUTTON = 31
const HOTBAR_TOGGLE = 33

/**
 * Makes the player an op in survival with an empty inventory and this much money.
 */
async function resetPlayer (rcon, name, money = 1000) {
  await rcon.command(`op ${name}`)
  await rcon.command(`gamemode survival ${name}`)
  await rcon.command(`clear ${name}`)
  await rcon.command(`eco set ${name} ${money}`)
  await sleep(300)
}

/**
 * The player's Essentials balance. Essentials answers "Pelaajan X saldo: $1,000.50".
 */
async function balance (rcon, name) {
  const text = await rcon.command(`money ${name}`)
  const match = /\$\s*(-?\d[\d,]*(?:\.\d+)?)/.exec(text)
  assert.ok(match, 'Balance not found in: ' + text)
  return Number(match[1].replace(/,/g, ''))
}

/**
 * Sets the price of the item the bot holds in its first hotbar slot, which must not be empty.
 */
async function setHandPrice (bot, price) {
  bot.bot.setQuickBarSlot(0)
  await sleep(200)
  await bot.command(`gs set material price hand ${price}`)
}

/**
 * Opens the shop for the bot. /give puts items in the hotbar, which the shop leaves out until the hotbar
 * button is clicked; tab 0 = blocks, 1 = tools and armor, 2 = shulker boxes.
 */
async function openShop (rcon, bot, shop, { includeHotbar = true, tab = 0 } = {}) {
  bot.closeWindow()
  await sleep(300)
  await rcon.command(`gsopenfor ${bot.name} ${shop}`)
  const window = await bot.waitForWindow()
  await sleep(200)
  if (includeHotbar)
    await bot.click(HOTBAR_TOGGLE)
  for (let i = 0; i < tab; i++)
    await bot.click(TAB_BUTTON)
  return window
}

module.exports = { resetPlayer, balance, setHandPrice, openShop, TAB_BUTTON, HOTBAR_TOGGLE }
