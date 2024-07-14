package org.tmmi.spell;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class CastSpell {
    public static @NotNull Collection<CastSpell> getNearbyCasts(Location loc, double radius) {
        return getNearbyCasts(loc, radius, null);
    }
    public static @NotNull Collection<CastSpell> getNearbyCasts(Location loc, double radius, CastSpell ex) {
        List<CastSpell> c = new ArrayList<>();
        for (CastSpell s : instances)
            if (s.getWorld() == loc.getWorld() && s != ex && s.getLoc().distance(loc) <= radius)
                c.add(s);
        return c;
    }

    public static Collection<CastSpell> instances = new HashSet<>();
    private final Spell s;
    private Location loc;
    private final Thread run;
    private final int cost;
    public CastSpell(Spell s, @NotNull Location loc, int cost) {
        this.s = s;
        this.loc = loc;
        this.cost = cost;
        this.run = cast();
        run.start();
        instances.add(this);
    }
    public abstract Thread cast();


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

    public void uncast(boolean natural) {
        run.interrupt();
        instances.remove(this);
        onUncast(natural);
    }

    public void uncast() {
        uncast(true);
    }
    public void onUncast(boolean natural) {}

    public int getCastCost() {
        return cost;
    }
}
