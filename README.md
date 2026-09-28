# Imu Minecraft Plugins

Spigot plugins for the Drevaria server. One Maven reactor: `ImusAPI` is the
shared library, every plugin builds against it, so an API change and the
plugins that use it move in a single commit.

## Layout

```
pom.xml            parent POM: shared versions, repositories, plugin config
libs/ImusAPI       shared library, published as me.imu:ImusAPI
plugins/*          one Maven module per plugin
archive/           older plugins and experiments, kept as source, not built
```

The modules that are built are the ones actually running on the 1.20.1
server: `ImusAPI`, `DontLoseItems`, `ImusChallenges`, `ImusEnchants`,
`imusGS`, `imusSpawners`, `imusTNT`, `imusWaystones`, plus `imusMcCards`.

## Prerequisites

- JDK 17. Minecraft 1.20.2 needs it, and some of this code already uses text
  blocks and switch rules.
- Maven 3.9+.
- Four artifacts that no public repository serves, installed into `~/.m2`:
  `org.spigotmc:spigot:1.20.2-R0.1-SNAPSHOT` (produced by Spigot's BuildTools,
  needed for the `net.minecraft` and `craftbukkit` classes),
  `com.github.dmulloy2:ProtocolLib:5.1.0`,
  `com.github.MilkBowl:VaultAPI:1.7` and
  `com.magmaguy:BetterStructures:1.6.4`. See "Installing the local artifacts".

## Build

```
mvn clean package
```

Jars land in each module's `target/`. To build straight into a server's
plugins folder instead:

```
mvn clean package -Dplugin.output.dir=E:/McServerThings/Server1.20.1/plugins
```

That path used to be hardcoded in every POM; it is now the
`plugin.output.dir` property, so the build works on any machine.

To build one plugin and the library it needs:

```
mvn -pl plugins/imusTNT -am package
```

### Build status

Eight of the nine modules compile. `DontLoseItems` does not, and did not
before this restructure either — it is stale against the current ImusAPI:

- it declares `ImusLootTable<ItemStack>`, but `ImusLootTable` is no longer
  generic (it holds `List<ILootTableItem<?>>` instead), and
- `Inv_SelectDifficulty` does not implement `ICustomInventory.onAwake()`,
  which the interface gained later.

The `DontLoseItems.jar` running on the server was built in December 2023,
against the ImusAPI of that time. Updating the plugin to the current API is a
real change to its loot handling, so it is left as is rather than guessed at.

## CI

`.github/workflows/build.yml` builds every module except `DontLoseItems` on each
push and pull request, and uploads the plugin jars as the `plugins` artifact of
the run (Actions tab → the run → Artifacts). It sets up the local artifacts on
its own: Spigot through BuildTools (cached between runs), ProtocolLib and Vault
from their GitHub releases, and a compile-only stub of BetterStructures'
`ChestFillEvent` from `.github/ci-stubs/`, since that plugin isn't published
anywhere. The stub is a provided dependency and never ends up in a jar.

## Installing the local artifacts

`org.spigotmc:spigot` is the remapped server jar; build it once with Spigot's
BuildTools, which installs it (plus `spigot-parent` and `minecraft-server`)
into `~/.m2`:

```
java -jar BuildTools.jar --rev 1.20.2 --remapped
```

The other three are plugin jars installed by hand. `BetterStructures` ships
with the server:

```
mvn install:install-file -Dfile=E:/McServerThings/Server1.20.1/plugins/BetterStructures.jar -DgroupId=com.magmaguy -DartifactId=BetterStructures -Dversion=1.6.4 -Dpackaging=jar
```

`ProtocolLib` and `VaultAPI` are installed the same way, as
`com.github.dmulloy2:ProtocolLib:5.1.0` and
`com.github.MilkBowl:VaultAPI:1.7`.

Note that VaultAPI 1.7 declares a dependency on `org.bukkit:bukkit:1.13.1`,
which shadows spigot-api on the compile classpath and hides everything added
to `Material`, `EntityType` and `ItemMeta` since 1.13. The parent POM excludes
it. The old per-plugin POMs only worked because they happened to list
spigot-api before VaultAPI.

## Archive

`archive/` holds plugins that are no longer deployed: `imusCasino`,
`imusGeneralStore`, `imusMiniGames`, `imusCards`, `imusFishing`, `HomeTele`,
`TokenTp`, `imusAntiCheat`, `imusDEFAULTplate` (the boilerplate the others
were copied from) and `Old Versions/`. They have no POM and are not part of
the reactor — they are kept so the code is not lost. `archive/imusEnchants`
is the pre-Maven Eclipse version of `plugins/ImusEnchants`; it still holds
`EnchantShard` and `IEnchant`, which the Maven version dropped.



