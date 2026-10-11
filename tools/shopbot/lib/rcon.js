'use strict'
// Minimal RCON client for the local test server's console. Reads the password from run/server.properties.
const net = require('net')
const fs = require('fs')
const path = require('path')

const SERVER_DIR = path.resolve(__dirname, '../../../run')

function serverProperties () {
  const props = {}
  for (const line of fs.readFileSync(path.join(SERVER_DIR, 'server.properties'), 'utf8').split(/\r?\n/)) {
    const match = /^([^#=]+)=(.*)$/.exec(line)
    if (match) props[match[1].trim()] = match[2].trim()
  }
  return props
}

function packet (id, type, body) {
  const payload = Buffer.from(body, 'utf8')
  const buffer = Buffer.alloc(14 + payload.length)
  buffer.writeInt32LE(10 + payload.length, 0)
  buffer.writeInt32LE(id, 4)
  buffer.writeInt32LE(type, 8)
  payload.copy(buffer, 12)
  return buffer
}

class Rcon {
  constructor (socket) {
    this._socket = socket
    this._nextId = 1
    this._pending = new Map()
    this._buffer = Buffer.alloc(0)
    socket.on('data', data => this._receive(data))
  }

  static async connect () {
    const props = serverProperties()
    if (props['enable-rcon'] !== 'true') throw new Error('RCON is off in run/server.properties')
    const socket = net.connect({ host: props['server-ip'] || '127.0.0.1', port: Number(props['rcon.port'] || 25575) })
    await new Promise((resolve, reject) => {
      socket.once('connect', resolve)
      socket.once('error', reject)
    })
    const rcon = new Rcon(socket)
    const id = rcon._nextId++
    const reply = rcon._wait(id)
    socket.write(packet(id, 3, props['rcon.password']))
    if ((await reply).id === -1) throw new Error('RCON password was refused')
    return rcon
  }

  /**
   * Runs a console command and returns its output, colour codes removed.
   */
  async command (text) {
    const id = this._nextId++
    const reply = this._wait(id)
    this._socket.write(packet(id, 2, text))
    return (await reply).body.replace(/§./g, '')
  }

  close () {
    this._socket.end()
  }

  _wait (id) {
    return new Promise((resolve, reject) => {
      const timer = setTimeout(() => reject(new Error('RCON timed out')), 10000)
      this._pending.set(id, reply => { clearTimeout(timer); resolve(reply) })
      // A refused login answers with id -1
      this._pending.set(-1, reply => { clearTimeout(timer); resolve(reply) })
    })
  }

  _receive (data) {
    this._buffer = Buffer.concat([this._buffer, data])
    while (this._buffer.length >= 4) {
      const length = this._buffer.readInt32LE(0)
      if (this._buffer.length < 4 + length) return
      const id = this._buffer.readInt32LE(4)
      const body = this._buffer.toString('utf8', 12, 4 + length - 2)
      this._buffer = this._buffer.subarray(4 + length)
      const handler = this._pending.get(id)
      if (handler) {
        this._pending.delete(id)
        handler({ id, body })
      }
    }
  }
}

module.exports = { Rcon, serverProperties, SERVER_DIR }
