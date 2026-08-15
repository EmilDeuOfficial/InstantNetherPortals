<!-- MODRINTH SUMMARY (paste into Project Settings → Summary, max 256 chars) -->
<!-- Removes the 4 second wait in Nether portals, in both directions. Configurable delay, destination chunks pre-loaded to avoid lag on arrival. Lightweight, no dependencies. -->

## InstantNetherPortals

A lightweight Paper plugin that removes the 4 second wait in Nether portals. Teleportation works in both directions (Overworld to Nether and back), and the delay is configurable if you want something between instant and vanilla.

### Features

- Instant teleportation through Nether portals in both directions
- Configurable delay from 0 ticks (instant) up to 80 ticks (vanilla)
- Destination chunks are pre-loaded before you arrive, so there is no freeze on arrival
- Per-player and per-group permissions, compatible with LuckPerms
- Toggle command, no restart needed
- Config reload without a server restart

### Configuration

```yaml
# Whether instant nether portals are enabled
enabled: true

# Time the player must stand in the portal before teleporting (ticks)
#   0  = instant
#  20  = 1 second
#  40  = 2 seconds
#  80  = vanilla (4 seconds)
teleport-delay: 0

chunk-preload:
  # Pre-load destination chunks when a player is near a portal
  enabled: true
  # Block radius to search for nearby portals
  radius: 10
```

### Permissions

| Permission | Description | Default |
|---|---|---|
| `instantnetherportals.use` | Instant portal teleportation | `op` |
| `instantnetherportals.admin` | Use `/instantportal` command | `op` |

LuckPerms examples:

```
/lp group default permission set instantnetherportals.use true
/lp user Steve permission set instantnetherportals.admin true
```

### Commands

| Command | Description |
|---|---|
| `/instantportal` | Toggle on/off |
| `/instantportal on` / `off` | Enable or disable |
| `/instantportal reload` | Reload config |
| `/instantportal status` | Show status and current delay |

Alias: `/inp`

### Requirements

| | |
|---|---|
| Server (full support) | Paper 1.21.8 (Purpur and Pufferfish also work) |
| Server (loads, no instant portals) | Spigot / Bukkit 1.21.8 |
| Java | 21+ |

Instant portal teleportation requires Paper. On Spigot and Bukkit the plugin loads and chunk pre-loading still works in basic async mode, but the instant teleport itself needs Paper's NMS access.

### How it works

Paper 1.21.8 tracks the portal wait timer in a `PortalProcessor` object. Every server tick the plugin advances that timer to `maxTime - delay`, so the game fires the teleport after exactly `delay` more ticks. At `delay = 0` that takes 1 to 2 ticks, roughly 50 to 100 ms.

While a player is near a portal, the plugin pre-loads a 17x17 chunk area at the destination coordinates before they step in. Without this, the server generates those chunks the moment the player arrives, which is what causes the usual freeze or lag spike.
