package de.emilo.instantnetherportals.portal;

import de.emilo.instantnetherportals.InstantNetherPortals;
import de.emilo.instantnetherportals.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class PortalAccelerator {

    private static final int VANILLA_WAIT_TIME = 80;

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final Logger log;

    private boolean reflectionAttempted;
    private Method getHandle;
    private Field portalProcessField;
    private Field portalTimeField;
    private Field portalCooldownField;
    private Method getPortalWaitTime;

    public PortalAccelerator(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.log = plugin.getLogger();
    }

    public void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    private void tick() {
        if (!config.isEnabled()) return;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!player.hasPermission("instantnetherportals.use")) continue;
            if (!isInNetherPortal(player)) continue;
            accelerate(player);
        }
    }

    private void accelerate(Player player) {
        if (!ensureReflection(player)) return;

        try {
            Object handle = getHandle.invoke(player);
            if (isOnPortalCooldown(handle)) return;

            int waitTime = resolveWaitTime(handle);
            int delay = config.getTeleportDelay();
            if (delay >= waitTime) return;

            Object processor = portalProcessField.get(handle);
            if (processor == null) return;

            int target = waitTime - delay;
            if (portalTimeField.getInt(processor) < target) {
                portalTimeField.setInt(processor, target);
            }
        } catch (ReflectiveOperationException e) {
            log.warning("Could not accelerate portal for " + player.getName() + ": " + e.getMessage());
        }
    }

    private boolean isInNetherPortal(Player player) {
        Block feet = player.getLocation().getBlock();
        return feet.getType() == Material.NETHER_PORTAL
                || feet.getRelative(0, 1, 0).getType() == Material.NETHER_PORTAL;
    }

    private boolean isOnPortalCooldown(Object handle) throws ReflectiveOperationException {
        return portalCooldownField != null && portalCooldownField.getInt(handle) > 0;
    }

    private int resolveWaitTime(Object handle) {
        if (getPortalWaitTime == null) return VANILLA_WAIT_TIME;
        try {
            int value = (int) getPortalWaitTime.invoke(handle);
            return value > 0 ? value : VANILLA_WAIT_TIME;
        } catch (ReflectiveOperationException e) {
            return VANILLA_WAIT_TIME;
        }
    }

    private boolean ensureReflection(Player player) {
        if (reflectionAttempted) return portalTimeField != null;
        reflectionAttempted = true;

        if (!InstantNetherPortals.isPaper()) return false;

        try {
            getHandle = player.getClass().getMethod("getHandle");
            Class<?> entityClass = findEntityClass(getHandle.getReturnType());
            if (entityClass == null) {
                log.severe("Could not locate the Entity class, portal acceleration is disabled.");
                return false;
            }

            portalProcessField = findPortalProcessField(entityClass);
            if (portalProcessField == null) {
                log.severe("Entity.portalProcess not found, unsupported server version.");
                return false;
            }

            portalTimeField = declaredField(portalProcessField.getType(), "portalTime");
            if (portalTimeField == null) {
                log.severe("PortalProcessor.portalTime not found, unsupported server version.");
                return false;
            }

            portalCooldownField = declaredField(entityClass, "portalCooldown");
            getPortalWaitTime = findMethod(entityClass, "getPortalWaitTime");
            log.info("Portal acceleration ready.");
        } catch (ReflectiveOperationException e) {
            log.severe("Reflection setup failed: " + e.getMessage());
            portalTimeField = null;
        }
        return portalTimeField != null;
    }

    private static Class<?> findEntityClass(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (current.getSimpleName().equals("Entity")) return current;
        }
        return null;
    }

    private static Field findPortalProcessField(Class<?> entityClass) {
        Field byName = declaredField(entityClass, "portalProcess");
        if (byName != null) return byName;
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.getType().getSimpleName().equals("PortalProcessor")) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    private static Field declaredField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static Method findMethod(Class<?> owner, String name) {
        for (Class<?> current = owner; current != null; current = current.getSuperclass()) {
            try {
                Method method = current.getDeclaredMethod(name);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }
}
