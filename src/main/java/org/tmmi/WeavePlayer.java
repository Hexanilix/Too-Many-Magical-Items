package org.tmmi;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.items.GrandBook;
import org.tmmi.items.Wand;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WeavePlayer {
    public static List<WeavePlayer> weavers = new ArrayList<>();
    public static @Nullable WeavePlayer getWeaver(@NotNull Player player) {
        return getWeaver(player.getUniqueId());
    }
    public static @Nullable WeavePlayer getWeaver(@NotNull HumanEntity human) {
        return getWeaver(human.getUniqueId());
    }
    public static @Nullable WeavePlayer getWeaver(UUID id) {
        for (WeavePlayer w : weavers)
            if (w.getPlayer().getUniqueId() == id) return w;
        return null;
    }

    private final Player player;
    private final SpellInventory spellInventory;
    private boolean isWeaving;
    private Wand wand;
    private final List<Spell> spells = new ArrayList<>();
    private GrandBook grandBook;

    public WeavePlayer(Player handler, SpellInventory spellInventory) {
        this.player = handler;
        this.spellInventory = spellInventory;
        this.isWeaving = false;
        this.grandBook = null;
        weavers.add(this);
    }
    public WeavePlayer(Player handler) {
        this(handler, new SpellInventory());
    }

    public Player getPlayer() {
        return player;
    }

    public SpellInventory getSpellInventory() {
        return spellInventory;
    }

    public void cast(@NotNull PlayerInteractEvent event) {
        Spell s = (event.getAction().name().contains("LEFT") ? this.getMainSpell() : this.getSecondarySpell());
        if (s != null) {
            float mul = (this.isWeaving ? wand.getPower() : 1);
            s.cast(event, this.player.getEyeLocation(), mul);
        }
    }

    public boolean addSpell(@NotNull Spell s) {
        return this.spellInventory.addSpell(s);
    }

    private Spell getMainSpell() {
        return this.getSpellInventory().getMainSpell();
    }

    private Spell getSecondarySpell() {
        return this.getSpellInventory().getSecondarySpell();
    }

    public List<Spell> getSpells() {
        List<Spell> spl = this.spellInventory.getCanSpells();
        spl.addAll(this.spellInventory.getSorcerySpells());
        return spl;
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

    public boolean hasWand() {
        return wand != null;
    }

    public boolean hasGrandBook() {
        return grandBook != null;
    }

    public void setWand(Wand wand) {
        this.wand = wand;
    }

    public void setMain(Spell s) {
        this.spellInventory.setActiveSpells(SpellInventory.SpellUsage.MAIN, s);
    }
    public void setMain(UUID id) {
        if (id != null) {
            Spell s = Spell.getSpell(id);
            if (s != null) setMain(s);
        }
    }
    public void setSecondary(Spell s) {
        this.spellInventory.setActiveSpells(SpellInventory.SpellUsage.SECONDARY, s);
    }
    public void setSecondary(UUID id) {
        if (id != null) {
            Spell s = Spell.getSpell(id);
            if (s != null) setSecondary(s);
        }
    }
}
