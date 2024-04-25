package org.tmmi;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.events.SpellCollideEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.tmmi.Main.isSim;
import static org.tmmi.Main.plugin;

public class Spell {
    public static List<Spell> spells = new ArrayList<>();

    public enum SpellType {
        CANTRIP,
        SORCERY,
        INCANTATION;
        @Contract(pure = true)
        public static SpellType getSpellType(@NotNull String type) {
            SpellType s = null;
            switch (type) {
                case "CANTRIP" -> s = CANTRIP;
                case "SORCERY" -> s = SORCERY;
                case "AREA_EFFECT" -> s = INCANTATION;
            }
            return s;
        }
    }

    public enum CastAreaEffect {
        DIRECT,
        WIDE,
        AREA;
        public static ItemStack DIRECT_ITEM;
        public static ItemStack WIDE_ITEM;
        public static ItemStack AREA_ITEM;
        @Contract(pure = true)
        public static CastAreaEffect getAreaEffect(@NotNull String type) {
            CastAreaEffect c = null;
            switch (type) {
                case "DIRECT" -> c = DIRECT;
                case "WIDE_RANGE" -> c = WIDE;
                case "AREA_EFFECT" -> c = AREA;
            }
            return c;
        }
        @Contract(pure = true)
        public static @Nullable CastAreaEffect getAreaEffect(ItemStack item) {
            if (isSim(item, DIRECT_ITEM)) {
                return DIRECT;
            } else if (isSim(item, WIDE_ITEM)) {
                return WIDE;
            } else if (isSim(item, AREA_ITEM)) {
                return AREA;
            }
            return null;
        }
    }

    public enum SpellAtribute {
        CHARGE
    }

    public enum Element {
        FIRE,
        EARTH,
        WATER,
        AIR;
        public static ItemStack FIRE_ITEM;
        public static ItemStack EARTH_ITEM;
        public static ItemStack WATER_ITEM;
        public static ItemStack AIR_ITEM;
        @Contract(pure = true)
        public static Element getElement(@NotNull String element) {
            Element e = null;
            switch (element) {
                case "FIRE" -> e = FIRE;
                case "EARTH" -> e = EARTH;
                case "WATER" -> e = WATER;
                case "AIR" -> e = AIR;
            }
            return e;
        }
        @Contract(pure = true)
        public static @Nullable Element getElement(ItemStack item) {
            Element e = null;
            if (isSim(item, FIRE_ITEM)) {
                e = FIRE;
            } else if (isSim(item, EARTH_ITEM)) {
                e = EARTH;
            } else if (isSim(item, WATER_ITEM)) {
                e = WATER;
            } else if (isSim(item, AIR_ITEM)) {
                e = AIR;
            }
            return e;
        }
    }

    private final UUID id;
    private final UUID handler;
    private final String name;
    private final CastAreaEffect castAreaEffect;
    private final Element mainElement;
    private final Element secondaryElement;
    private final int level;
    private final int castCost;
    private final SpellType spellType;
    private final double travel;
    private final double speed;
    private double baseDamage;
    private final Sound castSound;
    private boolean isCast;
    private Location castLocation;

    private BukkitTask spellRun;

    public Spell(UUID handler, String name, @NotNull Element mainElement, Element secondaryElement, @NotNull CastAreaEffect castAreaEffect, int usedMagicules) {
        this(Main.newUUID(Main.TMMIobject.SPELL), handler, name, 1, 10, mainElement, secondaryElement, castAreaEffect, SpellType.CANTRIP, (double) usedMagicules /100, 1, 10);
    }
    public Spell(UUID id, UUID handler, String name, int level, int castCost, @NotNull Element mainElement, Element secondaryElement, CastAreaEffect castAreaEffect, SpellType spellType, double baseDamage, double speed, double travel) {
        this.id = id;
        this.handler = handler;
        this.name = name;
        this.castCost = castCost;
        this.castAreaEffect = castAreaEffect;
        this.mainElement = mainElement;
        this.secondaryElement = secondaryElement;
        this.level = level;
        this.spellType = spellType;
        this.travel = travel;
        this.speed = speed;
        Sound castSpund1 = null;
        switch (mainElement) {
            case FIRE -> castSpund1 = Sound.BLOCK_FIRE_AMBIENT;
            case EARTH -> castSpund1 = Sound.MUSIC_UNDER_WATER;
            case WATER -> castSpund1 = Sound.MUSIC_UNDER_WATER;
            case AIR -> castSpund1 = Sound.ENTITY_GENERIC_EXPLODE;
        }
        this.castSound = castSpund1;
        this.isCast = false;
        this.baseDamage = baseDamage;
        spells.add(this);
    }
    public Spell(UUID handler, String name, @NotNull Spell.Element mainElement, CastAreaEffect castAreaEffect, int usedMagicules) {
        this(handler, name, mainElement, null, castAreaEffect, 6);
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
    public boolean isCast() {
        return isCast;
    }
    public CastAreaEffect getCastAreaEffect() {
        return castAreaEffect;
    }
    public Element getMainElement() {
        return mainElement;
    }
    public Element getSecondaryElement() {
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

    public double getBaseDamage() {
        return baseDamage;
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
            case WIDE -> {
                this.spellRun = new BukkitRunnable() {
                    private double distance = 0;
                    private final Location sloc = loc;
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
            case AREA -> {
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

    @Override
    public String toString() {
        return "Spell{" +
                "id=" + id +
                ", handler=" + handler +
                ", name='" + name + '\'' +
                ", castAreaEffect=" + castAreaEffect +
                ", mainElement=" + mainElement +
                ", secondaryElement=" + secondaryElement +
                ", level=" + level +
                ", castCost=" + castCost +
                ", spellType=" + spellType +
                ", travel=" + travel +
                ", speed=" + speed +
                ", baseDamage=" + baseDamage +
                ", castSound=" + castSound +
                ", isCast=" + isCast +
                ", castLocation=" + castLocation +
                ", spellRun=" + spellRun +
                '}';
    }
    public String toJson() {
        return "{\n" +
                "\t\"id\":\"" + this.id + "\",\n" +
                "\t\"name\":\"" + this.name + "\",\n" +
                "\t\"level\":" + this.level + ",\n" +
                "\t\"cast_cost\":" + this.castCost + ",\n" +
                "\t\"main_element\":\"" + this.mainElement + "\",\n" +
                "\t\"secondary_element\":\"" + this.secondaryElement + "\",\n" +
                "\t\"cast_area_effect\":\"" + this.castAreaEffect + "\",\n" +
                "\t\"spell_type\":\"" + this.spellType + "\",\n" +
                "\t\"base_damage\":" + this.baseDamage + ",\n" +
                "\t\"speed\":" + this.speed + ",\n" +
                "\t\"travel\":" + this.travel + "\n" +
                "}";
    }
}
