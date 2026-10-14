'use strict'
// Runs SQL on the test server's database, SQLite or MySQL as imusAPI's config.yml says: SQLite through Node's own
// node:sqlite on the plugin's data.db, MySQL with the mysql command line client.
const { execFileSync } = require('child_process')
const fs = require('fs')
const path = require('path')

const RUN = path.join(__dirname, '..', '..', '..', 'run')
// XAMPP's client by default; set MYSQL to use another
const CLIENT = process.env.MYSQL || 'F:/xampp/mysql/bin/mysql.exe'

/**
 * "sqlite" or "mysql", from the database section of imusAPI's config.yml.
 */
function databaseType () {
  const config = fs.readFileSync(path.join(RUN, 'plugins', 'imusAPI', 'config.yml'), 'utf8')
  const section = config.split(/^database:/m)[1] ?? ''
  const type = /^ {2}type:\s*['"]?(\w+)/m.exec(section)?.[1]
  return type === 'mysql' || type === 'mariadb' ? 'mysql' : 'sqlite'
}

/**
 * The plugin's data.db; database names match plugin folders without regard to case ("imuschallenges").
 */
function sqliteFile (database) {
  const plugins = path.join(RUN, 'plugins')
  const folder = fs.readdirSync(plugins).find(name => name.toLowerCase() === database.toLowerCase())
  if (!folder) throw new Error(`No plugin folder for the database ${database}`)
  return path.join(plugins, folder, 'data.db')
}

function sqlite (database, statements) {
  const { DatabaseSync } = require('node:sqlite')
  const db = new DatabaseSync(sqliteFile(database))
  try {
    db.exec('PRAGMA busy_timeout = 10000')
    const out = []
    for (const statement of statements.split(';').map(s => s.trim()).filter(Boolean)) {
      if (/^select/i.test(statement)) {
        for (const row of db.prepare(statement).all()) out.push(Object.values(row).join('\t'))
      } else {
        db.exec(statement)
      }
    }
    return out.length ? out.join('\n') + '\n' : ''
  } finally {
    db.close()
  }
}

/**
 * Runs the statements in the plugin's database and returns the rows a SELECT gave, a tab between columns.
 */
function sql (database, statements) {
  if (databaseType() === 'sqlite') return sqlite(database, statements)
  return execFileSync(CLIENT, ['-u', 'root', '--protocol=tcp', '-P', '3306', '-N', '-D', database, '-e', statements], { encoding: 'utf8' })
}

module.exports = { sql, databaseType }
