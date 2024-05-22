package org.tmmi.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public abstract class Block {
    public static Set<Block> instances = new HashSet<>();

    private Location loc;
    private final Material material;
    public Block(Material material, Location loc) {
        this.material = material;
        this.loc = loc;
        instances.add(this);
    }

    public Material getMaterial() {
        return material;
    }

    public void onPlace(@NotNull Location location) {
        this.loc = location;
    }

    public void onBreak(Location location) {
        this.loc = null;
        instances.remove(this);
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public abstract String toJSON();

    protected World getWorld() {
        return loc.getWorld();
    }
}
