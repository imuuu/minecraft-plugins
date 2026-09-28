# Imu Minecraft Plugins

Paper plugins for the Drevaria server, targeting **Minecraft 26.2** (Paper
26.2, Java 25). One Maven reactor: `ImusAPI` is the shared library, every
plugin builds against it, so an API change and the plugins that use it move
in a single commit.

## Layout

```
pom.xml            parent POM: shared versions, repositories, plugin config
libs/ImusAPI       shared library, published as me.imu:ImusAPI
plugins/*          one Maven module per plugin
archive/           older plugins and experiments, kept as source, not built
run/               local test server (git-ignored, see below)
```

The modules that are built are the ones that were running on the old 1.20.1
server: `ImusAPI`, `DontLoseItems`, `ImusChallenges`, `ImusEnchants`,
`imusGS`, `imusSpawners`, `imusTNT`, `imusWaystones`, plus `imusMcCards`.

## Prerequisites

- JDK 25 (Paper 26.x's minimum).
- Maven 3.9+.
- `com.magmaguy:BetterStructures:2.7.4` in `~/.m2` — it is not published to
  any Maven repository. Install it from the plugin jar:

  ```
  mvn install:install-file -Dfile=run/plugins/BetterStructures.jar -DgroupId=com.magmaguy -DartifactId=BetterStructures -Dversion=2.7.4 -Dpackaging=jar
  ```

  Everything else resolves from the Paper repository, JitPack (VaultAPI) and
  Maven Central.

## Build

```
mvn clean package
```

Jars land in each module's `target/`. To build straight into a server's
plugins folder instead:

```
mvn clean package -Dplugin.output.dir=path/to/server/plugins
```

The shade plugin leaves an `original-<name>.jar` next to each plugin jar;
copy only the shaded ones, or Paper will try to load every plugin twice.

To build one plugin and the library it needs:

```
mvn -pl plugins/imusTNT -am package
```

To move to a newer Minecraft, change `paper.version` in `pom.xml` to a
`*-stable` build from repo.papermc.io and the `api-version` in each
`plugin.yml`.

## Server requirements

Other plugins, all loaded as `depend`/`softdepend` in `plugin.yml`:

| Plugin | Needed by | Tested with |
|---|---|---|
| Vault + an economy (EssentialsX) | imusAPI, imusGS | Vault 1.7.3, EssentialsX 2.22.1-dev |
| BetterStructures (needs WorldEdit) | DontLoseItems, ImusEnchants | 2.7.4, WorldEdit 7.4.6-beta-02 |

ProtocolLib is no longer needed: its uses were replaced with Paper API.

**imusAPI requires a MySQL/MariaDB server.** It has no fallback: without a
database its `onEnable` fails, and every plugin that depends on it fails to
load. Connection settings are in `plugins/imusAPI/config.yml` (defaults:
`localhost:3306`, user `root`, empty password). ImusChallenges, imusGS and
imusWaystones create their own databases on the same server.

## Local test server

`run/` holds a Paper 26.2 server with the plugins above, bound to
`127.0.0.1:25599` in offline mode. It is git-ignored. With a database
listening on 3306 (for example XAMPP's MariaDB started against the private
datadir in `run/mariadb-data`):

```
F:/xampp/mysql/bin/mysqld.exe --defaults-file=run/mariadb-data/my.ini --console
```

build, copy the jars and start the server:

```
cd run
java -Xms1G -Xmx2G -jar paper.jar --nogui
```

## Known leftovers

- `org.bukkit.conversations` (ConversationFactory, Prompt, ...) is marked for
  removal in Paper. It still works on 26.2 and is used in 12 files across
  ImusAPI, imusGS and imusWaystones; its replacement, Paper's Dialog API, is
  a different UI, so the prompts were left as they are.
- Paper nags about `System.out.println` in five plugins. Cosmetic.

## Porting notes (1.20.2 Spigot → 26.2 Paper)

- 174 Bukkit constant renames (`Enchantment.DURABILITY` → `UNBREAKING`,
  `Attribute.GENERIC_*` → no prefix, `Particle.REDSTONE` → `DUST`, ...).
- `AttributeModifier(UUID, String, ...)` → `AttributeModifier(NamespacedKey,
  ..., EquipmentSlotGroup)`. Each modifier still gets a random, unique key:
  in 1.21+ modifiers with the same key replace each other.
- NMS and ProtocolLib replaced with Paper API: `BlockState.copy(Location)`
  for tile-entity copying, `setVisibleByDefault` and `swingMainHand` for the
  throwing axe.
- `new ItemStack(material)` now throws for block-only (`*_WALL_HANGING_SIGN`)
  and `LEGACY_*` materials; loops over `Material.values()` skip them.
- VaultAPI 1.7 declares a dependency on `org.bukkit:bukkit:1.13.1`, which
  would shadow paper-api on the compile classpath. The parent POM excludes it.
- javac runs forked: in-process javac 25 crashes while formatting a
  deprecation warning on DontLoseItems.

## Archive

`archive/` holds plugins that are no longer deployed: `imusCasino`,
`imusGeneralStore`, `imusMiniGames`, `imusCards`, `imusFishing`, `HomeTele`,
`TokenTp`, `imusAntiCheat`, `imusDEFAULTplate` (the boilerplate the others
were copied from) and `Old Versions/`. They have no POM and are not part of
the reactor — they are kept so the code is not lost. `archive/imusEnchants`
is the pre-Maven Eclipse version of `plugins/ImusEnchants`; it still holds
`EnchantShard` and `IEnchant`, which the Maven version dropped.



