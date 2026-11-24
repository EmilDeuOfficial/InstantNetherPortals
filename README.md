# InstantNetherPortals

Removes the 4 second wait in Nether portals. Teleportation works in both directions (Overworld to Nether and back), and the delay is configurable if you want something between instant and vanilla.

Lightweight, no dependencies. Destination chunks are loaded before the player steps through, so there is no freeze on arrival.

## Features

- Instant teleportation through Nether portals in both directions
- Configurable delay from 0 ticks (instant) up to 80 ticks (vanilla)
- Destination chunks are pre-loaded and pinned for 60 seconds while a player is near a portal
- Per-player and per-group permissions, compatible with LuckPerms
- Toggle command with tab completion, config reload without a restart
- Languages: en, de, ru, es, fr, ja, zh
- Builds for Paper 26.x and Paper 1.21.x

## Configuration

`plugins/InstantNetherPortals/config.yml`:

```yaml
language: en
enabled: true

# 0 = instant, 20 = 1 second, 80 = vanilla (4 seconds in survival)
teleport-delay: 0

chunk-preload:
  enabled: true
  radius: 10
```

## Permissions

| Permission | Description | Default |
|---|---|---|
| `instantnetherportals.use` | Instant portal teleportation | `op` |
| `instantnetherportals.admin` | Use `/instantportal` | `op` |

LuckPerms examples:

```
/lp group default permission set instantnetherportals.use true
/lp user Steve permission set instantnetherportals.admin true
```

## Commands

| Command | Description |
|---|---|
| `/instantportal` | Toggle on or off |
| `/instantportal on` | Enable |
| `/instantportal off` | Disable |
| `/instantportal status` | Show current state and delay |
| `/instantportal reload` | Reload `config.yml` and the language file |

Alias: `/inp`

## Requirements

| | |
|---|---|
| Server | Paper 26.1 to 26.3 with `InstantNetherPortals_1.14_26.x.jar`, Paper 1.21 to 1.21.11 with `InstantNetherPortals_1.14_1.21.x.jar` (Purpur and Pufferfish should work as Paper forks) |
| Java | 25+ for Paper 26.x, 21+ for Paper 1.21.x |

Instant portals need Paper. On Spigot and Bukkit the plugin loads and pre-loads chunks in basic async mode, but portals keep the vanilla delay. Folia is not supported.

## Installation

1. Copy the JAR that matches your server into the `plugins/` folder: `InstantNetherPortals_1.14_26.x.jar` for Paper 26.x or `InstantNetherPortals_1.14_1.21.x.jar` for Paper 1.21.x.
2. Start the server. `config.yml` is created in `plugins/InstantNetherPortals/`.
3. Give your players the `instantnetherportals.use` permission.

## Building

```
mvn package
```

The JAR is written to `target/InstantNetherPortals-1.14.jar`. Maven needs a JDK 25 or newer. The default build targets Paper 26.x with Java 25. For Paper 1.21.x use the profile `paper-1.21`, which compiles against Paper API 1.21.11 for Java 21 and sets `api-version: '1.21'`:

```
mvn package -Ppaper-1.21
```

The Paper API version is set by the `paper.version` property in `pom.xml`.

## How it works

The game tracks the portal wait time in a `PortalProcessor` object on the player entity. Every tick the plugin reads that object through reflection and raises `portalTime` to `waitTime - delay`, so the game fires the teleport after exactly `delay` more ticks. At `delay = 0` that takes 1 to 2 ticks, roughly 50 to 100 ms. The field is looked up by name and, if it was renamed, by its type, and the plugin disables the acceleration with a console message when neither is found.

While a player is near a portal, the plugin pre-loads a 17x17 chunk area at the destination coordinates (x8 for Overworld to Nether, /8 for Nether to Overworld) and pins it with `World.addPluginChunkTicket` for 60 seconds. Without this, the server generates those chunks the moment the player arrives, which is what causes the usual freeze. The portal search only looks at chunks that are already loaded, so it never triggers a synchronous disk read.

On Spigot and Bukkit the NMS access is skipped and chunks are loaded with `getChunkAtAsync` without pinning.

## Source

[github.com/EmilDeuOfficial/InstantNetherPortals](https://github.com/EmilDeuOfficial/InstantNetherPortals)
