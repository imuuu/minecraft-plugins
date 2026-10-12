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
| `restart.test.js` | Shops, stock, prices, sell limits, flash deals and wanted requests are the same after the server restarts |

`restart.test.js` stops the server and starts it again in the background (with the Java of
`run/Kaynnista-palvelin.bat`, log in `run/logs/shopbot-server.log`), so it is skipped unless asked for.
Run it after the others, which fill the shops:

```
set SHOPBOT_RESTART=1 && node --test test/restart.test.js
```

## Writing tests

`lib/shopbot.js` has the helpers: `joinServer(name)`, `click(slot, 'left'|'right'|'shiftLeft'|'shiftRight'|'middle')`,
`shopSlotsWith(item)`, `customerSlotsWith(item)`, `count(item)`, `waitForWindow()`, `waitForMessage(regex)`.
`lib/fixture.js` resets a test player, reads balances, sets prices and opens shops. `lib/rcon.js` runs console
commands; use `minecraft:give` for items with components, Essentials' `/give` doesn't understand them. Shop menu slots: 0-26 the shop, 27-35 buttons (33 = hotbar on/off),
36-53 the player's items.
