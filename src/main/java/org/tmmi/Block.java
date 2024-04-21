package org.tmmi;

import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class Block {
    public static List<Block> blocks = new ArrayList<>();

    private Location loc;
    private final Material material;
    public Block(Material material, Location loc) {
        this.material = material;
        this.loc = loc;
        blocks.add(this);
    }

    public Material getMaterial() {
        return material;
    }

    public void onPlace(@NotNull Location location) {
        this.loc = location;
    }

    public void onBreak(Location location) {
        this.loc = null;
        blocks.remove(this);
    }

    public Location getLoc() {
        return loc;
    }

    protected void setLoc(Location loc) {
        this.loc = loc;
    }
}
