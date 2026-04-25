package net.enelson.soptelephones.model;

import org.bukkit.Location;

public final class Tower {
    private final String id;
    private final String providerId;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final double coverageRadius;
    private final double linkRadius;

    public Tower(String id, String providerId, String world, double x, double y, double z, double coverageRadius, double linkRadius) {
        this.id = id;
        this.providerId = providerId;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.coverageRadius = coverageRadius;
        this.linkRadius = linkRadius;
    }

    public String getId() {
        return id;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getCoverageRadius() {
        return coverageRadius;
    }

    public double getLinkRadius() {
        return linkRadius;
    }

    public boolean isSameWorld(Location location) {
        return location != null && location.getWorld() != null && this.world.equalsIgnoreCase(location.getWorld().getName());
    }
}

