package org.tmmi.spells;

import org.bukkit.*;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Element;
import org.tmmi.block.Block;
import org.tmmi.spells.atributes.AreaEffect;
import org.tmmi.spells.atributes.Weight;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

import static org.tmmi.Main.log;
import static org.tmmi.Main.plugin;

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
    }
    public ATK(UUID id, UUID handler, String name, Weight w, int level, int XP, int castcost,
               @NotNull Element mainElement, Element secondaryElement, @NotNull AreaEffect areaEffect,
               double speed, double travel, double baseDamage) {
        this(id, handler, name, w, level, XP, castcost, mainElement, secondaryElement, areaEffect, speed, travel, baseDamage, false);
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

    }

    @Override
    public CastSpell cast(PlayerInteractEvent event, Location castLocation, float multiplier) {
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
                return new CastSpell(this, loc, getCastCost()) {

                    @Override
                    public BukkitTask cast(CastSpell casts) {
                        return new BukkitRunnable() {
                            private double distance = 0;
                            @Override
                            public void cancel() {
                                casts.uncast();
                                ATK.this.attemptLvlUP();
                                Bukkit.getScheduler().cancelTask(this.getTaskId());
                            }

                            @Override
                            public void run() {
                                if (distance > travDis) cancel();
                                distance += speed;
                                Location ll = loc.clone();
                                loc.add(direction.clone().multiply(speed));
                                Material t = loc.getBlock().getType();
                                if (t == Material.AIR) cancel();
                                else {
                                    loc.getWorld().spawnParticle(Particle.COMPOSTER, loc, (int) (baseDamage / 2) + (getLevel() / 2), 0, 0, 0, 0);
//                                loc.getWorld().playSound(loc, sound, 1, 1);
                                    for (int i = 0; i < Math.max(Math.floor(ll.distance(loc) * 4), 0) + 1; i++) {
                                        loc.getWorld().spawnParticle(finalP, ll.clone().add(loc.clone().subtract(ll).multiply(ll.distance(loc) / Math.ceil(ll.distance(loc) * 2))), (int) (baseDamage / 2) + (getLevel() / 2), 0, 0, 0, 0);
                                    }
                                    Collection<Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 0.3, 0.5), 0.5, 0.3, 0.5);
                                    if (loc.getBlock().getType() != Material.AIR || !nearbyEntities.isEmpty()) {
                                        for (Entity e : nearbyEntities) {
                                            if (e instanceof LivingEntity liv) {
                                                if (liv.getUniqueId() == event.getPlayer().getUniqueId()) continue;
                                                liv.damage(ATK.this.getBaseDamage());
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
//            case WIDE -> {
//                return new CastSpell(this, loc, getCastCost()) {
//
//                    @Override
//                    public BukkitTask cast(CastSpell casts) {
//                        return new BukkitRunnable() {
//                            private double distance = 0;
//                            @Override
//                            public void run() {
//                                if (distance > travDis) {
//                                    casts.uncast();
//                                    ATK.this.attemptLvlUP();
//                                }
//                                distance += spellSpeed;
//                                loc.add(direction.multiply(speed));
//                                Objects.requireNonNull(loc.getWorld()).playSound(loc, sound, 1, 1);
//                                Objects.requireNonNull(loc.getWorld()).spawnParticle(finalP, loc, (int) Math.ceil(baseDamage/2), 0, 0, 0, 0);
//                                Collection<Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 0.3, 0.5), 0.5, 0.3, 0.5);
//                                if (loc.getBlock().getType() != Material.AIR || !nearbyEntities.isEmpty()) {
//                                    for (Entity e : nearbyEntities) {
//                                        if (e instanceof LivingEntity liv) {
//                                            if (liv.getUniqueId() == event.getPlayer().getUniqueId()) continue;
//                                            liv.damage(ATK.this.getBaseDamage());
//                                            Objects.requireNonNull(loc.getWorld()).spawnParticle(finalP, loc, 10, 1, 1, 1, 0.3);
//                                            cancel();
//                                        }
//                                    }
//                                }
//                                ATK.this.attemptLvlUP();
//                            }
//                        }.runTaskTimer(plugin, 0, tick);
//                    }
//                };
//
//            }
            default -> {
                return null;
            }
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
        ItemStack item = Element.getItem(this.mainElement);
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
                "\t\"travel\":" + this.travel + "\n" +
                "\t\"phase\":" + this.phase + "\n" +
                "}";
    }
}
