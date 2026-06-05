# InstantNetherPortals

A lightweight plugin for Minecraft **1.21.8** that makes Nether portal teleportation instant (or configurable) in both directions — Overworld → Nether and Nether → Overworld.

## Features

- **Instant teleportation** through Nether portals (both directions)
- **Configurable delay** — set any value from 0 ticks (instant) up to vanilla (80 ticks / 4 seconds)
- **Chunk pre-loading** — destination chunks are loaded before the player arrives, preventing freeze on arrival
- **LuckPerms-compatible permissions** — grant per-player or per-group
- **Toggle command** with tab-completion
- Live reload config without server restart

## Requirements

| Server | Support |
|--------|---------|
| Paper 1.21.8 | Full (instant portals + chunk pre-loading) |
| Purpur / Pufferfish | Full |
| Spigot / Bukkit 1.21.8 | Loads — chunk pre-loading (basic mode), no instant portals |

| Other requirement | Version |
|-------------------|---------|
| Java | 21+ |
| Maven | 3.9+ (build only) |

> **Instant portal teleportation requires Paper.** On Spigot/Bukkit the plugin loads safely but NMS features are disabled — portals use the vanilla 4-second delay.

## Installation

1. Download the latest JAR from [Releases](../../releases)
2. Drop it into your server's `plugins/` folder
3. Restart the server
4. Assign the `instantnetherportals.use` permission to players (see below)

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

**LuckPerms examples:**
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
`InstantNetherPortals_v{major}.{minor}_{mc-version}.jar`, and auto-increments the minor version in `pom.xml`.

See [CLAUDE.md](.claude/CLAUDE.md) for full build documentation.

## How it works

Paper 1.21.8 tracks portal wait time inside a `PortalProcessor` object (`Entity.portalProcess`). Each tick the plugin sets `PortalProcessor.portalTime` to `maxTime - delay` via reflection, so the game fires the portal after exactly `delay` more ticks. With `delay = 0` this happens within 1–2 game ticks (~50–100 ms), which is imperceptible.

While a player is near a portal, the plugin pre-loads a 17×17 chunk area at the translated destination coordinates (`×8` OW→Nether, `÷8` Nether→OW) before they step in, preventing the freeze that occurs when destination chunks are generated on arrival.

On Spigot/Bukkit, the NMS field names are obfuscated and differ per version — the plugin detects this at startup, disables NMS-dependent features, and logs a warning.

## License

MIT
