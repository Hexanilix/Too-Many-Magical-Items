package org.tmmi.spells;

import org.bukkit.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.Main;
import org.tmmi.Property;
import org.tmmi.spells.atributes.Weight;
import org.tmmi.events.SpellCollideEvent;

import java.util.*;

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
    private final UUID handler;
    private final String name;
    private int level;
    private int XP;
    private int castCost;
    private final Weight weight;
    private Vector box;

    private @NotNull UUID uuid() {
        String u = UUID_SEQUENCE + IntToHex(spells.size(), 4) +
                '-' +
                toHex(handler, 4) +
                '-' +
                toHex(weight.name(), 4) +
                '-' +
                toHex(name, 4) +
                String.valueOf(Main.digits.charAt(new Random().nextInt(16))).repeat(8);
        return UUID.fromString(u);
    }
    public Spell(UUID id, UUID handler, String name, Weight weight, int level, int XP, int castCost, Vector box) {
        this.handler = handler;
        this.name = name;
        this.castCost = castCost;
        assert level > 0;
        this.level = level;
        this.XP = XP;
        this.weight = weight;
        this.box = box;
        this.id = (id == null ? uuid() : id);
        if (!Property.DISABLED_SPELLS.v().contains(this.id)) spells.add(this);
    }

    public UUID getHandler() {
        return handler;
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
    }
    public Weight getWeight() {
        return weight;
    }
    public void attemptLvlUP() {
        if (XP - xpsum(this.level-1) >= lvlXPcalc(this.level-1)) {
            this.level++;
            if (Bukkit.getPlayer(this.handler) != null) Bukkit.getPlayer(this.handler).sendMessage(this.name + " upgraded to level " + this.level);
            onLevelUP();
        }
    }

    public abstract CastSpell cast(PlayerInteractEvent event, Location castLocation, float multiplier);

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
                ", handler=" + handler +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", XP=" + XP +
                ", castCost=" + castCost +
                ", spellType=" + weight +
                '}';
    }

    public abstract String toJson();
}
