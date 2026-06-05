## ⚡ InstantNetherPortals

Tired of standing in a Nether portal for 4 seconds? This lightweight plugin makes portal teleportation instant — in **both directions** (Overworld → Nether and Nether → Overworld).

No libraries, no bloat. Drop it in and go.

---

### ✨ Features

- **Instant teleportation** — no more waiting animation
- **Both directions** — Overworld ↔ Nether
- **Configurable delay** — set anywhere from 0 ticks (instant) to 80 ticks (vanilla)
- **Chunk pre-loading** — destination chunks are loaded before you arrive, preventing freeze on arrival
- **Per-player permissions** — fully compatible with LuckPerms
- **Toggle command** — enable/disable without restarting
- **Live reload** — change config without restarting the server

---

### ⚙️ Configuration

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

---

### 🔑 Permissions

| Permission | Description | Default |
|---|---|---|
| `instantnetherportals.use` | Instant portal teleportation | `op` |
| `instantnetherportals.admin` | Use `/instantportal` command | `op` |

**LuckPerms examples**
```
/lp group default permission set instantnetherportals.use true
/lp user Steve permission set instantnetherportals.admin true
```

---

### 💬 Commands

| Command | Description |
|---|---|
| `/instantportal` | Toggle on/off |
| `/instantportal on` / `off` | Enable or disable |
| `/instantportal reload` | Reload config |
| `/instantportal status` | Show status and current delay |

Alias: `/inp`

---

### 📋 Requirements

| | |
|---|---|
| Server (full support) | **Paper 1.21.8** (Purpur & Pufferfish also work) |
| Server (loads, no instant portals) | Spigot / Bukkit 1.21.8 |
| Java | 21+ |

> **Paper** is required for instant portal teleportation. On Spigot/Bukkit the plugin loads and chunk pre-loading works in basic async mode, but the instant teleport feature requires Paper's NMS access.

---

### 🔧 How it works

Paper 1.21.8 tracks the portal wait timer inside a `PortalProcessor` object. Each server tick the plugin advances that timer to `maxTime − delay`, so the game fires the teleport after exactly `delay` more ticks. At `delay = 0` this takes 1–2 ticks (~50–100 ms) — completely imperceptible.

While a player is near a portal, the plugin pre-loads a 17×17 chunk area at the destination coordinates before they step in. This prevents the freeze or lag spike that normally occurs when the server generates destination chunks on arrival.

---

### 📦 Source

[github.com/EmilDeuOfficial/InstantNetherPortals](https://github.com/EmilDeuOfficial/InstantNetherPortals)
