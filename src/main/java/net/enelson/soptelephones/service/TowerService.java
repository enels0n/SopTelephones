package net.enelson.soptelephones.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.model.Tower;
import net.enelson.soptelephones.storage.StorageManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class TowerService {
    private final SopTelephonesPlugin plugin;
    private final StorageManager storageManager;
    private final Map<String, Tower> towers = new LinkedHashMap<String, Tower>();

    public TowerService(SopTelephonesPlugin plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
        reload();
    }

    public void reload() {
        this.towers.clear();
        YamlConfiguration config = this.storageManager.getTowersConfig();
        ConfigurationSection section = config.getConfigurationSection("towers");
        if (section == null) {
            return;
        }
        for (String towerId : section.getKeys(false)) {
            String base = "towers." + towerId;
            Tower tower = new Tower(
                towerId,
                config.getString(base + ".provider-id", ""),
                config.getString(base + ".world", "world"),
                config.getDouble(base + ".x"),
                config.getDouble(base + ".y"),
                config.getDouble(base + ".z"),
                config.getDouble(base + ".coverage-radius", 128.0D),
                config.getDouble(base + ".link-radius", 256.0D)
            );
            this.towers.put(towerId.toLowerCase(), tower);
        }
    }

    public Tower addTower(String towerId, String providerId, String world, double x, double y, double z, double coverageRadius, double linkRadius) {
        Tower tower = new Tower(towerId, providerId, world, x, y, z, coverageRadius, linkRadius);
        this.towers.put(towerId.toLowerCase(), tower);
        save();
        return tower;
    }

    public boolean isCovered(String providerId, Location location) {
        return isCovered(providerId, location, 0.0D);
    }

    public boolean isCovered(String providerId, Location location, double signalBonus) {
        if (providerId == null || location == null || location.getWorld() == null) {
            return false;
        }
        for (Tower tower : getActiveTowers(providerId)) {
            if (!tower.isSameWorld(location)) {
                continue;
            }
            double coverageRadius = Math.max(0.0D, tower.getCoverageRadius() + signalBonus);
            if (distanceSquared(tower, location) <= coverageRadius * coverageRadius) {
                return true;
            }
        }
        return false;
    }

    public List<Tower> getActiveTowers(String providerId) {
        List<Tower> providerTowers = getProviderTowers(providerId);
        List<Tower> active = new ArrayList<Tower>();
        if (providerTowers.isEmpty()) {
            return active;
        }

        Location core = getCoreLocation();
        double coreRadius = this.plugin.getConfig().getDouble("core.radius", 128.0D);
        ArrayDeque<Tower> queue = new ArrayDeque<Tower>();
        Set<String> visited = new HashSet<String>();

        for (Tower tower : providerTowers) {
            if (!tower.isSameWorld(core)) {
                continue;
            }
            if (distanceSquared(tower, core) <= coreRadius * coreRadius) {
                queue.add(tower);
                visited.add(tower.getId().toLowerCase());
                active.add(tower);
            }
        }

        while (!queue.isEmpty()) {
            Tower current = queue.poll();
            for (Tower tower : providerTowers) {
                if (visited.contains(tower.getId().toLowerCase())) {
                    continue;
                }
                if (!tower.getWorld().equalsIgnoreCase(current.getWorld())) {
                    continue;
                }
                double maxLink = Math.max(current.getLinkRadius(), tower.getLinkRadius());
                if (distanceSquared(current, tower) > maxLink * maxLink) {
                    continue;
                }
                visited.add(tower.getId().toLowerCase());
                active.add(tower);
                queue.add(tower);
            }
        }

        return active;
    }

    private List<Tower> getProviderTowers(String providerId) {
        List<Tower> result = new ArrayList<Tower>();
        for (Tower tower : this.towers.values()) {
            if (tower.getProviderId().equalsIgnoreCase(providerId)) {
                result.add(tower);
            }
        }
        return result;
    }

    private Location getCoreLocation() {
        String worldName = this.plugin.getConfig().getString("core.world", "world");
        World world = this.plugin.getServer().getWorld(worldName);
        return world == null ? null : new Location(
            world,
            this.plugin.getConfig().getDouble("core.x", 0.0D),
            this.plugin.getConfig().getDouble("core.y", 64.0D),
            this.plugin.getConfig().getDouble("core.z", 0.0D)
        );
    }

    private double distanceSquared(Tower first, Tower second) {
        double dx = first.getX() - second.getX();
        double dy = first.getY() - second.getY();
        double dz = first.getZ() - second.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private double distanceSquared(Tower tower, Location location) {
        double dx = tower.getX() - location.getX();
        double dy = tower.getY() - location.getY();
        double dz = tower.getZ() - location.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    public void save() {
        YamlConfiguration config = this.storageManager.getTowersConfig();
        config.set("towers", null);
        for (Tower tower : this.towers.values()) {
            String base = "towers." + tower.getId();
            config.set(base + ".provider-id", tower.getProviderId());
            config.set(base + ".world", tower.getWorld());
            config.set(base + ".x", tower.getX());
            config.set(base + ".y", tower.getY());
            config.set(base + ".z", tower.getZ());
            config.set(base + ".coverage-radius", tower.getCoverageRadius());
            config.set(base + ".link-radius", tower.getLinkRadius());
        }
        this.storageManager.saveTowers();
    }
}
