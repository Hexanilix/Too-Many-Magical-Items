package org.tmmi.items;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hetils.Util.genVec;
import static org.tmmi.Main.plugin;

public class ItemCommand {
    public static List<ItemCommand> instances = new ArrayList<>();

    private final org.bukkit.entity.Item item;
    private BukkitRunnable operation;
    private final List<BukkitRunnable> waitlist = new ArrayList<>();
    public ItemCommand(org.bukkit.entity.Item item) {
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

    public static @NotNull ItemCommand getOrNew(org.bukkit.entity.Item i) {
        for (ItemCommand c : instances)
            if (c.getItem() == i)
                return c;
        return new ItemCommand(i);
    }

    public org.bukkit.entity.Item getItem() {
        return item;
    }

    public ItemCommand newOperation(BukkitRunnable runnable) {
        waitlist.add(runnable);
        return this;
    }
    public ItemCommand newOperation(Runnable run) {
        waitlist.add(new BukkitRunnable() {
            @Override
            public void run() {
                run.run();
            }
        });
        return this;
    }
    public ItemCommand moveTo(Location des) {
        moveTo(des, 1, 1, null);
        return this;
    }
    public ItemCommand moveTo(Location des, double speed) {
        moveTo(des, speed,1, null);
        return this;
    }
    public ItemCommand moveTo(Location des, double speed, double dis) {
        moveTo(des, speed, dis, null);
        return this;
    }
    public ItemCommand moveTo(Location des, double speed, double dis, Runnable end) {
        waitlist.add(new BukkitRunnable() {
            final org.bukkit.entity.Item item = ItemCommand.this.item;
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
    public ItemCommand moveTo(Entity des) {
        return moveTo(des, 1, 1, null);
    }
    public ItemCommand moveTo(Entity des, double speed) {
        return moveTo(des, speed, 1, null);
    }
    public ItemCommand moveTo(Entity des, double speed, int dis) {
        return moveTo(des, speed, dis,null);
    }
    public ItemCommand moveTo(@NotNull Entity des, double speed, int dis, Runnable end) {
        item.setPickupDelay((int) des.getLocation().distance(item.getLocation()));
        waitlist.add(new BukkitRunnable() {
            final org.bukkit.entity.Item item = ItemCommand.this.item;
            final Location l = item.getLocation();
            @Override
            public void run() {
                if (des.isDead() || item.isDead() || (l.distance(des.getLocation()) < dis)) {
                    if (end != null) end.run();
                    ItemCommand.this.operation = null;
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
    public ItemCommand revolve(@NotNull Location loc) {
        return revolve(loc, 10, 2);
    }
    public ItemCommand revolve(@NotNull Location loc, float rspeed, double distance) {
        waitlist.add(new BukkitRunnable() {
            final org.bukkit.entity.Item item = ItemCommand.this.item;
            Location cen = loc.clone();
            float r = 0;
            @Override
            public void run() {
                if (item.isDead()) {
                    ItemCommand.this.operation = null;
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