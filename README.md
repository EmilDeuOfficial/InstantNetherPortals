# InstantNetherPortals

A lightweight Paper plugin for Minecraft 1.21.8 that removes the 4 second wait in Nether portals. Teleportation works in both directions (Overworld to Nether and back), and the delay is configurable if you want something between instant and vanilla.

## Features

- Instant teleportation through Nether portals in both directions
- Configurable delay from 0 ticks (instant) up to 80 ticks (vanilla)
- Destination chunks are pre-loaded before the player arrives, so there is no freeze on arrival
- Per-player and per-group permissions, compatible with LuckPerms
- Toggle command with tab completion
- Config reload without a server restart

## Requirements

| Server | Support |
|--------|---------|
| Paper 1.21.8 | Full (instant portals and chunk pre-loading) |
| Purpur / Pufferfish | Full |
| Spigot / Bukkit 1.21.8 | Loads, chunk pre-loading in basic mode, no instant portals |

| Other requirement | Version |
|-------------------|---------|
| Java | 21+ |
| Maven | 3.9+ (build only) |

Instant portal teleportation requires Paper. On Spigot and Bukkit the plugin still loads, but the NMS features are disabled and portals use the vanilla 4 second delay.

## Installation

1. Download the latest JAR from [Releases](../../releases)
2. Drop it into your server's `plugins/` folder
3. Restart the server
4. Grant the `instantnetherportals.use` permission to your players

## Configuration

`plugins/InstantNetherPortals/config.yml`:

```yaml
# Whether instant nether portals are enabled
enabled: true

# How long a player must stand in the portal before teleporting (in ticks)
#   0  = instant
#  20  = 1 second
#  40  = 2 seconds
#  80  = vanilla default (4 seconds in survival)
teleport-delay: 0

chunk-preload:
  # Pre-load destination chunks when a player is near a portal
  enabled: true
  # Block radius to search for nearby portals
  radius: 10
```

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `instantnetherportals.use` | Instant portal teleportation | `op` |
| `instantnetherportals.admin` | Use the `/instantportal` command | `op` |

LuckPerms examples:

```
/lp group default permission set instantnetherportals.use true
/lp user Steve permission set instantnetherportals.admin true
```

## Commands

| Command | Description |
|---------|-------------|
| `/instantportal` | Toggle on/off |
| `/instantportal on` | Enable |
| `/instantportal off` | Disable |
| `/instantportal reload` | Reload config |
| `/instantportal status` | Show current status and delay |

Alias: `/inp`

## Building

```powershell
.\build.ps1
```

The script compiles the plugin, copies the JAR to `versions/` using the naming convention
`InstantNetherPortals_v{major}.{minor}_{mc-version}.jar`, and bumps the minor version in `pom.xml`.

## How it works

Paper 1.21.8 tracks the portal wait time in a `PortalProcessor` object (`Entity.portalProcess`). Every tick the plugin sets `PortalProcessor.portalTime` to `maxTime - delay` via reflection, so the game fires the portal after exactly `delay` more ticks. At `delay = 0` that happens within 1 to 2 game ticks (roughly 50 to 100 ms), which nobody notices.

While a player is near a portal, the plugin pre-loads a 17x17 chunk area at the translated destination coordinates (x8 for Overworld to Nether, /8 for Nether to Overworld). Without this, the server generates those chunks the moment the player arrives, which is what causes the usual freeze.

On Spigot and Bukkit the NMS field names are obfuscated and differ per version. The plugin detects this at startup, disables the NMS-dependent features and logs a warning instead of failing.

## Source

[github.com/EmilDeuOfficial/InstantNetherPortals](https://github.com/EmilDeuOfficial/InstantNetherPortals)

## License

MIT
