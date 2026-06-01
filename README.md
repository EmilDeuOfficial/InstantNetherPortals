# InstantNetherPortals

A lightweight [Paper](https://papermc.io/) plugin for Minecraft **1.21.8** that makes Nether portal teleportation instant (or configurable) in both directions — Overworld → Nether and Nether → Overworld.

## Features

- **Instant teleportation** through Nether portals (both directions)
- **Configurable delay** — set any value from 0 ticks (instant) up to vanilla (80 ticks / 4 seconds)
- **LuckPerms-compatible permissions** — grant per-player or per-group
- **Toggle command** with tab-completion
- Auto-reloads config without server restart

## Requirements

| Requirement | Version |
|-------------|---------|
| Server | Paper 1.21.x |
| Java | 21+ |
| Maven | 3.9+ (build only) |

> **Note:** Only Paper-based servers are supported (Purpur, Pufferfish, etc.). Spigot and Folia are **not** supported.

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

See [CLAUDE.md](CLAUDE.md) for full build documentation.

## How it works

Paper 1.21.8 tracks portal wait time inside a `PortalProcessor` object (`Entity.portalProcess`). Each tick the plugin sets `PortalProcessor.portalTime` to `maxTime - delay` via reflection, so the game fires the portal after exactly `delay` more ticks. With `delay = 0` this happens within 1–2 game ticks (~50–100 ms), which is imperceptible.

## License

MIT
