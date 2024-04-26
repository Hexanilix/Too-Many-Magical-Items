package org.tmmi;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
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
    public static @Nullable WeavePlayer getWeaver(UUID id) {
        for (WeavePlayer w : weavers)
            if (w.getPlayer().getUniqueId() == id) return w;
        return null;
    }

    private final Player player;
    private final SpellInventory spellInventory;
    private boolean isWeaving;
    private Wand wand;
    private List<Spell> spells = new ArrayList<>();
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

    public void cast(@NotNull Action action) {
        Spell s = (action.name().contains("LEFT") ? this.getMainSpell() : this.getSecondarySpell());
        if (s != null) {
            float mul = (this.isWeaving ? wand.getPower() : 1);
            s.cast(action, this.player.getEyeLocation(), 5);
        }
    }

    public boolean addSpell(@NotNull Spell @NotNull ... spells) {
        for (Spell s : spells) {
            if (s.getType() == Spell.SpellType.CANTRIP) {
                if (this.getSpellInventory().getCanSize() > this.getSpellInventory().getCanSpells().size()) {
                    this.getSpellInventory().addSpell(s);
                    return false;
                }
            } else {
                if (this.getSpellInventory().getSorSize() > this.getSpellInventory().getSorcerySpells().size()) {
                    this.getSpellInventory().addSpell(s);
                    return false;
                }
            }
        }
        return true;
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
