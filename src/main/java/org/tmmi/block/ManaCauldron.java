package org.tmmi.block;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.tmmi.Main;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.hetils.Util.newItemStack;
import static org.tmmi.Main.log;
import static org.tmmi.Main.setCauldronFillLevel;

public class ManaCauldron extends Block {
    public static Collection<ManaCauldron> instances = new HashSet<>();
    public static final ItemStack item = newItemStack(Material.CAULDRON, "ManaCaul", 3435879);


    final int max = 1000;
    int mana = 0;
    public ManaCauldron(org.bukkit.block.Block block) {
        super(Material.CAULDRON, block, item);
    }

    public int getMana() {
        return mana;
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
