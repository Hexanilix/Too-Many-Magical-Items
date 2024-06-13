package org.tmmi.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

import static org.hetils.Util.isSimBlk;
import static org.tmmi.Main.log;

public abstract class Block {
    public static Collection<Block> blocks = new HashSet<>();

    public static class BlockLocationExists extends Exception {
        public BlockLocationExists(String e) {
            super(e);
        }
    }

    ItemStack item;
    org.bukkit.block.Block block;
    Material material;
    public Block(Material material, org.bukkit.block.Block block, ItemStack item) {
        if (block == null) throw new RuntimeException();
        try {
            for (Block b : blocks) {
                log(b.getLoc());
                if (isSimBlk(b.getBlock(), block)) throw new BlockLocationExists("Block already exists at " + block);
            }
            this.material = material;
            this.item = item;
            this.block = block;
            blocks.add(this);
            onPlace();
        } catch (BlockLocationExists e) {
            e.printStackTrace();
        }
    }
    public Block(Material material, @NotNull Location loc, ItemStack item) {
        this(material, loc.getBlock(), item);
    }
    public Block(Material material, @NotNull Location block) {
        this(material, block.getBlock(), new ItemStack(material));
    }
    public Block(Material material, @NotNull org.bukkit.block.Block block) {
        this(material, block, new ItemStack(material));
    }
    public org.bukkit.block.Block getBlock() {
        return block;
    }

    public Material getMaterial() {
        return material;
    }

    public void onPlace() {}

    public final void remove(boolean drop) {
        blocks.remove(this);
        log("jhbrefg");
        if (this instanceof InteractiveBlock)
            InteractiveBlock.instances.remove(this);
        if (drop) block.getWorld().dropItem(block.getLocation().add(0.5,0.5,0.5), item);
        onBreak();
    }

    public void onBreak() {}

    public Location getLoc() {
        return block.getLocation();
    }

    public abstract String toJSON();

    protected World getWorld() {
        return block.getWorld();
    }
}
