'use strict'
// An old HomeTele's homes.yml and settings.yml carry over to the new one. Stops the server, writes the old files
// and starts it again, so it only runs with SHOPBOT_RESTART=1.
const { test, after } = require('node:test')
const assert = require('node:assert/strict')
const { Rcon } = require('../lib/rcon')
const { joinServer, sleep } = require('../lib/shopbot')
const server = require('../lib/server')
const hometele = require('../lib/hometele-fixture')

const OLD_BOT = 'OldHomeBot'
const run = process.env.SHOPBOT_RESTART === '1'

let rcon
let bot

after(async () => {
  bot?.quit()
  rcon?.close()
})

test('an old HomeTele home and its settings carry over', { skip: !run && 'set SHOPBOT_RESTART=1 to restart the server' }, async () => {
  await server.stop()
  hometele.write()
  await server.start()

  rcon = await Rcon.connect()
  const list = (await rcon.command('ia config hometele list')).replace(/§./g, '')
  assert.match(list, /warmup-seconds 3\b/)
  assert.match(list, /teleport-cooldown-seconds 120\b/)
  assert.match(list, /sethome-cooldown-seconds 600\b/)

  bot = await joinServer(OLD_BOT)
  await rcon.command(`op ${OLD_BOT}`)
  await rcon.command(`gamemode creative ${OLD_BOT}`)
  await rcon.command(`tp ${OLD_BOT} 0 100 0`)
  // Land first: falling counts as moving
  await sleep(5000)
  await bot.command('home')
  await bot.waitForMessage(/teleported home/, 6000)
  await sleep(300)
  const p = bot.bot.entity.position
  assert.ok(Math.hypot(p.x - hometele.OLD_HOME.x, p.z - hometele.OLD_HOME.z) < 1.5, `at ${p}`)

  await rcon.command('ia config hometele set warmup-seconds 10')
  await rcon.command('ia config hometele set teleport-cooldown-seconds 300')
  await rcon.command('ia config hometele set sethome-cooldown-seconds 1800')
  await rcon.command(`gamemode survival ${OLD_BOT}`)
})
