'use strict'
// An admin sets a custom price of 20 diamonds and 10 iron through the shop's admin menus, and a player buys
// with it through the custom price buy menu.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer, balance, openShop } = require('../lib/fixture')

const SHOP = 'BotCustom'
const ADMIN = 'CustomAdmin'
const BUYER = 'CustomBuyer'
const ITEM = 'totem_of_undying'

// Admin menu slots
const STOCKABLE_PRICE = 14
const STOCKABLE_CONFIRM = 18
const PRICE_TYPE_CUSTOM = 6
const CUSTOM_PRICE_ITEM_SLOTS = 45
const CUSTOM_PRICE_CONFIRM = 53
// Custom price buy menu
const BUY_BUTTON = 53

let rcon
let admin
let buyer

before(async () => {
  rcon = await Rcon.connect()
  admin = await joinServer(ADMIN)
  buyer = await joinServer(BUYER)
  await resetPlayer(rcon, ADMIN)
  await resetPlayer(rcon, BUYER)
  await rcon.command(`give ${ADMIN} ${ITEM} 1`)
  await sleep(300)
  admin.bot.setQuickBarSlot(0)
  await admin.command(`gs create shop ${SHOP}`)
  await admin.command(`gs add hand ${SHOP}`)
  await rcon.command(`clear ${ADMIN}`)
  await rcon.command(`give ${ADMIN} diamond 20`)
  await rcon.command(`give ${ADMIN} iron_ingot 10`)
  await sleep(300)
})

after(async () => {
  for (const bot of [admin, buyer]) {
    bot?.closeWindow()
    bot?.quit()
  }
  rcon?.close()
})

/**
 * Puts a stack from the bot's inventory on the first empty item slot of the custom price editor, the way an
 * admin does: pick it up, click the slot, put it back. The editor keeps a copy as part of the price.
 */
async function addToPrice (bot, itemName) {
  const from = await bot.waitForOwnSlot(itemName)
  assert.ok(from !== undefined, `${bot.name} should have ${itemName}`)
  let target = 0
  while (target < CUSTOM_PRICE_ITEM_SLOTS && bot.slot(target)) target++

  await bot.click(from)
  await bot.click(target)
  await bot.click(from)
}

/**
 * The editor slot that shows the item as part of the price. An item already in the price keeps its slot and
 * gets the new amount.
 */
function priceSlotOf (bot, itemName) {
  for (let slot = 0; slot < CUSTOM_PRICE_ITEM_SLOTS; slot++) {
    if (bot.slot(slot)?.name === itemName) return slot
  }
  return undefined
}

test('an admin sets a custom price of 20 diamonds and 10 iron', async () => {
  admin.closeWindow()
  await admin.command(`gs modify shop ${SHOP}`)
  await admin.waitForWindow()
  const listing = await admin.waitForShopSlot(ITEM)
  assert.ok(listing !== undefined, 'The editor should show the stockable totem')

  await admin.clickForNewWindow(listing)
  await admin.clickForNewWindow(STOCKABLE_PRICE)
  await admin.clickForNewWindow(PRICE_TYPE_CUSTOM)
  assert.match(admin.windowTitle(), /Create Custom Price/)

  await addToPrice(admin, 'diamond')
  await addToPrice(admin, 'iron_ingot')
  assert.match(admin.lore(priceSlotOf(admin, 'diamond')).join('\n'), /Amount: 20/)
  assert.match(admin.lore(priceSlotOf(admin, 'iron_ingot')).join('\n'), /Amount: 10/)
  assert.equal(admin.count('diamond'), 20, 'The admin keeps the items')
  assert.equal(admin.count('iron_ingot'), 10, 'The admin keeps the items')

  await admin.clickForNewWindow(CUSTOM_PRICE_CONFIRM)
  assert.match(admin.windowTitle(), /Modify Stockable Item/)
  await admin.clickForNewWindow(STOCKABLE_CONFIRM)
  assert.match(admin.windowTitle(), /Modding/)
  admin.closeWindow()
})

test('the shop shows the item with a custom price', async () => {
  await openShop(rcon, buyer, SHOP, { includeHotbar: false })
  const slot = await buyer.waitForShopSlot(ITEM)
  assert.ok(slot !== undefined, 'The totem should be listed')
  assert.match(buyer.lore(slot).join('\n'), /Click.*to see price/)
})

test('without the items the purchase is refused and nothing changes', async () => {
  await resetPlayer(rcon, BUYER)
  await openShop(rcon, buyer, SHOP, { includeHotbar: false })
  await buyer.clickForNewWindow(await buyer.waitForShopSlot(ITEM))
  assert.match(buyer.windowTitle(), /Buying/)
  buyer.clearMessages()

  await buyer.click(BUY_BUTTON)

  await buyer.waitForMessage(/don't have the items/)
  assert.equal(buyer.count(ITEM), 0)
})

test('a player pays with 20 diamonds and 10 iron and gets the totem', async () => {
  await resetPlayer(rcon, BUYER)
  await rcon.command(`give ${BUYER} diamond 20`)
  await rcon.command(`give ${BUYER} iron_ingot 10`)
  await sleep(300)
  const moneyBefore = await balance(rcon, BUYER)

  await openShop(rcon, buyer, SHOP, { includeHotbar: false })
  await buyer.clickForNewWindow(await buyer.waitForShopSlot(ITEM))
  const diamondCost = await buyer.waitForShopSlot('diamond')
  assert.ok(diamondCost !== undefined, 'The buy menu should show the diamond cost')
  const costLore = buyer.lore(diamondCost).join('\n')
  assert.match(costLore, /You have: 20/)
  assert.match(costLore, /Cost you: 20/)

  // Left click buys and goes back to the shop
  await buyer.clickForNewWindow(BUY_BUTTON)

  assert.equal(buyer.count(ITEM), 1)
  assert.equal(buyer.count('diamond'), 0)
  assert.equal(buyer.count('iron_ingot'), 0)
  assert.equal(await balance(rcon, BUYER), moneyBefore, 'No money is part of this price')
})
