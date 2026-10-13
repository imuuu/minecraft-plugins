'use strict'
// Reads ImusChallenges' database for the tests: a player's points, the materials and advancements someone has
// been first to, and the current leader. Firsts stay in the database, so every run picks ones nobody has yet.
const { sql } = require('./mysql')

const DB = 'imuschallenges'

function query (statement) {
  return sql(DB, statement).trim()
}

/**
 * The player's challenge points and lifetime points, 0 when they have none.
 */
function points (name) {
  const row = query(`SELECT pp.points, pp.lifetime_points FROM player_points pp JOIN players p ON p.id = pp.player_id ` +
    `WHERE p.player_name = '${name}' ORDER BY pp.id DESC LIMIT 1`)
  if (!row) return { points: 0, lifetime: 0 }
  const [now, lifetime] = row.split(/\s+/).map(Number)
  return { points: now, lifetime }
}

/**
 * The most lifetime points anyone has.
 */
function topLifetime () {
  return Number(query('SELECT COALESCE(MAX(lifetime_points), 0) FROM player_points')) || 0
}

function collected () {
  return new Set(query('SELECT material FROM collected_materials').split(/\s+/).filter(Boolean))
}

// Rare items nobody picks up by chance, to be the first to collect
const RARE = [
  'MUSIC_DISC_13', 'MUSIC_DISC_CAT', 'MUSIC_DISC_BLOCKS', 'MUSIC_DISC_CHIRP', 'MUSIC_DISC_FAR', 'MUSIC_DISC_MALL',
  'MUSIC_DISC_MELLOHI', 'MUSIC_DISC_STAL', 'MUSIC_DISC_STRAD', 'MUSIC_DISC_WARD', 'MUSIC_DISC_11', 'MUSIC_DISC_WAIT',
  'MUSIC_DISC_OTHERSIDE', 'MUSIC_DISC_5', 'MUSIC_DISC_PIGSTEP', 'MUSIC_DISC_RELIC', 'MUSIC_DISC_CREATOR',
  'MUSIC_DISC_PRECIPICE', 'ANGLER_POTTERY_SHERD', 'ARCHER_POTTERY_SHERD', 'ARMS_UP_POTTERY_SHERD',
  'BLADE_POTTERY_SHERD', 'BREWER_POTTERY_SHERD', 'BURN_POTTERY_SHERD', 'DANGER_POTTERY_SHERD',
  'EXPLORER_POTTERY_SHERD', 'FRIEND_POTTERY_SHERD', 'HEART_POTTERY_SHERD', 'HEARTBREAK_POTTERY_SHERD',
  'HOWL_POTTERY_SHERD', 'MINER_POTTERY_SHERD', 'MOURNER_POTTERY_SHERD', 'PLENTY_POTTERY_SHERD',
  'PRIZE_POTTERY_SHERD', 'SHEAF_POTTERY_SHERD', 'SHELTER_POTTERY_SHERD', 'SKULL_POTTERY_SHERD',
  'SNORT_POTTERY_SHERD', 'FLOW_POTTERY_SHERD', 'GUSTER_POTTERY_SHERD', 'SCRAPE_POTTERY_SHERD',
  'COAST_ARMOR_TRIM_SMITHING_TEMPLATE', 'DUNE_ARMOR_TRIM_SMITHING_TEMPLATE', 'EYE_ARMOR_TRIM_SMITHING_TEMPLATE',
  'HOST_ARMOR_TRIM_SMITHING_TEMPLATE', 'RAISER_ARMOR_TRIM_SMITHING_TEMPLATE', 'RIB_ARMOR_TRIM_SMITHING_TEMPLATE',
  'SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE', 'SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE', 'SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE',
  'SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE', 'SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE', 'TIDE_ARMOR_TRIM_SMITHING_TEMPLATE',
  'VEX_ARMOR_TRIM_SMITHING_TEMPLATE', 'WARD_ARMOR_TRIM_SMITHING_TEMPLATE', 'WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE',
  'WILD_ARMOR_TRIM_SMITHING_TEMPLATE', 'BOLT_ARMOR_TRIM_SMITHING_TEMPLATE', 'FLOW_ARMOR_TRIM_SMITHING_TEMPLATE',
  'FLOWER_BANNER_PATTERN', 'CREEPER_BANNER_PATTERN', 'SKULL_BANNER_PATTERN', 'MOJANG_BANNER_PATTERN',
  'GLOBE_BANNER_PATTERN', 'PIGLIN_BANNER_PATTERN', 'FLOW_BANNER_PATTERN', 'GUSTER_BANNER_PATTERN'
]

/**
 * Rare materials nobody has collected yet, as many as asked.
 */
function uncollected (count) {
  const taken = collected()
  const free = RARE.filter(material => !taken.has(material)).slice(0, count)
  if (free.length < count) throw new Error('Every test material is collected already; add more to RARE in lib/challenge-fixture.js')
  return free
}

/**
 * Whether someone has been first to the advancement, by its title ("Stone Age").
 */
function advancementTaken (title) {
  return query(`SELECT COUNT(*) FROM player_achievements WHERE achievement_name = '${title.replace(/'/g, "''")}'`) !== '0'
}

module.exports = { query, points, topLifetime, collected, uncollected, advancementTaken }
