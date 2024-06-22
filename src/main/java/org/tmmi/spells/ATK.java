package org.tmmi.spells;

import org.bukkit.*;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Element;
import org.tmmi.MagicChunk;
import org.tmmi.block.Block;
import org.tmmi.events.Listener;
import org.tmmi.events.SpellCollideEvent;
import org.tmmi.spells.atributes.AreaEffect;
import org.tmmi.spells.atributes.Weight;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

import static org.tmmi.Main.*;

public class ATK extends Spell {
    final AreaEffect areaEffect;
    final Element mainElement;
    final Element secondaryElement;
    double travel;
    double speed;
    double baseDamage;
    boolean phase;
    public ATK(UUID id, UUID handler, String name, Weight w, int level, int XP, int castcost,
               @NotNull Element mainElement, Element secondaryElement, @NotNull AreaEffect areaEffect,
               double speed, double travel, double baseDamage, boolean phase) {
        super(id, handler, name, w, level, XP, castcost, new Vector(0.1, 0.1, 0.1));
        this.mainElement = mainElement;
        this.secondaryElement = secondaryElement;
        this.areaEffect = areaEffect;
        this.speed = speed;
        this.travel = travel;
        this.baseDamage = baseDamage;
        this.phase = phase;
        this.attemptLvlUP();
    }
    public ATK(UUID id, UUID handler, String name, Weight w, int level, int XP, int castcost,
               @NotNull Element mainElement, Element secondaryElement, @NotNull AreaEffect areaEffect,
               double speed, double travel, double baseDamage) {
        this(id, handler, name, w, level, XP, castcost, mainElement, secondaryElement, areaEffect, speed, travel, baseDamage, false);
    }
    public ATK(UUID handler, String name, Weight w, @NotNull Element mainElement, Element secondaryElement, @NotNull AreaEffect areaEffect) {
        this(null, handler, name, w, 1, 0, 3, mainElement, secondaryElement, areaEffect, 1, 10, 2, false);
    }

    public double getSpeed() {
        return speed;
    }

    public double getTravel() {
        return travel;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    @Override
    public void onLevelUP() {
        this.baseDamage += 0.15;
        this.speed += 0.05;
        this.travel += 1;
    }

    @Override
    public CastSpell cast(@NotNull Location castLocation, float multiplier) {
        log("cast " + this.getName());
        boolean opE = true;
        Particle p = null;
        switch (this.mainElement) {
            case AIR -> {
                p = Particle.CLOUD;
                for (int i = 0; i < 10; i++)
                    if (castLocation.getWorld().getBlockAt(castLocation.clone().add(0, i, 0)).getType() != Material.AIR &&
                            castLocation.getWorld().getBlockAt(castLocation.clone().add(0, -i, 0)).getType() != Material.AIR)
                        opE = false;
            }
            case FIRE -> {
                p = Particle.FLAME;
            }
            case EARTH -> {
                p = Particle.BLOCK;
            }
            case WATER -> {
                p = Particle.DRIPPING_WATER;
            }
        }
        boolean opB = (!Element.getOptimalBiomes(this.mainElement).contains(castLocation.getWorld().getBiome(castLocation)));
        Particle finalP = p;
        double travDis = this.travel + (opE ? 0.7 : 0) + (opB ? 1 : 0);
        double spellSpeed = BigDecimal.valueOf((this.speed - Math.floor(this.speed)) / 2).round(MathContext.DECIMAL32).doubleValue() + 0.3;
        Vector direction = castLocation.getDirection();
        Location loc = castLocation.clone();
        double speed = this.speed;
        log(this.speed);
        loc.add(direction.multiply(spellSpeed));
        Sound sound;
        switch (mainElement) {
            case FIRE -> sound = Sound.BLOCK_FIRE_AMBIENT;
            case EARTH -> sound = Sound.MUSIC_UNDER_WATER;
            case WATER -> sound = Sound.MUSIC_UNDER_WATER;
            default -> sound = Sound.ENTITY_GENERIC_EXPLODE;
        }
        int tick = (int) Math.max(speed/4, 0);
        switch (this.areaEffect) {
            case DIRECT -> {
                ArmorStand amor = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                amor.setVisible(false);
                amor.setCustomNameVisible(true);
                amor.setGravity(false);
                return new CastSpell(this, loc, getCastCost()) {

                    @Override
                    public BukkitTask cast(CastSpell casts) {
                        return new BukkitRunnable() {
                            private final ArmorStand arm = amor;
                            private final Location stl = castLocation;
                            double dmg = ATK.this.getBaseDamage();
                            @Override
                            public void cancel() {
                                arm.remove();
                                casts.uncast();
                                ATK.this.attemptLvlUP();
                                Bukkit.getScheduler().cancelTask(this.getTaskId());
                            }

                            @Override
                            public void run() {
                                loc.add(direction.clone().multiply(speed));
                                double distance = loc.distance(stl);
                                Material t = loc.getBlock().getType();
                                if (distance >= travDis || t.isSolid()) cancel();
                                else {
                                    arm.setCustomName("S: " + speed + " | Dis: " + distance);
                                    arm.teleport(loc.clone().subtract(0, 1, 0));
//                                       loc.getWorld().playSound(loc, sound, 1, 1);
                                    Location l = loc.clone();
                                    l.getWorld().spawnParticle(Particle.FLAME, l, 1, 0, 0, 0, 0);
//                                    l.getWorld().spawnParticle(Particle.FLAME, l.add(l.clone().toVector().subtract(l.clone().toVector().multiply(-1).normalize())), 1, 0, 0, 0, 0);
                                    loc.getWorld().spawnParticle(finalP, l, (int) (baseDamage / 2) + (getLevel() / 2), 0, 0, 0, 0);
                                    Collection<Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 0.3, 0.5), 0.5, 0.3, 0.5);
                                    if (loc.getBlock().getType() != Material.AIR || !nearbyEntities.isEmpty()) {
                                        for (Entity e : nearbyEntities) {
                                            if (e instanceof LivingEntity liv) {
                                                if (liv == Bukkit.getPlayer(getHandler()) || liv == arm) continue;
                                                liv.damage(dmg);
                                                loc.getWorld().spawnParticle(finalP, loc, 10, 1, 1, 1, 0.05);
                                                cancel();
                                            }
                                        }
                                    }
                                }
                            }
                        }.runTaskTimer(plugin, 0, tick);
                    }
                };
            }
            case WIDE -> {
                ArmorStand amor = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                amor.setVisible(false);
                amor.setCustomNameVisible(true);
                amor.setGravity(false);
                return new CastSpell(this, loc, getCastCost()) {
                    private final ArmorStand arm = amor;
                    private double dmg = ATK.this.getBaseDamage();

                    @Override
                    public BukkitTask cast(CastSpell casts) {
                        return new BukkitRunnable() {
                            private final Location stl = castLocation;
                            @Override
                            public void cancel() {
                                arm.remove();
                                casts.uncast();
                                ATK.this.attemptLvlUP();
                                Bukkit.getScheduler().cancelTask(this.getTaskId());
                            }

                            @Override
                            public void run() {
                                loc.add(direction.clone().multiply(speed));
                                double distance = loc.distance(stl);
                                Material t = loc.getBlock().getType();
                                if (distance >= travDis || t.isSolid()) cancel();
                                else {
                                    arm.setCustomName("S: " + speed + " | Dis: " + distance);
                                    arm.teleport(loc.clone().subtract(0, 1, 0));
//                                       loc.getWorld().playSound(loc, sound, 1, 1);
                                    Location l = loc.clone();
                                    l.getWorld().spawnParticle(Particle.FLAME, l, 1, 0, 0, 0, 0);
//                                    l.getWorld().spawnParticle(Particle.FLAME, l.add(l.clone().toVector().subtract(l.clone().toVector().multiply(-1).normalize())), 1, 0, 0, 0, 0);
//                                    loc.getWorld().spawnParticle(finalP, l, (int) (baseDamage / 2) + (getLevel() / 2), 0, 0, 0, 0);
                                    double rad = ((double) getLevel() /20) + 1;
                                    spawnParticleSphere(loc, Particle.FLAME, rad, getLevel());
                                    Collection<Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 0.3, 0.5), rad, rad, rad);
                                    if (loc.getBlock().getType() != Material.AIR || !nearbyEntities.isEmpty()) {
                                        for (Entity e : nearbyEntities) {
                                            if (e instanceof LivingEntity liv) {
                                                if (liv == Bukkit.getPlayer(getHandler()) || liv == arm) continue;
                                                liv.damage(dmg);
                                                loc.getWorld().spawnParticle(finalP, loc, 10, 1, 1, 1, 0.05);
                                                cancel();
                                            }
                                        }
                                    }
                                }
                            }
                        }.runTaskTimer(plugin, 0, tick);
                    }

                    @Override
                    public void onUncast(boolean natural) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                arm.remove();
                                cancel();
                            }
                        }.runTaskTimer(plugin, 0, 0);
                        if (natural) {
                            MagicChunk.getOrNew(getLoc()).addMana(getCastCost());
                        }
                    }

//                    public class lis extends Listener {
//                        public void spellCollide(SpellCollideEvent event) {
//                            if (event.getHitSpell().getS() instanceof ATK a) {
//                                if (dmg < event.getHitSpell().dmg)
//                                dmg -= a.getBaseDamage();
//                            }
//                        }
//                    }
                };
            }
            case null, default -> {
                return null;
            }
        }
    }
    public void spawnParticleSphere(Location center, Particle particle, double radius, int points) {
        for (int i = 0; i < points; i++) {
            // Use spherical coordinates to calculate points on the sphere
            double theta = Math.random() * 2 * Math.PI; // Angle in the XY-plane
            double phi = Math.acos(2 * Math.random() - 1); // Angle from the Z-axis

            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.sin(phi) * Math.sin(theta);
            double z = radius * Math.cos(phi);

            Location particleLocation = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(particle, particleLocation, 1, 0, 0, 0, 0);
        }
    }

    @Override
    public ItemStack toItem() {
        ChatColor c;
        switch (this.mainElement) {
            case AIR -> c = ChatColor.WHITE;
            case FIRE -> c = ChatColor.RED;
            case EARTH -> c = ChatColor.GREEN;
            default -> c = ChatColor.AQUA;
        }
        ChatColor sc;
        switch (this.secondaryElement == null ? this.mainElement : this.secondaryElement) {
            case AIR -> sc = ChatColor.GRAY;
            case FIRE -> sc = ChatColor.DARK_RED;
            case EARTH -> sc = ChatColor.DARK_GREEN;
            default -> sc = ChatColor.DARK_AQUA;
        }
        ItemStack item = getWeight() == Weight.SORCERY ? new ItemStack(Material.ENCHANTED_BOOK) : Element.getItem(this.mainElement);
        ItemMeta m = item.getItemMeta();
        assert m != null;
        m.setDisplayName(c+this.getName() + " (" + this.getWeight() + ")");
        int nxtlxp = this.getXP() - (this.xpsum(this.getLevel()-1));
        int nxtLvlXP = lvlXPcalc(this.getLevel()-1);
        int perc = (int) (((float) nxtlxp / nxtLvlXP) * 100);
        m.setLore(List.of(sc + "Damage: " + c + this.baseDamage,
                sc + "Range: " + c + this.travel + " Blocks",
                sc + "Speed: " + c + this.speed + " B/s",
                sc+"Level "+c+this.getLevel(), c + "[" +
                        "-".repeat(Math.max(0, perc/10)) +
                        sc + "-".repeat(Math.max(0, 10 - perc/10)) +
                        c + "]" + (nxtlxp >= 1000 ? BigDecimal.valueOf((float) nxtlxp / 1000).setScale(2, RoundingMode.HALF_EVEN) + "k" : nxtlxp) +
                        sc + "/" + c + (nxtLvlXP >= 1000 ? BigDecimal.valueOf((float) nxtLvlXP / 1000).setScale(2, RoundingMode.HALF_EVEN) + "k" : nxtLvlXP),
                ChatColor.GRAY + "Total XP: " + this.getXP()));
        item.setItemMeta(m);
        return item;
    }

    @Override
    public String toJson() {
        return  "\t\t{\n" +
                "\t\"type\":\"ATK\",\n" +
                "\t\"id\":\"" + this.getId() + "\",\n" +
                "\t\"name\":\"" + this.getName() + "\",\n" +
                "\t\"level\":" + this.getLevel() + ",\n" +
                "\t\"experience\":" + this.getXP() + ",\n" +
                "\t\"cast_cost\":" + this.getCastCost() + ",\n" +
                "\t\"weight\":\"" + this.getWeight() + "\",\n" +
                "\t\"main_element\":\"" + this.mainElement + "\",\n" +
                "\t\"secondary_element\":\"" + this.secondaryElement + "\",\n" +
                "\t\"area_effect\":\"" + this.areaEffect + "\",\n" +
                "\t\"base_damage\":" + this.baseDamage + ",\n" +
                "\t\"speed\":" + this.speed + ",\n" +
                "\t\"travel\":" + this.travel + ",\n" +
                "\t\"phase\":" + this.phase + "\n" +
                "}";
    }
}
