package org.tmmi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.tmmi.Main.*;
import static org.tmmi.Spell.SpellType.CANTRIP;

public class SpellInventory {
    public Inventory toInventory() {
        Inventory inv = Bukkit.createInventory(null, 54, "Spell Weaver");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, background);
        setCusData(inv.getItem(0), 2003245);
        for (int i = 10; i < 17; i++) {
            if (i < this.sorcerySpells.size()) {
                Spell s = this.sorcerySpells.get(i);
                ItemStack item = s.toItem();
                if (s == this.getMainSpell()) {
                    ItemMeta m = item.getItemMeta();
                    m.addEnchant(Enchantment.DAMAGE_ALL, 0, true);
                    item.setItemMeta(m);
                }
                inv.setItem(i, item);
            } else inv.setItem(i, (i-10 < this.sorSize ? newItemStack(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Empty Slot", unclickable) : newItemStack(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Locked Slot", unclickable)));
        }
        for (int i = 0; i < 14; i++) {
            int j = (i > 6 ? i + 30 : i + 28);
            if (j < this.canSpells.size()) {
                Spell s = this.canSpells.get(j);
                ItemStack item = s.toItem();
                if (s == this.getMainSpell()) {
                    ItemMeta m = item.getItemMeta();
                    m.addEnchant(Enchantment.DAMAGE_ALL, 0, true);
                    item.setItemMeta(m);
                }
                inv.setItem(j, item);
            } else inv.setItem(j, (i < this.canSize ? newItemStack(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Empty Slot", unclickable) : newItemStack(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Locked Slot", unclickable)));
        }
        for (int i = 28; i < this.canSize; i++) inv.setItem((i > 35 ? i+3 : i), null);
        int j = 28;
        for (Spell s : this.canSpells) {
            log(s.getName());
            ItemStack item = s.toItem();
            inv.setItem(j, item);
            j++;
        }
        return inv;
    }

    public enum SpellUsage {
        MAIN,
        SECONDARY
    }

    private final List<Spell> sorcerySpells;
    private final List<Spell> canSpells;
    private final Pair<Spell, Spell> activeSpells;
    private int canSize;
    private int sorSize;

    SpellInventory(List<Spell> canSpells, List<Spell> sorcerySpells, Pair<Spell, Spell> activeSpells, int canSize, int sorSize) {
        this.canSpells = canSpells;
        this.sorcerySpells = sorcerySpells;
        this.activeSpells = activeSpells;
        this.canSize = canSize;
        this.sorSize = sorSize;
    }

    SpellInventory(List<Spell> canSpells, List<Spell> sorcerySpells) {
        this(canSpells, sorcerySpells, new Pair<>(null, null), 5, 2);
    }
    SpellInventory(List<Spell> canSpells) {
        this(canSpells, new ArrayList<>());
    }
    SpellInventory() {
        this(new ArrayList<>());
    }

    public List<Spell> getSorcerySpells() {
        return this.sorcerySpells;
    }

    public List<Spell> getCanSpells() {
        return this.canSpells;
    }

    public Spell getMainSpell() {
        return this.activeSpells.key();
    }

    public Spell getSecondarySpell() {
        return this.activeSpells.value();
    }

    public boolean addSpell(@NotNull Spell s) {
        log("Attempting add spell " + s.getName());
        if (s.getType() == Spell.SpellType.CANTRIP) {
            if (this.canSize <= this.canSpells.size()) return false;
            else this.canSpells.add(s);
        } else {
            if (this.sorSize <= this.sorcerySpells.size()) return false;
            else this.sorcerySpells.add(s);
        }
        return true;
    }

    public void removeSpell(@NotNull Spell s) {
        if (s.getType() == CANTRIP) this.canSpells.remove(s);
        else this.sorcerySpells.remove(s);
    }

    public void setActiveSpells(@NotNull SpellUsage t, @NotNull Spell s) {
        log("Setting active " + t.name() + " spell to " + s.getName());
        if (this.canSpells.contains(s) || this.sorcerySpells.contains(s)) {
            if (t == SpellUsage.MAIN) this.activeSpells.setKey(s);
            else this.activeSpells.setValue(s);
        } else {
            log("Soft warning: Spell doesn't exist in inventory");
        }
    }

    public void expandCanSize(int amnt) {
        this.canSize += amnt;
    }

    public void expandSorSize(int amnt) {
        this.sorSize += amnt;
    }

    public int getSorSize() {
        return sorSize;
    }

    public int getCanSize() {
        return canSize;
    }
}
