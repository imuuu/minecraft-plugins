'use strict'
// imusAPI's permissions menu in a real client: an op opens /imustnt permissions, picks a player standing next to
// them, gives imustnt.admin (a high one, so it asks first) and the player can use /imustnt at once; the grant is
// in permissions.yml; taking it away works at once too. /ia perms lists the plugins built on imusAPI.
// The menus' own logic has unit tests (ImusAPI PermissionsTest); this checks the attachments and the file.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const fs = require('fs')
const path = require('path')
const { Rcon, SERVER_DIR } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')

const ADMIN = 'PermAdmin'
const PLAYER = 'PermPlayer'
const RESET_ALL = 47
const YES = 11
const FILE = path.join(SERVER_DIR, 'plugins', 'imusAPI', 'permissions.yml')

let rcon
let admin
let player

before(async () => {
  rcon = await Rcon.connect()
  admin = await joinServer(ADMIN)
  player = await joinServer(PLAYER)
  await rcon.command(`op ${ADMIN}`)
  await rcon.command(`deop ${PLAYER}`)
  for (const name of [ADMIN, PLAYER]) {
    await rcon.command(`gamemode survival ${name}`)
    await rcon.command(`clear ${name}`)
  }
  await rcon.command(`tp ${ADMIN} 700 120 700`)
  await rcon.command(`tp ${PLAYER} 702 120 700`)
  await sleep(1000)
  await resetPlayer()
})

after(async () => {
  await resetPlayer().catch(() => {})
  admin?.quit()
  player?.quit()
  rcon?.close()
})

function nameOf (item) {
  return item ? JSON.stringify(item.customName ?? '').replace(/§./g, '') : ''
}

function slotNamed (pattern) {
  const window = admin.bot.currentWindow
  return window.slots.slice(0, window.inventoryStart).findIndex(item => pattern.test(nameOf(item)))
}

/**
 * The admin's menu of the player's imusTNT permissions, through /imustnt permissions and the nearby list.
 */
async function openPlayersPermissions () {
  const players = admin.nextWindow(5000, '/imustnt permissions opened nothing')
  await admin.command('imustnt permissions')
  await players
  assert.match(admin.windowTitle(), /imusTNT: pick a player/)
  const slot = slotNamed(new RegExp(PLAYER))
  assert.ok(slot >= 0, `${PLAYER} is not listed close by`)
  await admin.clickForNewWindow(slot)
  assert.match(admin.windowTitle(), new RegExp(`imusTNT: ${PLAYER}`))
}

/**
 * Everything given to the player in imusTNT back to the defaults.
 */
async function resetPlayer () {
  await openPlayersPermissions()
  if (/Reset all/.test(nameOf(admin.slot(RESET_ALL)))) {
    await admin.clickForNewWindow(RESET_ALL)
    await admin.clickForNewWindow(YES)
  }
  admin.closeWindow()
  await sleep(300)
}

async function playerGetsTnt () {
  await rcon.command(`clear ${PLAYER}`)
  await sleep(300)
  await player.command('imustnt give chunk-tnt', 1000)
  return player.count('tnt') > 0
}

test('/ia perms lists the plugins built on imusAPI', async () => {
  const list = admin.nextWindow(5000, '/ia perms opened nothing')
  await admin.command('ia perms')
  await list
  assert.match(admin.windowTitle(), /Permissions/)
  for (const plugin of [/imusTNT/, /imusGS/, /imusAPI/]) assert.ok(slotNamed(plugin) >= 0, `${plugin} not listed`)
  admin.closeWindow()
})

test('a high permission given in the menu works at once, and taking it away too', async () => {
  assert.equal(await playerGetsTnt(), false, `${PLAYER} could use /imustnt before`)

  await openPlayersPermissions()
  const slot = slotNamed(/imustnt\.admin/)
  assert.ok(slot >= 0, 'imustnt.admin is not in the menu')
  assert.match(JSON.stringify(admin.lore(slot)), /Danger: High/)
  assert.equal(admin.slot(slot).name, 'gray_dye')

  await admin.clickForNewWindow(slot)
  assert.match(admin.windowTitle(), /Give high permission/)
  await admin.clickForNewWindow(YES)
  await admin.until(() => admin.slot(slot)?.name === 'lime_dye', 3000, 'The permission did not show as given')
  admin.closeWindow()

  assert.equal(await playerGetsTnt(), true, `${PLAYER} couldn't use /imustnt after it was given`)
  const file = fs.readFileSync(FILE, 'utf8')
  assert.match(file, new RegExp(`name: ${PLAYER}`))
  assert.match(file, /- imustnt\.admin/)

  await openPlayersPermissions()
  await admin.click(slot)
  await admin.until(() => admin.slot(slot)?.name === 'gray_dye', 3000, 'The permission did not show as taken')
  admin.closeWindow()
  assert.equal(await playerGetsTnt(), false, `${PLAYER} could still use /imustnt`)
})

test('every plugin\'s command opens its own permissions', async () => {
  for (const [command, plugin] of [['gs perms', 'imusGS'], ['waystones perms', 'imusWaystones'],
    ['spawners perms', 'ImusSpawners'], ['imq perms', 'ImusMiniQuests'], ['ic permissions', 'ImusChallenges'],
    ['ien perms', 'ImusEnchants'], ['imustnt perms', 'imusTNT'], ['ht perms', 'HomeTele'],
    ['difficult perms', 'ImusDifficult'], ['dontlose perms', 'ImusDontLoseItems'],
    ['hellgear perms', 'ImusHellGear'], ['fear perms', 'ImusUnstableFearSystem']]) {
    const opened = admin.nextWindow(5000, `/${command} opened nothing`)
    await admin.command(command)
    await opened
    assert.match(admin.windowTitle(), new RegExp(`${plugin}: pick a player`))
    admin.closeWindow()
    await sleep(300)
  }
})

test('permissions.yml edited by hand applies with /ia perms reload, no restart', async () => {
  const uuid = player.bot.player.uuid
  const before = fs.readFileSync(FILE, 'utf8')
  fs.writeFileSync(FILE, `players:\n  ${uuid}:\n    name: ${PLAYER}\n    grants:\n    - imustnt.admin\n`)
  try {
    assert.match(await rcon.command('ia perms reload'), /1 players/)
    assert.equal(await playerGetsTnt(), true, `${PLAYER} couldn't use /imustnt after the reload`)
  } finally {
    fs.writeFileSync(FILE, before)
    await rcon.command('ia perms reload')
  }
  assert.equal(await playerGetsTnt(), false, `${PLAYER} could still use /imustnt`)
})

test('a non-op who changes permissions can\'t change an op\'s', async () => {
  const uuid = player.bot.player.uuid
  const before = fs.readFileSync(FILE, 'utf8')
  fs.writeFileSync(FILE, `players:\n  ${uuid}:\n    name: ${PLAYER}\n    grants:\n    - ia.permissions\n    - imustnt.admin\n`)
  try {
    await rcon.command('ia perms reload')
    await sleep(500)
    const opened = player.nextWindow(5000, `/ia perms imusTNT ${ADMIN} opened nothing`)
    await player.command(`ia perms imusTNT ${ADMIN}`)
    await opened
    const window = player.bot.currentWindow
    const slot = window.slots.slice(0, window.inventoryStart).findIndex(item => /imustnt\.admin/.test(nameOf(item)))
    assert.ok(slot >= 0, 'imustnt.admin is not in the menu')
    player.clearMessages()
    await player.click(slot)
    await player.waitForMessage(/Only ops can change/)
    assert.equal(player.slot(slot)?.name, 'lime_dye', `${ADMIN} lost the permission`)
    player.closeWindow()
  } finally {
    fs.writeFileSync(FILE, before)
    await rcon.command('ia perms reload')
  }
})
