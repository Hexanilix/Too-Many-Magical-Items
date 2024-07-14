package org.tmmi.spell;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Element;
import org.tmmi.MagicChunk;
import org.tmmi.spell.atributes.AreaEffect;
import org.tmmi.spell.atributes.Weight;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

import static org.tmmi.Main.*;

public abstract class ATK extends Spell {
    final AreaEffect areaEffect;
    final Element mainElement;
    final Element secondaryElement;
    double travel;
    double speed;
    double baseDamage;
    boolean phase;
    public ATK(UUID id, String name, Weight w, int level, int XP, int castcost,
               @NotNull Element mainElement, Element secondaryElement, @NotNull AreaEffect areaEffect,
               double speed, double travel, double baseDamage, boolean phase) {
        super(id, name, w, mainElement, level, XP, castcost);
        this.mainElement = mainElement;
        this.secondaryElement = secondaryElement;
        this.areaEffect = areaEffect;
        this.speed = speed;
        this.travel = travel;
        this.baseDamage = baseDamage;
        this.phase = phase;
        this.attemptLvlUP();
    }
    public ATK(UUID id, String name, Weight w, int level, int XP, int castcost,
               @NotNull Element mainElement, Element secondaryElement, @NotNull AreaEffect areaEffect,
               double speed, double travel, double baseDamage) {
        this(id, name, w, level, XP, castcost, mainElement, secondaryElement, areaEffect, speed, travel, baseDamage, false);
    }
    public ATK(String name, Weight w, @NotNull Element mainElement, Element secondaryElement, @NotNull AreaEffect areaEffect) {
        this(null, name, w, 1, 0, 3, mainElement, secondaryElement, areaEffect, 1, 10, 2, false);
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
    public abstract CastSpell cast(@NotNull Location castLocation, float multiplier, Entity e);
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

    private List<String> extraItemLore() {
        final ChatColor c = switch (this.getElement()) {
            case AIR -> ChatColor.WHITE;
            case FIRE -> ChatColor.RED;
            case EARTH -> ChatColor.GREEN;
            case WATER -> ChatColor.AQUA;
        };
        final ChatColor sc = switch (this.getElement()) {
            case AIR -> ChatColor.GRAY;
            case FIRE -> ChatColor.DARK_RED;
            case EARTH -> ChatColor.DARK_GREEN;
            case WATER -> ChatColor.DARK_AQUA;
        };
        return List.of(sc + "Damage: " + c + this.baseDamage,
                sc + "Range: " + c + this.travel + " Blocks",
                sc + "Speed: " + c + this.speed + " B/s");
    }

    @Override
    public String toJson() {
        return  "\t\t{\n" +
                "\t\"type\":\"ATK\",\n" +
                "\t\"id\":\"" + this.getId() + "\",\n" +
                "\t\"name\":\"" + this.getName() + "\",\n" +
                "\t\"level\":" + this.getLevel() + ",\n" +
                "\t\"experience\":" + this.getXP() + ",\n" +
                "\t\"cast_cost\":" + this.getCc() + ",\n" +
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

    public AreaEffect getAreaEffect() {
        return areaEffect;
    }
}
