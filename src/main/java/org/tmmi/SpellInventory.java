package org.tmmi;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.tmmi.Main.background;
import static org.tmmi.Main.newItemStack;
import static org.tmmi.Spell.SpellType.CANTRIP;

public class SpellInventory {

    public enum SpellType {
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
        this(canSpells, sorcerySpells, new Pair<>(null, null), 1, 0);
    }
    SpellInventory(List<Spell> canSpells) {
        this(canSpells, new ArrayList<>());
    }
    SpellInventory() {
        this(new ArrayList<>());
    }

    public List<Spell> getSorcerySpells() {
        return sorcerySpells;
    }

    public List<Spell> getCanSpells() {
        return canSpells;
    }

    public Spell getMainSpell() {
        return activeSpells.key();
    }

    public Spell getSecondarySpell() {
        return activeSpells.value();
    }

    public void addSpell(@NotNull Spell s) {
        if (s.getType() == CANTRIP) this.canSpells.add(s);
        else this.sorcerySpells.add(s);
    }

    public void removeSpell(@NotNull Spell s) {
        if (s.getType() == CANTRIP) this.canSpells.remove(s);
        else this.sorcerySpells.remove(s);
    }

    public void setActiveSpells(SpellType t, Spell s) {
        if (t == SpellType.MAIN) this.activeSpells.setKey(s);
    }

    public void expandCanSize(int amnt) {
        this.canSize += amnt;
    }

    public void expandSorSize(int amnt) {
        this.sorSize += amnt;
    }

    public int getSorSize() {
        return this.sorSize;
    }

    public int getCanSize() {
        return this.canSize;
    }

    public Inventory toInventory() {
        Inventory inv = Bukkit.createInventory(null, 54, "Spell Weaver");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, background);
        for (int i = 10; i < this.canSize; i++) inv.setItem(i, null);
        for (Spell s : canSpells) {
            ItemStack item = s.toItem();
            if (s == this.getMainSpell()) item.getItemMeta().setAtributes();
            inv.add(s.toItem());
        }
        for (int i = 28; i < this.canSize; i++) inv.setItem((i > 35 ? i+3 : i), null);
        int j = 28;
        for (Spell s : sorcerySpells) {
            ItemStack item = s.toItem();
            inv.setItem(j, item);
            j++;
        }
        return inv;
    }
}
