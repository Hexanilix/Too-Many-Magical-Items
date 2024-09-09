package org.tmmi.block;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

import static org.hetils.Block.setCauldronFillLevel;
import static org.hetils.Item.newItemStack;
import static org.hetils.Util.isSim;

public class ManaCauldron extends Block {
    public static Collection<ManaCauldron> instances = new HashSet<>();
    public static final ItemStack item = newItemStack(Material.CAULDRON, "ManaCaul", 3435879);

    final int max = 1000;
    int mana;
    public ManaCauldron(@NotNull Location loc) {
        this(loc.getBlock(), 0);
    }
    public ManaCauldron(@NotNull Location loc, int mana) {
        this(loc.getBlock(), mana);
    }
    public ManaCauldron(org.bukkit.block.Block block) {
        this(block, 0);
    }
    public ManaCauldron(org.bukkit.block.Block block, int mana) {
        super(Material.CAULDRON, block, item);
        this.mana = mana;
        update();
        instances.add(this);
    }
    public static @NotNull ManaCauldron getOrNew(org.bukkit.block.Block block) {
        for (ManaCauldron m : instances)
            if (isSim(m.getBlock(), block))
                return m;
        return new ManaCauldron(block);
    }
    public int getMana() {
        return mana;
    }
    public void setMana(int a) {
        this.mana = a;
        update();
    }
    public void addMana(int a) {
        this.mana+=a;
        update();
    }
    public void subMana(int a) {
        this.mana-=Math.min(Math.abs(a), this.mana);
        update();
    }
    public void update() {
        if (mana < max) {
            int l = mana == 0 ? 0 : (mana > max / 3 ? (mana > (max / 3) * 2 ? 3 : 2) : 1);
            setCauldronFillLevel(this.getBlock(), l);
        } else {
            Bukkit.broadcastMessage("boom");
        }
    }

    @Override
    public String json() {
        return "\t\"mana\":\"" + this.mana + "\"";
    }
}
