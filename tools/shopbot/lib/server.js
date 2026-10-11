'use strict'
// Stops and starts the local test server, for tests that need a restart.
const fs = require('fs')
const path = require('path')
const { spawn } = require('child_process')
const { Rcon, SERVER_DIR } = require('./rcon')
const { sleep } = require('./shopbot')

const LOG = path.join(SERVER_DIR, 'logs', 'shopbot-server.log')

/**
 * The Java the server's own launcher uses (run/Kaynnista-palvelin.bat), or JAVA_25 from the environment.
 */
function javaPath () {
  if (process.env.JAVA_25) return process.env.JAVA_25
  const launcher = fs.readFileSync(path.join(SERVER_DIR, 'Kaynnista-palvelin.bat'), 'utf8')
  const match = /set "JAVA=([^"]+)"/.exec(launcher)
  if (!match) throw new Error('No JAVA in run/Kaynnista-palvelin.bat; set JAVA_25')
  return match[1]
}

async function isUp () {
  try {
    const rcon = await Rcon.connect()
    rcon.close()
    return true
  } catch {
    return false
  }
}

async function stop () {
  const rcon = await Rcon.connect()
  rcon.command('stop').catch(() => {})
  await sleep(500)
  rcon.close()
  for (let i = 0; i < 120 && await isUp(); i++) await sleep(1000)
  // RCON closes before the world and the plugins finish saving
  await sleep(5000)
}

/**
 * Starts the server in the background; it keeps running after the tests. Resolves when RCON answers and
 * imusGS has loaded its shops.
 */
async function start () {
  const log = fs.openSync(LOG, 'w')
  const child = spawn(javaPath(), ['-Xms1G', '-Xmx2G', '-jar', 'paper.jar', '--nogui'], {
    cwd: SERVER_DIR,
    detached: true,
    stdio: ['ignore', log, log]
  })
  child.unref()

  for (let i = 0; i < 180; i++) {
    await sleep(1000)
    const text = fs.readFileSync(LOG, 'utf8')
    if (/\[imusGS\] Loaded \d+ shops/.test(text) && await isUp()) return
    if (child.exitCode !== null) throw new Error('The server stopped while starting, see ' + LOG)
  }
  throw new Error('The server didn\'t start in 3 minutes, see ' + LOG)
}

module.exports = { stop, start, isUp, LOG }
