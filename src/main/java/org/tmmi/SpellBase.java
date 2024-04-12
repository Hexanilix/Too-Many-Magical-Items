package org.tmmi;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.tmmi.events.SpellCollideEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.tmmi.Main.log;
import static org.tmmi.Main.plugin;

public class SpellBase {

    public enum SpellType {
        CANTRIP,
        SORCERY,
        INCANTATION
    }

    public enum CastAreaEffect {
        DIRECT,
        WIDE_RANGE,
        AREA_EFFECT
    }

    public enum SpellAtribute {
        CHARGE
    }

    public enum MainElement {
        FIRE,
        EARTH,
        WATER,
        AIR
    }
    public enum SecondaryElement {
        FIRE,
        EARTH,
        WATER,
        AIR
    }

    private final String name;
    private final CastAreaEffect castAreaEffect;
    private final MainElement mainElement;
    private final SecondaryElement secondaryElement;
    private final int level;
    private final int castCost;
    private final SpellType spellType;
    private final double travel;
    private final double speed;
    private final Sound castSpund;

    private BukkitTask spellRun;

    SpellBase(String name, CastAreaEffect castAreaEffect, MainElement mainElement) {
        this(name, castAreaEffect, mainElement, null);
    }
    SpellBase(String name, CastAreaEffect castAreaEffect, MainElement mainElement, SecondaryElement secondaryElement) {
        this.name = name;
        this.castAreaEffect = castAreaEffect;
        this.mainElement = mainElement;
        this.secondaryElement = secondaryElement;
        this.level = 5;
        this.spellType = (this.level > 10 ? SpellType.INCANTATION : (this.level > 3 ? SpellType.SORCERY : SpellType.CANTRIP));
        this.castCost = 5;
        this.travel = 15;
        this.speed = 1;
        this.castSpund = Sound.MUSIC_UNDER_WATER;
    }

    public String getName() {
        return name;
    }

    public CastAreaEffect getCastAreaEffect() {
        return castAreaEffect;
    }

    public MainElement getMainElement() {
        return mainElement;
    }

    public SecondaryElement getSecondaryElement() {
        return secondaryElement;
    }

    public int getLevel() {
        return level;
    }

    public SpellType getType() {
        return spellType;
    }

    public int getCastCost() {
        return castCost;
    }

    public double getTravel() {
        return travel;
    }

    public void cast(Action action, Location castLocation, float multiplier) {
        Particle p = null;
        switch (this.mainElement) {
            case AIR -> p = Particle.CLOUD;
            case FIRE -> p = Particle.FLAME;
            case EARTH -> p = Particle.BLOCK_DUST;
            case WATER -> p = Particle.WATER_DROP;
        }
        Particle finalP = p;
        double travDis = this.travel;
        double spellSpeed = this.speed;
        Vector direction = castLocation.getDirection();
        Location loc = castLocation.clone();
        loc.add(direction.multiply(spellSpeed));
        switch (this.castAreaEffect) {
            case DIRECT -> {
                this.spellRun = new BukkitRunnable() {
                    private double distance = 0;
                    @Override
                    public void run() {
                        if (distance > travDis) cancel();
                        distance += spellSpeed;
                        loc.add(direction.multiply(spellSpeed));
                        Objects.requireNonNull(loc.getWorld()).spawnParticle(finalP, loc, Math.round(1*multiplier), 0, 0, 0, 0);
                        List<Entity> nearbyEntities = (List<Entity>) Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc.clone().add(0.5, 0.3, 0.5), 0.5, 0.3, 0.5);
                        if (loc.getBlock().getType() != Material.AIR || !nearbyEntities.isEmpty()) {
                            for (Entity e : nearbyEntities) {
                                if (e instanceof LivingEntity liv) {
                                    liv.damage(1 * multiplier);
                                    for (int j = 0; j < 10; j++)
                                        Objects.requireNonNull(loc.getWorld()).spawnParticle(finalP, loc, 0, 0, 0.5, 0, 0);
                                }
                            }
                        }
                    }
                }.runTaskTimer(plugin, 0, 2);
            }
            case WIDE_RANGE -> {
                this.spellRun = new BukkitRunnable() {
                    private double distance = 0;
                    private final List<Integer> hitInts = new ArrayList<>();
                    @Override
                    public void run() {
                        if (distance > travDis) cancel();
                        for (int i = 0; i < 6; i++) {
                            loc.setYaw(60*i);
                            if (!hitInts.contains(i)) {
                                Objects.requireNonNull(loc.getWorld()).spawnParticle(finalP, loc, Math.round(1 * multiplier), 0, 0, 0, 0);
                                List<Entity> nearbyEntities = (List<Entity>) Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc.clone().add(0.5, 0.3, 0.5), 0.5, 0.3, 0.5);
                                if (loc.getBlock().getType() != Material.AIR || !nearbyEntities.isEmpty()) {
                                    for (Entity e : nearbyEntities) {
                                        if (e instanceof LivingEntity liv) {
                                            liv.damage(1 * multiplier);
                                            for (int j = 0; j < 10; j++)
                                                Objects.requireNonNull(loc.getWorld()).spawnParticle(finalP, loc, 0, 0, 0.5, 0, 0);
                                        }
                                    }
                                    hitInts.add(i);
                                    if (hitInts.size() == 6) cancel();
                                }
                            }
                        }
                        distance += spellSpeed;
                        loc.add(direction.multiply(spellSpeed));
                    }
                }.runTaskTimer(plugin, 0, 2);
            }
            case AREA_EFFECT -> {

            }
        }

    }
    private void castOnBlock() {

    }

    public void onCollide(SpellCollideEvent event) {

    }

}
