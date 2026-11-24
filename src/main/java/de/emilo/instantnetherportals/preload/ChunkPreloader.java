package de.emilo.instantnetherportals.preload;

import de.emilo.instantnetherportals.config.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ChunkPreloader implements Listener {

    private static final int CHUNK_RADIUS = 8;
    private static final long CHECK_INTERVAL_TICKS = 20L;
    private static final long TICKET_TICKS = 1200L;
    private static final double COORDINATE_SCALE = 8.0;

    private final Plugin plugin;
    private final ConfigManager config;
    private final boolean paperTickets;
    private final Map<UUID, Location> lastPreloadPosition = new HashMap<>();
    private final Set<String> activeTickets = new HashSet<>();

    public ChunkPreloader(Plugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.paperTickets = detectPaperTickets();
        if (!paperTickets) {
            plugin.getLogger().info("Chunk pre-loading runs in basic async mode (no ticket pinning).");
        }
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::checkPlayers,
                CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS);
    }

    private static boolean detectPaperTickets() {
        try {
            World.class.getMethod("addPluginChunkTicket", int.class, int.class, Plugin.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private void checkPlayers() {
        if (!config.isPreloadEnabled()) return;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            check(player);
        }
    }

    private void check(Player player) {
        int detectionRadius = config.getPreloadRadius();
        Location location = player.getLocation();

        Location last = lastPreloadPosition.get(player.getUniqueId());
        if (last != null && last.getWorld() == location.getWorld()) {
            double threshold = Math.max(4.0, detectionRadius / 2.0);
            if (last.distanceSquared(location) < threshold * threshold) return;
        }

        if (!hasNearbyPortal(location, detectionRadius)) return;

        World destination = destinationWorld(location.getWorld());
        if (destination == null) return;

        loadChunksAround(destination, translate(location));
        lastPreloadPosition.put(player.getUniqueId(), location.clone());
    }

    private boolean hasNearbyPortal(Location location, int radius) {
        World world = location.getWorld();
        int baseX = location.getBlockX();
        int baseY = location.getBlockY();
        int baseZ = location.getBlockZ();
        int radiusSquared = radius * radius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radiusSquared) continue;
                if (!world.isChunkLoaded((baseX + dx) >> 4, (baseZ + dz) >> 4)) continue;
                for (int dy = -radius; dy <= radius; dy++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSquared) continue;
                    if (world.getBlockAt(baseX + dx, baseY + dy, baseZ + dz).getType() == Material.NETHER_PORTAL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void loadChunksAround(World destination, Location center) {
        int centerX = center.getBlockX() >> 4;
        int centerZ = center.getBlockZ() >> 4;
        List<int[]> loaded = new ArrayList<>();

        for (int x = centerX - CHUNK_RADIUS; x <= centerX + CHUNK_RADIUS; x++) {
            for (int z = centerZ - CHUNK_RADIUS; z <= centerZ + CHUNK_RADIUS; z++) {
                if (!activeTickets.add(ticketKey(destination, x, z))) continue;
                loaded.add(new int[]{x, z});
                if (paperTickets) {
                    destination.addPluginChunkTicket(x, z, plugin);
                } else {
                    destination.getChunkAtAsync(x, z);
                }
            }
        }

        if (loaded.isEmpty()) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (int[] chunk : loaded) {
                if (paperTickets) {
                    destination.removePluginChunkTicket(chunk[0], chunk[1], plugin);
                }
                activeTickets.remove(ticketKey(destination, chunk[0], chunk[1]));
            }
        }, TICKET_TICKS);
    }

    private static String ticketKey(World world, int x, int z) {
        return world.getName() + ":" + x + ":" + z;
    }

    private World destinationWorld(World current) {
        World.Environment target = switch (current.getEnvironment()) {
            case NORMAL -> World.Environment.NETHER;
            case NETHER -> World.Environment.NORMAL;
            default -> null;
        };
        if (target == null) return null;
        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() == target) return world;
        }
        return null;
    }

    private Location translate(Location source) {
        World destination = destinationWorld(source.getWorld());
        double scale = source.getWorld().getEnvironment() == World.Environment.NORMAL
                ? 1.0 / COORDINATE_SCALE
                : COORDINATE_SCALE;
        return new Location(destination, source.getX() * scale, source.getY(), source.getZ() * scale);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastPreloadPosition.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        lastPreloadPosition.remove(event.getPlayer().getUniqueId());
    }
}
