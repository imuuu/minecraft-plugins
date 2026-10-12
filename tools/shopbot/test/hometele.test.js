'use strict'
// HomeTele: /sethome and /home with the warmup, moving cancels, the cooldown, several named homes and the admin
// list. The bot plays in creative, so mobs and falling can't cancel a teleport by hitting it.
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const { resetPlayer } = require('../lib/fixture')

const BOT = 'HomeBot'

let rcon
let bot

const set = (setting, value) => rcon.command(`ia config hometele set ${setting} ${value}`)

/**
 * Horizontal distance from the bot to x, z.
 */
function distanceTo (x, z) {
  const p = bot.bot.entity.position
  return Math.hypot(p.x - x, p.z - z)
}

async function moveTo (x, z) {
  await rcon.command(`tp ${BOT} ${x} 100 ${z}`)
  await sleep(1000)
}

before(async () => {
  rcon = await Rcon.connect()
  bot = await joinServer(BOT)
  await resetPlayer(rcon, BOT)
  await rcon.command(`gamemode creative ${BOT}`)
  await set('sethome-cooldown-seconds', 0)
  await set('teleport-cooldown-seconds', 0)
  await set('cancel-cooldown-seconds', 0)
  await set('max-homes', 1)
  await moveTo(40, 40)
  await bot.command('sethome')
})

after(async () => {
  await set('warmup-seconds', 10)
  await set('teleport-cooldown-seconds', 300)
  await set('sethome-cooldown-seconds', 1800)
  await set('cancel-cooldown-seconds', 15)
  await set('max-homes', 1)
  await rcon?.command(`op ${BOT}`)
  await rcon?.command(`gamemode survival ${BOT}`)
  bot?.quit()
  rcon?.close()
})

test('/sethome and /home after standing still', async () => {
  await set('warmup-seconds', 1)
  await moveTo(40, 40)
  bot.clearMessages()
  await bot.command('sethome')
  await bot.waitForMessage(/Home home (set|moved)/)

  await moveTo(80, 80)
  bot.clearMessages()
  await bot.command('home')
  await bot.waitForMessage(/Teleporting home in 1s/)
  await bot.waitForMessage(/teleported home/, 4000)
  await sleep(300)
  assert.ok(distanceTo(40, 40) < 1.5, `at ${bot.bot.entity.position}`)
})

test('moving cancels the teleport', async () => {
  await set('warmup-seconds', 3)
  await moveTo(80, 80)
  bot.clearMessages()
  await bot.command('home')
  await sleep(500)
  await moveTo(90, 90)
  await bot.waitForMessage(/cancelled, you moved/)
  await sleep(3000)
  assert.ok(distanceTo(90, 90) < 2, `still where it moved to: ${bot.bot.entity.position}`)
})

test('players wait for the cooldown, ops do not', async () => {
  await set('warmup-seconds', 0)
  await set('teleport-cooldown-seconds', 0)
  await rcon.command(`deop ${BOT}`)
  await sleep(300)
  try {
    bot.clearMessages()
    await bot.command('home')
    await bot.waitForMessage(/teleported home/)
    // The cooldown counts from the last teleport, with the setting as it is now
    await set('teleport-cooldown-seconds', 300)
    bot.clearMessages()
    await bot.command('home')
    await bot.waitForMessage(/You can do that again in (299|300)s/)
  } finally {
    await rcon.command(`op ${BOT}`)
    await set('teleport-cooldown-seconds', 0)
  }
  bot.clearMessages()
  await bot.command('home')
  await bot.waitForMessage(/teleported home/)
})

test('several named homes up to the limit', async () => {
  await set('max-homes', 2)
  await bot.command('delhome base')
  bot.clearMessages()
  await bot.command('sethome base')
  await bot.waitForMessage(/Home base set/)
  bot.clearMessages()
  await bot.command('sethome farm')
  await bot.waitForMessage(/You can have 2 home\(s\)/)
  bot.clearMessages()
  await bot.command('homes')
  const list = await bot.waitForMessage(/Your homes/)
  assert.match(list, /\[home\].*\[base\]/)
  bot.clearMessages()
  await bot.command('delhome base')
  await bot.waitForMessage(/Home base deleted/)
})

test('admins see everyone\'s homes', async () => {
  bot.clearMessages()
  await bot.command('homelist')
  await bot.waitForMessage(/Homes of \d+ players/)
  await bot.waitForMessage(new RegExp(`${BOT}: \\[home\\]`))
})
