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

The bot joins as `ShopBot`, creates the shop `BotTest` if it doesn't exist and trades with it. The tests
look at amounts and balances before and after each click, so they can be run again on the same server.

## Writing tests

`lib/shopbot.js` has the helpers: `joinServer(name)`, `click(slot, 'left'|'right'|'shiftLeft'|'shiftRight'|'middle')`,
`shopSlotsWith(item)`, `customerSlotsWith(item)`, `count(item)`, `waitForWindow()`, `waitForMessage(regex)`.
`lib/rcon.js` runs console commands. Shop menu slots: 0-26 the shop, 27-35 buttons (33 = hotbar on/off),
36-53 the player's items.
