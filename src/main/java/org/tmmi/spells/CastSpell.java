package org.tmmi.spells;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class CastSpell {
    public static @NotNull Collection<CastSpell> getNearbyCasts(Location loc, double radius) {
        return getNearbyCasts(loc, radius, null);
    }
    public static @NotNull Collection<CastSpell> getNearbyCasts(Location loc, double radius, CastSpell ex) {
        List<CastSpell> c = new ArrayList<>();
        for (CastSpell s : castSpells)
            if (s.getWorld() == loc.getWorld() && s != ex && s.getLoc().distance(loc) <= radius)
                c.add(s);
        return c;
    }

    public static Collection<CastSpell> castSpells = new ArrayList<>();
    private final Spell s;
    private Location loc;
    private final BukkitTask run;
    private final int cost;
    CastSpell(Spell s, @NotNull Location loc, int cost) {
        this.s = s;
        this.loc = loc;
        this.cost = cost;
        this.run = this.cast(this);
        castSpells.add(this);
    }
    public abstract BukkitTask cast(CastSpell casts);


    private World getWorld() {
        return loc.getWorld();
    }

    public Spell getS() {
        return s;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public void uncast() {
        run.cancel();
        castSpells.remove(this);
    }

    public int getCastCost() {
        return cost;
    }
}