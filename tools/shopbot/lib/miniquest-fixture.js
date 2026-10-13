'use strict'
// Test quests for the ImusMiniQuests tests, with exact rewards and no random ones unless asked for. They are
// put at the end of the test server's quests.yml between markers, and taken out again after.
const fs = require('fs')
const path = require('path')
const { SERVER_DIR } = require('./rcon')
const { sleep } = require('./shopbot')

const FILE = path.join(SERVER_DIR, 'plugins', 'ImusMiniQuests', 'quests.yml')
const START = '  # shopbot test quests'
const END = '  # end of shopbot test quests'

/**
 * A quest with one objective. reward is the YAML of its rewards section, without random rewards unless it
 * sets random-rolls.
 */
function quest (id, type, targets, amount, { rewards = 'guaranteed:\n  - item: DIAMOND\n    min: 2\n    max: 2', timeLimit = 0 } = {}) {
  const lines = [
    `  ${id}:`,
    `    name: "&eTest ${id}"`,
    '    drop-weight: 0',
    '    objective:',
    `      type: ${type}`,
    `      targets: [${targets.map(t => `"${t}"`).join(', ')}]`,
    `      amount: ${amount}`
  ]
  if (timeLimit) lines.push(`      time-limit: ${timeLimit}`)
  lines.push('    rewards:')
  if (!/random-rolls/.test(rewards)) lines.push('      random-rolls: 0')
  for (const line of rewards.split('\n')) lines.push('      ' + line)
  return lines.join('\n')
}

const QUESTS = [
  quest('t_break', 'BREAK_BLOCK', ['OAK_LOG'], 3),
  quest('t_crop', 'BREAK_BLOCK', ['WHEAT'], 2),
  quest('t_kill', 'KILL_ENTITY', ['ZOMBIE'], 2),
  quest('t_hostile', 'KILL_ENTITY', ['@hostile'], 1),
  quest('t_fish', 'FISH', [], 1),
  quest('t_craft', 'CRAFT', ['STICK'], 8),
  quest('t_smelt', 'SMELT', ['IRON_INGOT'], 5),
  quest('t_enchant', 'ENCHANT', [], 1),
  quest('t_breed', 'BREED', ['COW'], 1),
  quest('t_trade', 'TRADE', [], 1),
  quest('t_eat', 'EAT', [], 2),
  quest('t_tame', 'TAME', ['WOLF'], 1),
  quest('t_shear', 'SHEAR', ['SHEEP'], 1),
  quest('t_walk', 'WALK', ['SAND'], 10),
  quest('t_timed', 'KILL_ENTITY', ['ZOMBIE'], 2, { timeLimit: 3 }),
  quest('t_rewards', 'BREAK_BLOCK', ['OAK_LOG'], 1, {
    rewards: [
      'guaranteed:',
      '  - item: IRON_SWORD',
      '    name: "&bTest Blade"',
      '    enchants: [SHARPNESS:4]',
      '  - tool: PICKAXE',
      '    tiers:',
      '      DIAMOND: 1',
      '    slots: true',
      '  - enchant-book: timber',
      '    level: 2',
      '  - slot-core: true',
      '    min: 2',
      '    max: 2',
      '  - money: 500',
      '  - commands: ["minecraft:give %player% apple 3"]',
      '    message: "&dTest reward message"'
    ].join('\n')
  }),
  quest('t_locked', 'BREAK_BLOCK', ['OAK_LOG'], 1, {
    rewards: 'rolls: 12\npool:\n  - item: NETHERITE_INGOT\n  - item: DIAMOND'
  }),
  quest('t_random', 'BREAK_BLOCK', ['OAK_LOG'], 1, { rewards: 'random-rolls: 3\nguaranteed:\n  - item: DIAMOND' }),
  quest('t_rare', 'BREAK_BLOCK', ['OAK_LOG'], 4, { rewards: 'guaranteed:\n  - item: DIAMOND' })
]

function strip (text) {
  const start = text.indexOf(START)
  if (start < 0) return text
  const end = text.indexOf(END, start)
  return text.slice(0, start).replace(/\n+$/, '\n') + text.slice(end + END.length).replace(/^\n+/, '')
}

/**
 * Puts the test quests in quests.yml and reloads the plugin.
 */
async function addQuests (rcon) {
  const text = strip(fs.readFileSync(FILE, 'utf8'))
  fs.writeFileSync(FILE, text.replace(/\n*$/, '\n') + [START, ...QUESTS, END, ''].join('\n'))
  const answer = await rcon.command('imq reload')
  await sleep(500)
  return answer
}

/**
 * Takes the test quests out of quests.yml again and reloads the plugin.
 */
async function removeQuests (rcon) {
  fs.writeFileSync(FILE, strip(fs.readFileSync(FILE, 'utf8')))
  await rcon?.command('imq reload')
}

const KEY = 'components."minecraft:custom_data".PublicBukkitValues'

/**
 * The quest id and progress of the panel in an inventory slot (0-8 hotbar, 9-35 the rest), or null.
 */
async function panel (rcon, name, slot) {
  const id = /data: "([^"]+)"/.exec(await rcon.command(`data get entity ${name} Inventory[{Slot:${slot}b}].${KEY}."imusminiquests:quest_id"`))
  if (!id) return null
  const progress = /data: (\d+)/.exec(await rcon.command(`data get entity ${name} Inventory[{Slot:${slot}b}].${KEY}."imusminiquests:progress"`))
  return { id: id[1], progress: progress ? Number(progress[1]) : 0 }
}

/**
 * Gives the bot a panel of the quest, of the given rarity, in the first hotbar slot.
 */
async function givePanel (rcon, bot, id, rarity = 'common') {
  await rcon.command(`minecraft:item replace entity ${bot.name} hotbar.0 with air`)
  const answer = await rcon.command(`imq give ${bot.name} ${id} 1 ${rarity}`)
  if (!/Gave/.test(answer.replace(/§./g, ''))) throw new Error(`Couldn't give ${id}: ${answer}`)
  await bot.until(() => bot.bot.inventory.items().some(item => item.slot === 36), 3000, 'No panel given')
  bot.bot.setQuickBarSlot(0)
  await sleep(300)
}

/**
 * Waits until the panel in the slot has the progress.
 */
async function waitForProgress (rcon, name, slot, progress, timeoutMs = 5000) {
  const deadline = Date.now() + timeoutMs
  let now = null
  for (;;) {
    now = await panel(rcon, name, slot)
    if (now && now.progress === progress) return now
    if (Date.now() > deadline) throw new Error(`The panel in slot ${slot} is at ${now ? now.progress : 'nothing'}, not ${progress}`)
    await sleep(200)
  }
}

module.exports = { addQuests, removeQuests, panel, givePanel, waitForProgress }
