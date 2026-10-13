'use strict'
// Runs SQL on the test server's database with the mysql command line client.
const { execFileSync } = require('child_process')

// XAMPP's client by default; set MYSQL to use another
const CLIENT = process.env.MYSQL || 'F:/xampp/mysql/bin/mysql.exe'

/**
 * Runs the statements in the database and returns what the client printed.
 */
function sql (database, statements) {
  return execFileSync(CLIENT, ['-u', 'root', '--protocol=tcp', '-P', '3306', '-N', '-D', database, '-e', statements], { encoding: 'utf8' })
}

module.exports = { sql }
