package de.emilo.instantnetherportals;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class PortalListener implements Listener {

    private static final int VANILLA_WAIT_TIME = 80;

    private final InstantNetherPortals plugin;
    private final ConfigManager config;
    private final Logger log;

    // NMS reflection cache (initialised on first player portal entry)
    private boolean reflectionReady     = false;
    private Field   portalProcessField;  // Entity        -> PortalProcessor
    private Field   processorTimeField;  // PortalProcessor -> int portalTime
    private Method  getPortalWaitTimeMethod;
    private Method  getHandleMethod;

    public PortalListener(InstantNetherPortals plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.log    = plugin.getLogger();
        startPortalTicker();
    }

    // -----------------------------------------------------------------------
    // Per-tick portal acceleration (dimension-agnostic)
    // -----------------------------------------------------------------------

    private void startPortalTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!config.isEnabled()) return;
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!player.hasPermission("instantnetherportals.use")) continue;
                    if (player.getPortalCooldown() > 0) continue;
                    if (!isInNetherPortal(player)) continue;
                    acceleratePortal(player);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    /**
     * Sets PortalProcessor.portalTime to (maxTime - delay) every tick.
     * The game increments it naturally; once it reaches maxTime the portal fires.
     * With delay=0 this happens after 1-2 game ticks (~instant).
     */
    private void acceleratePortal(Player player) {
        if (!ensureReflection(player)) return;

        try {
            Object nms = getHandleMethod.invoke(player);
            Object pp  = portalProcessField.get(nms); // PortalProcessor instance
            if (pp == null) return;                    // Created next tick by the game

            int maxTime = resolveMaxTime(nms);
            int delay   = config.getTeleportDelay();
            if (delay >= maxTime) return;

            int currentTime = processorTimeField.getInt(pp);
            int targetTime  = maxTime - delay;

            if (currentTime < targetTime) {
                processorTimeField.setInt(pp, targetTime);
            }
        } catch (Exception e) {
            log.warning("[INP] Could not accelerate portal for "
                    + player.getName() + ": " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private boolean isInNetherPortal(Player player) {
        var feet  = player.getLocation().getBlock();
        var torso = feet.getRelative(0, 1, 0);
        return feet.getType()  == Material.NETHER_PORTAL
            || torso.getType() == Material.NETHER_PORTAL;
    }

    // -----------------------------------------------------------------------
    // NMS reflection (pure reflection – no NMS imports needed)
    // -----------------------------------------------------------------------

    private boolean ensureReflection(Player player) {
        if (reflectionReady) return processorTimeField != null;
        reflectionReady = true;
        try {
            getHandleMethod = player.getClass().getDeclaredMethod("getHandle");
            getHandleMethod.setAccessible(true);
            Object nms = getHandleMethod.invoke(player);

            // Locate the base NMS Entity class
            Class<?> entityClass = findNmsEntityClass(nms.getClass());
            if (entityClass == null) {
                log.severe("[INP] Could not locate NMS Entity class.");
                return false;
            }

            getPortalWaitTimeMethod = findMethod(nms.getClass(), "getPortalWaitTime");

            // In Paper 1.21.8 portalTime lives inside PortalProcessor
            portalProcessField = getDeclaredFieldSafe(entityClass, "portalProcess");
            if (portalProcessField == null) {
                log.severe("[INP] Entity.portalProcess not found – unsupported server version.");
                return false;
            }

            Class<?> ppClass   = portalProcessField.getType();
            processorTimeField = getDeclaredFieldSafe(ppClass, "portalTime");
            if (processorTimeField == null) {
                log.severe("[INP] PortalProcessor.portalTime not found.");
                return false;
            }

            log.info("[INP] Reflection ready (Paper 1.21.8 PortalProcessor API).");
        } catch (Exception e) {
            log.severe("[INP] Reflection init failed: " + e.getMessage());
            processorTimeField = null;
        }
        return processorTimeField != null;
    }

    private int resolveMaxTime(Object nms) {
        if (getPortalWaitTimeMethod == null) return VANILLA_WAIT_TIME;
        try {
            int v = (int) getPortalWaitTimeMethod.invoke(nms);
            return v > 0 ? v : VANILLA_WAIT_TIME;
        } catch (Exception e) {
            return VANILLA_WAIT_TIME;
        }
    }

    // -----------------------------------------------------------------------
    // Reflection utilities
    // -----------------------------------------------------------------------

    private static Class<?> findNmsEntityClass(Class<?> cls) {
        while (cls != null) {
            if (cls.getSimpleName().equals("Entity")) return cls;
            cls = cls.getSuperclass();
        }
        return null;
    }

    private static Field getDeclaredFieldSafe(Class<?> cls, String name) {
        try {
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    private static Method findMethod(Class<?> cls, String name, Class<?>... p) {
        while (cls != null) {
            try { Method m = cls.getDeclaredMethod(name, p); m.setAccessible(true); return m; }
            catch (NoSuchMethodException ignored) { cls = cls.getSuperclass(); }
        }
        return null;
    }
}
