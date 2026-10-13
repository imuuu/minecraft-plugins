# shopbot

Plays the imusGS shops on the local test server (`run/`) like a player, with
[Mineflayer](https://github.com/PrismarineJS/mineflayer), to test the shop menus end to end.

## Setup (once)

- Node.js 24, e.g. unpacked to `C:\tools\node-v24.21.0-win-x64`.
- `npm ci --ignore-scripts` here. Versions are pinned in `package-lock.json`; no package runs install scripts.
- On the test server:
  - **ViaVersion + ViaBackwards** in `run/plugins`. Mineflayer speaks Minecraft 26.1 and the server is 26.2.
  - **RCON on** in `run/server.properties` (`enable-rcon=true`, a password). It listens on `server-ip`, 127.0.0.1.
    The tests use it to give the bot op, money and items.
  - The server stays in offline mode bound to 127.0.0.1, so only this machine can join.

## Running

Start MariaDB and the server (see the main README), then:

```
npm test
```

Each test file uses its own bots and shop, created if it doesn't exist. The tests look at amounts and
balances before and after each click, so they can be run again on the same server.

| File | What it plays |
|---|---|
| `shop.test.js` | Every click type: buy and sell 1, 8, 64, all; lore prices; the hotbar button; not enough money |
| `race.test.js` | Two bots click the last unit at the same time: one gets it, only that one pays |
| `shulker.test.js` | A shulker box sells its contents, the empty box stays |
| `stockable.test.js` | `/gs add` lists a stack that never runs out |
| `custom-price.test.js` | An admin sets a price of 20 diamonds + 10 iron through the admin menus; a player buys with it, and is refused without the items |
| `profile.test.js` | The default price profile: preview without confirm, diamond 1000, recipe-priced items, scaling, tags |
| `sell-limit.test.js` | A shop buys 5 stacks of iron at the full price, then pays less to every seller; another shop has its own limit |
| `flash.test.js` | Flash deals: the announcement, a peek, the blinking button, buying up to the per player limit, no selling back; `/gs restock` |
| `market.test.js` | The day's mood (market button, prices follow it) and wanted requests (announcement, button, the bonus) |
| `progress.test.js` | Flash deals offer only items and enchants non-op players have had, up to the seen level |
| `stats.test.js` | `/gs stats` after a sale, live events in the stats, the language setting (fi/en) |
| `village.test.js` | A blacksmith from its preset with its NPC: buy only, lasting stock, enchanted gear, gear flash deals; `/gs village`; removing a shop with its NPC |
| `custom.test.js` | Custom enchants and items of ImusEnchants and imusTNT through ImusAPI: known to the shop, special TNT at its tier price, an admin price, the Enchanter's boosters and custom books, progress |
| `hometele.test.js` | HomeTele: /sethome and /home after the warmup, moving cancels, the cooldown, named homes up to the limit, /homelist |
| `hometele-upgrade.test.js` | An old HomeTele's homes.yml and settings.yml carry over (restarts the server, SHOPBOT_RESTART=1) |
| `restart.test.js` | Shops, stock, prices, sell limits, flash deals and wanted requests are the same after the server restarts |
| `tnt.test.js` | imusTNT: Chunk TNT clears its chunk layer but leaves chests, Chunk Stone TNT fills it with stone, special TNT drops itself, the admin menu |
| `spawners.test.js` | ImusSpawners: a lucky silk touch roll, a placed spawner always comes back, a miss raises the chance, `/spawners give` |
| `waystones.test.js` | imusWaystones: building and confirming, room between own waystones, finding by touch, travelling, an upgrade, the cooldown, admin settings, breaking (empties the waystone tables) |
| `old-plugins-upgrade.test.js` | The old imusWaystones and ImusSpawners data carries over (restarts the server, SHOPBOT_RESTART=1) |
| `enchants-armor.test.js` | ImusEnchants armor enchants at every level: Night Vision, Aquatic, Jump Boost, Saturation, Fall Guard, Magma Walker, Regrowth, Reflect, Stopping Force |
| `enchants-combat.test.js` | Melee enchants at every level on still mobs, with exact damage: Venom, Withering, Frost, Lifesteal, Execute, Cleave, Beheading, Scavenger, Telekinesis, Wisdom |
| `enchants-bow.test.js` | Bow enchants at every level: Poison Arrow, Hunter, Retrieval (arrows summoned with a set speed), Sniper, Ender Arrow |
| `enchants-tools.test.js` | Tool enchants at every level: Tunnel shapes, Vein Miner and Timber block counts, Harvest areas, Haste, Wisdom; Smelting, Telekinesis, Glass Breaker, Replanter |
| `enchants-fishing.test.js` | Real catches at every level: Auto Reel, Survivalist, Double Catch, Seasoned Angler (each catch's XP must be one the level can give), Long Cast |
| `enchants-misc.test.js` | A custom book clicked on a tool, a Slot Core and a book in the table grid, Curse of Fragility, Soulbound |
| `miniquests-objectives.test.js` | ImusMiniQuests objectives done for real: breaking blocks and grown crops, killing, fishing, crafting, smelting, enchanting, breeding, trading, eating, taming, shearing, walking; wrong targets, placed blocks, creative; FIRST and ALL modes |
| `miniquests-rewards.test.js` | Claiming: progress of an unfinished panel, exact rewards (item, tool tier with slots, custom book, Slot Cores, money, command, message), quest points and luck, locked rewards and unlocking, random rewards, legendary panels, time limits, unplaceable panels, the menu |
| `miniquests-drops.test.js` | Panels from hostile mobs (not animals), listed blocks and loot chests, and none with drops off |
| `challenges.test.js` | ImusChallenges with bots that aren't ops: point commands from the console, the first to pick up a material, advancement points by frame and the first bonus (off by default), creative players not taking part, the leader announcement, the shop (points, slots for money) |

The MiniQuests tests add their own quests (`lib/miniquest-fixture.js`) to the end of the server's `quests.yml`
and take them out again when done.

The enchant tests put enchants straight on items with `/ien enchant` and set ImusEnchants' `testing.fixed-roll`,
the roll chance enchants make instead of a random one. Each chance is checked at every level: a roll just below
it triggers the enchant, the chance itself doesn't. The roll goes back to random (-1) when they are done. The bots
play in survival, where enchants work.

`restart.test.js` stops the server and starts it again in the background (with the Java of
`run/Kaynnista-palvelin.bat`, log in `run/logs/shopbot-server.log`), so it is skipped unless asked for.
Run it after the others, which fill the shops:

```
set SHOPBOT_RESTART=1 && node --test test/restart.test.js
```

## Writing tests

`lib/shopbot.js` has the helpers: `joinServer(name)`, `click(slot, 'left'|'right'|'shiftLeft'|'shiftRight'|'middle')`,
`shopSlotsWith(item)`, `customerSlotsWith(item)`, `count(item)`, `waitForWindow()`, `waitForMessage(regex)`.

The helpers wait for the server instead of sleeping a fixed time. The plugins open a menu first and fill it after,
so a menu that just opened can still be empty: `waitForWindow()`, `nextWindow()` and `clickForNewWindow(slot)`
return the menu only after the server has sent its contents, and `click()` returns after the server has answered
the click (and keeps the shop's 0.2 s click cooldown between clicks). Use `waitForShopSlot(item)`,
`waitForCustomerSlot(item)` and `waitForOwnSlot(item)` for a slot that should be there, and `until(check)` for
anything else that shows up a little later; keep `shopSlotsWith(item)` for checking that something is gone.

`lib/fixture.js` resets a test player, reads balances, sets prices and opens shops. `lib/rcon.js` runs console
commands; use `minecraft:give` for items with components, Essentials' `/give` doesn't understand them. Shop menu slots: 0-26 the shop, 27-35 buttons (33 = hotbar on/off),
36-53 the player's items.
