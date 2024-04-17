package org.tmmi;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.items.GrandBook;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.tmmi.Main.log;

public class WeavePlayer {
    public static List<WeavePlayer> weavers = new ArrayList<>();
    public static @Nullable WeavePlayer getWeaver(Player player) {
        for (WeavePlayer w : weavers) {
            if (w.getPlayer() == player) return w;
        }
        return null;
    }

    private final Player player;
    private final SpellInventory spellInventory;
    private boolean isWeaving;
    private Wand wand;
    private List<Spell> spells;
    private GrandBook grandBook;

    public WeavePlayer(Player player, SpellInventory spellInventory) {
        this.player = player;
        this.spellInventory = spellInventory;
        this.isWeaving = false;
        this.grandBook = null;
        weavers.add(this);
    }

    public String toJson() {
        return "{ \"Weaver\":\n" +
                    "{\n" +
                        "\"Power\":" + this.player.getUniqueId() + ",\n" +
                        String.join("\n", this.getSpells().stream().map(Spell::toJson).toList()) +
                    "}\n" +
                "}";
    }

    public Player getPlayer() {
        return player;
    }

    public SpellInventory getSpellInventory() {
        return spellInventory;
    }

    public void cast(@NotNull Action action) {
        Spell s = (action.name().contains("LEFT") ? this.getMainSpell() : this.getSecondarySpell());
        if (s != null) {
            float mul = (this.isWeaving ? wand.getPower() : 1);
            s.cast(action, this.player.getEyeLocation(), 5);
        }
    }

    public void addSpell(Spell s) {
        this.spells.add(s);
    }

    private Spell getMainSpell() {
        return this.getSpellInventory().getMainSpell();
    }

    private Spell getSecondarySpell() {
        return this.getSpellInventory().getSecondarySpell();
    }

    public List<Spell> getSpells() {
        return this.spells;
    }

    public boolean isWeaving() {
        return isWeaving;
    }

    public void setWeaving(boolean b) {
        isWeaving = b;
    }

    public int getWandSlot() {
        return this.wand.getSlot();
    }

    public Wand getWand() {
        return this.wand;
    }

    public boolean hasGrandBook() {
        return grandBook != null;
    }

    public void setWand(Wand wand) {
        this.wand = wand;
    }
}
