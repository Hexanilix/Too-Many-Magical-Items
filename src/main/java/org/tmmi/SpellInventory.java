package org.tmmi;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
}
