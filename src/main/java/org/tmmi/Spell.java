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
import org.jetbrains.annotations.NotNull;
import org.tmmi.events.SpellCollideEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.tmmi.Main.plugin;

public class Spell {
    public static List<Spell> spells = new ArrayList<>();

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

    private final UUID handler;
    private final String name;
    private final CastAreaEffect castAreaEffect;
    private final MainElement mainElement;
    private final SecondaryElement secondaryElement;
    private final int level;
    private final int castCost;
    private final SpellType spellType;
    private final double travel;
    private final double speed;
    private final Sound castSound;
    private boolean isCast;
    private Location castLocation;

    private BukkitTask spellRun;

    public Spell(UUID handler, String name, CastAreaEffect castAreaEffect, @NotNull MainElement mainElement, SecondaryElement secondaryElement, int usedMagicules) {
        this.handler = handler;
        this.name = name;
        this.castAreaEffect = castAreaEffect;
        this.mainElement = mainElement;
        this.secondaryElement = secondaryElement;
        this.level = Math.round((float)usedMagicules/100);
        this.spellType = (this.level > 10 ? SpellType.INCANTATION : (this.level > 3 ? SpellType.SORCERY : SpellType.CANTRIP));
        this.castCost = (int) (level * (this.spellType == SpellType.INCANTATION ? 1 : (this.spellType == SpellType.SORCERY ? 2.5 : 10)));
        this.travel = 15;
        this.speed = 1;
        Sound  castSpund1 = null;
        switch (mainElement) {
            case FIRE -> castSpund1 = Sound.MUSIC_UNDER_WATER;
            case EARTH -> castSpund1 = Sound.MUSIC_UNDER_WATER;
            case WATER -> castSpund1 = Sound.MUSIC_UNDER_WATER;
            case AIR -> castSpund1 = Sound.MUSIC_UNDER_WATER;
        }
        this.castSound = castSpund1;
        this.isCast = false;
        spells.add(this);
    }
    public Spell(UUID handler, String name, CastAreaEffect castAreaEffect, @NotNull MainElement mainElement, int usedMagicules) {
        this(handler, name, castAreaEffect, mainElement, null, 6);
    }

    public UUID getHandler() {
        return handler;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Spell{" +
                "handler=" + handler +
                ", castAreaEffect=" + this.castAreaEffect +
                ", mainElement=" +this. mainElement +
                ", secondaryElement=" + this.secondaryElement +
                ", level=" +this.level +
                ", castCost=" + this.castCost +
                ", spellType=" + this.spellType +
                ", travel=" + this.travel +
                ", speed=" + this.speed +
                ", castSound=" + this.castSound +
                ", isCast=" + this.isCast +
                ", spellRun=" + this.spellRun +
                '}';
    }
    public String toJson() {
        return "{\n" +
                "\"Name\":" + this.name + "\",\n" +
                "\"Handler\":" + this.handler.toString() + "\",\n" +
                "\"Level\":" + this.level + "\",\n" +
                "\"mainElement\":" + this.mainElement.toString() + "\"\n" +
                "\"secondaryElement\":" + this.secondaryElement + "\",\n" +
                "\"castAreaEffect\":" + this.castAreaEffect + "\",\n" +
                "\"spellType\":" + this.spellType + "\",\n" +
                "\"speed\":" + this.speed + "\",\n" +
                "\"travel\":" + this.travel + "\",\n" +
                "}";
    }
    public boolean isCast() {
        return isCast;
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
    public Location getCastLocation() {
        return castLocation;
    }

    public void cast(Action action, Location castLocation, float multiplier) {
        this.castLocation = castLocation;
        this.isCast = true;
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
        double speed = this.speed;
        loc.add(direction.multiply(spellSpeed));
        Spell s = this;
        Sound sound = this.castSound;
        switch (this.castAreaEffect) {
            case DIRECT -> {
                this.spellRun = new BukkitRunnable() {
                    private double distance = 0;
                    @Override
                    public void run() {
                        if (distance > travDis) {
                            s.unCast();
                            cancel();
                            s.castLocation = null;
                        }
                        distance += spellSpeed;
                        loc.add(direction.multiply(spellSpeed));
                        Objects.requireNonNull(loc.getWorld()).playSound(loc, sound, 1, 1);
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
                    private Location sloc = loc;
                    private final List<Integer> hitInts = new ArrayList<>();
                    @Override
                    public void run() {
                        if (distance > travDis) {
                            s.unCast();
                            cancel();
                            s.castLocation = null;
                        }
                        for (int i = 0; i < 6; i++) {
                            Location loca = sloc.clone();
                            loca.setYaw(sloc.getYaw() + (i * 60));
                            loca.setDirection(loc.getDirection().multiply(distance+speed));
                            Objects.requireNonNull(loca.getWorld()).playSound(loca, sound, 1, 1);
                            if (!hitInts.contains(i)) {
                                Objects.requireNonNull(loca.getWorld()).spawnParticle(finalP, loca, Math.round(1 * multiplier), 0, 0, 0, 0);
                                List<Entity> nearbyEntities = (List<Entity>) Objects.requireNonNull(loca.getWorld()).getNearbyEntities(loca.clone().add(0.5, 0.3, 0.5), 0.5, 0.3, 0.5);
                                if (loca.getBlock().getType() != Material.AIR || !nearbyEntities.isEmpty()) {
                                    for (Entity e : nearbyEntities) {
                                        if (e instanceof LivingEntity liv) {
                                            liv.damage(1 * multiplier);
                                            for (int j = 0; j < 10; j++)
                                                Objects.requireNonNull(loca.getWorld()).spawnParticle(finalP, loca, 0, 0, 0.5, 0, 0);
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
                this.spellRun = new BukkitRunnable() {
                    private double distance = 0;
                    private final List<Integer> hitInts = new ArrayList<>();
                    @Override
                    public void run() {
                        if (distance > travDis) {
                            s.unCast();
                            cancel();
                            s.castLocation = null;
                        }
                        for (int i = 0; i < 6; i++) {
                            loc.setYaw(60*i);
                            Objects.requireNonNull(loc.getWorld()).playSound(loc, sound, 1, 1);
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
        }

    }

    public void unCast() {
        this.spellRun.cancel();
        this.isCast = false;
    }

    private void castOnBlock() {

    }

    public void onCollide(SpellCollideEvent event) {

    }
}
