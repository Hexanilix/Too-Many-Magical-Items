package org.tmmi.spells;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.spells.atributes.Weight;
import org.tmmi.events.SpellCollideEvent;

import java.util.*;

import static org.hetils.Util.*;
import static org.tmmi.Main.*;

public abstract class Spell {
    public static Collection<Spell> spells = new HashSet<>();
    public static ArrayList<UUID> disabled = new ArrayList<>();
    public static double mxT = 20;
    public static double mxS = 20;
    public static double mxD = 20;

    public static @Nullable Spell getSpell(UUID id) {
        for (Spell s : spells)
            if (id.equals(s.getId()))
                return s;
        return null;
    }

    public enum SpellAtribute {
        CHARGE
    }

    private final UUID id;
    private final String name;
    private int level;
    private int XP;
    private int castCost;
    private final Weight weight;
    private Vector box;

    private @NotNull UUID uuid() {
        String u = UUID_SEQUENCE + IntToHex(spells.size(), 4) +
                '-' +
                toHex("3hure9tgi", 4) +
                '-' +
                toHex(weight.name(), 4) +
                '-' +
                toHex(name, 4) +
                String.valueOf(digits.charAt(new Random().nextInt(16))).repeat(8);
        return UUID.fromString(u);
    }
    public Spell(UUID id, String name, Weight weight, int level, int XP, int castCost, Vector box) {
        this.name = name;
        this.castCost = castCost;
        assert level > 0;
        this.level = level;
        this.XP = XP;
        this.weight = weight;
        this.box = box;
        this.id = (id == null ? uuid() : id);
        if (!DISABLED_SPELLS.v().contains(this.id)) spells.add(this);
    }

    public String getName() {
        return name;
    }
    public UUID getId() {
        return id;
    }
    public int getLevel() {
        return level;
    }
    public int getCastCost() {
        return castCost;
    }

    public void setCastCost(int castCost) {
        this.castCost = castCost;
    }

    public Vector getBox() {
        return box;
    }
    public int getXP() {
        return XP;
    }
    public void addXP(int XP) {
        this.XP += XP;
        this.attemptLvlUP();
    }
    public Weight getWeight() {
        return weight;
    }
    public void attemptLvlUP() {
        int l = this.level;
        while (XP - xpsum(this.level-1) >= lvlXPcalc(this.level-1)) {
            this.level++;
            this.onLevelUP();
        }
    }

    public abstract CastSpell cast(@NotNull Location castLocation, float multiplier, Entity e);

    public abstract void onLevelUP();

    public void onCollide(SpellCollideEvent event) {}

    public abstract ItemStack toItem();
    public int xpsum(int lvl) {
        int s = 0;
        for (int i = 0; i < lvl; i++) s += lvlXPcalc(i);
        return s;
    }
    public static int lvlXPcalc(int lvl) {
        return (int) (lvl < 0 ? 0 : Math.round(((Math.pow(14, 1+lvl))/Math.pow(10, lvl)-4)));
    }

    @Override
    public String toString() {
        return "Spell{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", XP=" + XP +
                ", castCost=" + castCost +
                ", spellType=" + weight +
                '}';
    }

    public abstract String toJson();
}
