'use strict'
// Writes an old HomeTele's homes.yml and settings.yml into run/plugins/HomeTele, so a test can check they carry
// over. Run it while the server is stopped: node lib/hometele-fixture.js
const crypto = require('crypto')
const fs = require('fs')
const path = require('path')
const { SERVER_DIR } = require('./rcon')

const OLD_BOT = 'OldHomeBot'
const OLD_HOME = { x: 120.5, y: 90, z: 120.5 }

/**
 * The uuid an offline-mode server gives the player: UUID.nameUUIDFromBytes("OfflinePlayer:" + name).
 */
function offlineUuid (name) {
  const hash = crypto.createHash('md5').update('OfflinePlayer:' + name, 'utf8').digest()
  hash[6] = (hash[6] & 0x0f) | 0x30
  hash[8] = (hash[8] & 0x3f) | 0x80
  const hex = hash.toString('hex')
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`
}

function write () {
  const folder = path.join(SERVER_DIR, 'plugins', 'HomeTele')
  fs.mkdirSync(folder, { recursive: true })
  fs.writeFileSync(path.join(folder, 'homes.yml'), [
    'homes:',
    `  ${offlineUuid(OLD_BOT)}:`,
    '    loc:',
    '      ==: org.bukkit.Location',
    '      world: world',
    `      x: ${OLD_HOME.x}`,
    `      y: ${OLD_HOME.y}`,
    `      z: ${OLD_HOME.z}`,
    '      pitch: 0.0',
    '      yaw: 0.0',
    `    name: ${OLD_BOT}`,
    ''
  ].join('\n'))
  fs.writeFileSync(path.join(folder, 'settings.yml'), [
    'settings:',
    '  teleCastTime: 3.0',
    '  teleCooldown: 120.0',
    '  setHomeCooldown: 600.0',
    ''
  ].join('\n'))
  // A config.yml of an earlier run would already have the settings migrated
  fs.rmSync(path.join(folder, 'config.yml'), { force: true })
}

if (require.main === module) write()

module.exports = { OLD_HOME, offlineUuid, write }
