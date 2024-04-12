package org.tmmi;

import org.bukkit.Location;
import org.bukkit.Material;

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

    public abstract void onPlace(Location location);
    public abstract void onBreak(Location location);

    public Location getLoc() {
        return loc;
    }

    protected void setLoc(Location loc) {
        this.loc = loc;
    }
}
