package org.tmmi.item;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hetils.Vector.genVec;
import static org.tmmi.Main.plugin;

public class ItemCommander {
    public static List<ItemCommander> instances = new ArrayList<>();

    private final org.bukkit.entity.Item item;
    private BukkitRunnable operation;
    private final List<BukkitRunnable> waitlist = new ArrayList<>();
    public ItemCommander(org.bukkit.entity.Item item) {
        this.item = item;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (operation != null) {
                    if (operation.isCancelled())
                        operation = null;
                } else if (!waitlist.isEmpty()) {
                    item.setVelocity(new Vector(0, 0, 0));
                    operation = waitlist.getFirst();
                    operation.runTaskTimer(plugin, 0, 0);
                    waitlist.removeFirst();
                }
            }
        }.runTask(plugin);
    }

    public static @NotNull ItemCommander getOrNew(org.bukkit.entity.Item i) {
        for (ItemCommander c : instances)
            if (c.getItem() == i)
                return c;
        return new ItemCommander(i);
    }

    public org.bukkit.entity.Item getItem() {
        return item;
    }

    public ItemCommander newOperation(BukkitRunnable runnable) {
        waitlist.add(runnable);
        return this;
    }
    public ItemCommander newOperation(Runnable run) {
        waitlist.add(new BukkitRunnable() {
            @Override
            public void run() {
                run.run();
            }
        });
        return this;
    }
    public ItemCommander moveTo(Location des) {
        moveTo(des, 1, 1, null);
        return this;
    }
    public ItemCommander moveTo(Location des, double speed) {
        moveTo(des, speed,1, null);
        return this;
    }
    public ItemCommander moveTo(Location des, double speed, double dis) {
        moveTo(des, speed, dis, null);
        return this;
    }
    public ItemCommander moveTo(Location des, double speed, double dis, Runnable end) {
        waitlist.add(new BukkitRunnable() {
            final org.bukkit.entity.Item item = ItemCommander.this.item;
            final Location l = item.getLocation();
            @Override
            public void run() {
                if (item.isDead() || (l.distance(des) < dis)) {
                    if (end != null) end.run();
                    cancel();
                }
                l.add(genVec(l, des).multiply(speed));
                Objects.requireNonNull(l.getWorld()).spawnParticle(Particle.END_ROD, l, 1, 0, 0, 0, 0);
                Vector direction = l.toVector().subtract(item.getLocation().toVector()).normalize();
                double distance = l.distance(item.getLocation());
                if (distance > 4) {
                    item.teleport(l);
                    item.setVelocity(new Vector(0, 0, 0));
                }
                item.setVelocity(direction.multiply(distance));
            }
        });
        return this;
    }
    public ItemCommander moveTo(Entity des) {
        return moveTo(des, 1, 1, null);
    }
    public ItemCommander moveTo(Entity des, double speed) {
        return moveTo(des, speed, 1, null);
    }
    public ItemCommander moveTo(Entity des, double speed, int dis) {
        return moveTo(des, speed, dis,null);
    }
    public ItemCommander moveTo(@NotNull Entity des, double speed, int dis, Runnable end) {
        item.setPickupDelay((int) des.getLocation().distance(item.getLocation()));
        waitlist.add(new BukkitRunnable() {
            final org.bukkit.entity.Item item = ItemCommander.this.item;
            final Location l = item.getLocation();
            @Override
            public void run() {
                if (des.isDead() || item.isDead() || (l.distance(des.getLocation()) < dis)) {
                    if (end != null) end.run();
                    ItemCommander.this.operation = null;
                    cancel();
                }
                l.add(genVec(l, des.getLocation()).multiply(speed));
                Objects.requireNonNull(l.getWorld()).spawnParticle(Particle.END_ROD, l, 1, 0, 0, 0, 0);
                Vector direction = l.toVector().subtract(item.getLocation().toVector()).normalize();
                double distance = l.distance(item.getLocation());
                if (distance > 4) {
                    item.teleport(l);
                    item.setVelocity(new Vector(0, 0, 0));
                }
                item.setVelocity(direction.multiply(distance));
            }
        });
        return this;
    }
    public ItemCommander revolve(@NotNull Location loc) {
        return revolve(loc, 10, 2);
    }
    public ItemCommander revolve(@NotNull Location loc, float rspeed, double distance) {
        waitlist.add(new BukkitRunnable() {
            final org.bukkit.entity.Item item = ItemCommander.this.item;
            Location cen = loc.clone();
            float r = 0;
            @Override
            public void run() {
                if (item.isDead()) {
                    ItemCommander.this.operation = null;
                    cancel();
                }
                item.setPickupDelay(23452345);
                cen = loc.clone();
                cen.setYaw(r); cen.setPitch(0);
                Location l = cen.clone().add(cen.getDirection().multiply(distance));
                Vector direction = l.toVector().subtract(item.getLocation().toVector()).normalize();
                Objects.requireNonNull(l.getWorld()).spawnParticle(Particle.END_ROD, l, 1, 0, 0, 0, 0);
                double distance = l.distance(item.getLocation());
                if (distance > 4) {
                    item.teleport(l);
                    item.setVelocity(new Vector(0, 0, 0));
                }
                item.setVelocity(direction.multiply(distance));
                r+=rspeed;
            }
        });
        return this;
    }

    public void remove() {
        instances.remove(this);
        item.remove();
    }
}