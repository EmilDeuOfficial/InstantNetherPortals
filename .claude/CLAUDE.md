# InstantNetherPortals – Project Notes

## Build

**Never run `mvn` directly.** Always use the build script:

```powershell
.\build.ps1
```

The script:
1. Reads the current version from `pom.xml`
2. Runs `mvn clean package`
3. Copies the JAR to `versions/` with the naming convention below
4. Bumps the minor version in `pom.xml` for the next build

## Output naming convention

```
versions/InstantNetherPortals_v{major}.{minor}_{mc-version}.jar
```

Example: `versions/InstantNetherPortals_v1.0_1.21.8.jar`

- **Plugin version** – controlled by `<version>` in `pom.xml`. Auto-incremented (minor) after each successful build.
- **MC version** – controlled by `<mc.version>` property in `pom.xml` (currently `1.21.8`). Change it there when targeting a different Minecraft version.

## Version increment behaviour

- Minor version increments after **every successful** build (`1.0 → 1.1 → 1.2 …`).
- On a **failed** build the version is **not** changed.
- To manually set the version, edit `<version>` in `pom.xml` directly.

## Requirements

| Tool   | Minimum |
|--------|---------|
| Java   | 21      |
| Maven  | 3.9+    |
| Target | Paper 1.21.x |

## Permissions (LuckPerms)

| Node | Purpose |
|------|---------|
| `instantnetherportals.use` | Instant portal teleportation |
| `instantnetherportals.admin` | `/instantportal` command |

## Key files

| File | Purpose |
|------|---------|
| `src/main/resources/config.yml` | `enabled`, `teleport-delay` (ticks) |
| `src/main/java/…/PortalListener.java` | NMS reflection for portal speed |
| `src/main/java/…/PortalCommand.java` | `/instantportal [on\|off\|reload\|status]` |
| `build.ps1` | Build + version bump script |
