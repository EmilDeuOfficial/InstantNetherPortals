<!-- MODRINTH SUMMARY (paste into Project Settings → Summary, max 256 chars) -->
<!-- Removes the 4 second wait in Nether portals, in both directions, with a configurable delay and destination chunks pre-loaded to avoid lag on arrival. For Paper 26.x and 1.21.x. Lightweight, no dependencies. -->

## InstantNetherPortals

Removes the 4 second wait in Nether portals. Teleportation works in both directions (Overworld to Nether and back), and the delay is configurable if you want something between instant and vanilla.

Lightweight, no dependencies. Destination chunks are loaded before the player steps through, so there is no freeze on arrival.

### Features

- Instant teleportation through Nether portals in both directions
- Configurable delay from 0 ticks (instant) up to 80 ticks (vanilla)
- Destination chunks are pre-loaded and pinned for 60 seconds while a player is near a portal
- Per-player and per-group permissions, compatible with LuckPerms
- Toggle command with tab completion, config reload without a restart
- Languages: en, de, ru, es, fr, ja, zh
- Builds for Paper 26.x and Paper 1.21.x

### Configuration

```yaml
language: en
enabled: true

# 0 = instant, 20 = 1 second, 80 = vanilla (4 seconds in survival)
teleport-delay: 0

chunk-preload:
  enabled: true
  radius: 10
```

### Permissions

| Permission | Description | Default |
|---|---|---|
| `instantnetherportals.use` | Instant portal teleportation | `op` |
| `instantnetherportals.admin` | Use `/instantportal` | `op` |

LuckPerms examples:

```
/lp group default permission set instantnetherportals.use true
/lp user Steve permission set instantnetherportals.admin true
```

### Commands

| Command | Description |
|---|---|
| `/instantportal` | Toggle on or off |
| `/instantportal on` | Enable |
| `/instantportal off` | Disable |
| `/instantportal status` | Show current state and delay |
| `/instantportal reload` | Reload `config.yml` and the language file |

Alias: `/inp`

### Requirements

| | |
|---|---|
| Server | Paper 26.1 to 26.3 with the `26.x` file, Paper 1.21 to 1.21.11 with the `1.21.x` file (Purpur and Pufferfish should work as Paper forks) |
| Java | 25+ for Paper 26.x, 21+ for Paper 1.21.x |

Download the file that matches your server and drop it into the `plugins/` folder. Instant portals need Paper. On Spigot and Bukkit the plugin loads and pre-loads chunks in basic async mode, but portals keep the vanilla delay. Folia is not supported.

### How it works

The game tracks the portal wait time in a `PortalProcessor` object on the player entity. Every tick the plugin reads that object through reflection and raises `portalTime` to `waitTime - delay`, so the game fires the teleport after exactly `delay` more ticks. At `delay = 0` that takes 1 to 2 ticks, roughly 50 to 100 ms.

While a player is near a portal, the plugin pre-loads a 17x17 chunk area at the destination coordinates (x8 for Overworld to Nether, /8 for Nether to Overworld) and pins it with `World.addPluginChunkTicket` for 60 seconds. Without this, the server generates those chunks the moment the player arrives, which is what causes the usual freeze.
