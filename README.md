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

## Dependency that is not on a public repo

`com.magmaguy:BetterStructures:1.6.4` (used by `DontLoseItems` and
`ImusEnchants`) does not resolve from Maven Central or the Spigot repo. It
has to be installed into the local repository once, from the jar that ships
with the server:

```
mvn install:install-file -Dfile=E:/McServerThings/Server1.20.1/plugins/BetterStructures.jar -DgroupId=com.magmaguy -DartifactId=BetterStructures -Dversion=1.6.4 -Dpackaging=jar
```

## Archive

`archive/` holds plugins that are no longer deployed: `imusCasino`,
`imusGeneralStore`, `imusMiniGames`, `imusCards`, `imusFishing`, `HomeTele`,
`TokenTp`, `imusAntiCheat`, `imusDEFAULTplate` (the boilerplate the others
were copied from) and `Old Versions/`. They have no POM and are not part of
the reactor — they are kept so the code is not lost. `archive/imusEnchants`
is the pre-Maven Eclipse version of `plugins/ImusEnchants`; it still holds
`EnchantShard` and `IEnchant`, which the Maven version dropped.

## Where this came from

This repository is `github.com/imuuu/Java`, restructured. Two things were
recovered from local Eclipse workspaces that had never been pushed:

- `imusTNT` — a wrong `Listener` import and a commented-out `registerEvents`
  call, fixed on disk 2023-12-09 but never committed.
- `imusAntiCheat` and `imusDEFAULTplate` — projects that existed only in
  `E:\Users\kajan\eclipse-workspace` on an old Windows install.

The original working copies were left in place on E: and F:; nothing was
moved or deleted off those drives.
