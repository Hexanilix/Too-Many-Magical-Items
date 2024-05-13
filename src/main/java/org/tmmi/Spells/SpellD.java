package org.tmmi.Spells;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.Element;
import org.tmmi.Main;
import org.tmmi.events.SpellCollideEvent;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

import static org.tmmi.Main.*;

public class SpellD {
    public static List<SpellD> spells = new ArrayList<>();
    public static ArrayList<UUID> disabled = new ArrayList<>();
    public static double mxT = 20;
    public static double mxS = 20;
    public static double mxD = 20;

    public static @Nullable SpellD getSpell(UUID id) {
        for (SpellD s : spells)
            if (id == s.getId())
                return s;
        return null;
    }





    public enum SpellAtribute {
        CHARGE
    }

    final UUID id;
    final UUID handler;
    final String name;
    final AreaEffect areaEffect;
    final Element mainElement;
    final Element secondaryElement;
    int level;
    final int castCost;
    final Weight weight;
    double travel;
    double speed;
    double baseDamage;
    boolean isCast;
    Location castLocation;
    int XP;
    final List<BukkitTask> spellRun = new ArrayList<>();

    private @NotNull UUID uuid(AreaEffect effect, @NotNull Element main, Element second) {
        String u = UUID_SEQUENCE + IntToHex(spells.size(), 4) +
                '-' +
                toHex(effect, 4) +
                '-' +
                toHex(main.name(), 4) +
                '-' +
                (second != null ? toHex(second, 4) : "0000") +
                String.valueOf(Main.digits.charAt(new Random().nextInt(16))).repeat(8);
        return UUID.fromString(u);
    }
    public SpellD(UUID handler, String name, @NotNull Element mainElement, Element secondaryElement, @NotNull AreaEffect areaEffect, int usedMagicules) {
        this(null, handler, name, 1, 0, 10, mainElement, secondaryElement, areaEffect, Weight.CANTRIP, (double) usedMagicules /100, 0.7, 5);
    }
    public SpellD(UUID id, UUID handler, String name, int level, int XP, int castCost, @NotNull Element mainElement, Element secondaryElement, AreaEffect areaEffect, Weight weight, double baseDamage, double speed, double travel) {
        this.id = (id == null ? uuid(areaEffect, mainElement, secondaryElement) : id);
        this.handler = handler;
        this.name = name;
        this.castCost = castCost;
        this.areaEffect = areaEffect;
        this.mainElement = mainElement;
        this.secondaryElement = secondaryElement;
        assert level > 0;
        this.level = level;
        this.XP = XP;
        this.weight = weight;
        assert travel > 0;
        assert speed > 0;
        this.travel = Math.min(travel, mxT);
        this.speed = Math.min(speed, mxS);
        this.isCast = false;
        this.baseDamage = Math.min(baseDamage, mxD);
        if (!disabled.contains(this.id)) spells.add(this);
    }
    public SpellD(UUID handler, String name, @NotNull Element mainElement, AreaEffect areaEffect, int usedMagicules) {
        this(handler, name, mainElement, null, areaEffect, usedMagicules);
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
    public AreaEffect getCastAreaEffect() {
        return areaEffect;
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
    public Weight getType() {
        return weight;
    }
    public int getCastCost() {
        return castCost;
    }
    public double getSpeed() {
        return speed;
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

    private void attemptLvlUP() {
        if (XP - xpsum(this.level-1) >= lvlXPcalc(this.level-1)) {
            this.level++;
            if (Bukkit.getPlayer(this.handler) != null) Bukkit.getPlayer(this.handler).sendMessage(this.name + " upgraded to level " + this.level);
            float margin = (this.level % 5 == 0 ? 0.03F : 0.01F);
            this.speed = Math.min(this.speed + margin, 10);
            this.travel += margin * 2;
            this.baseDamage += margin * 2;
        }
    }

    public void cast(PlayerInteractEvent event, Location castLocation, float multiplier) {
        int bxp = this.XP;
        this.castLocation = castLocation;
        this.isCast = true;
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
        this.XP += Math.round((float) (this.level * this.level) /15)+1;
        Particle finalP = p;
        double travDis = this.travel + (opE ? 0.7 : 0) + (opB ? 1 : 0);
        double spellSpeed = BigDecimal.valueOf((this.speed - Math.floor(this.speed)) / 2).round(MathContext.DECIMAL32).doubleValue() + 0.3;
        Vector direction = castLocation.getDirection();
        Location loc = castLocation.clone();
        double speed = this.speed;
        loc.add(direction.multiply(spellSpeed));
        SpellD s = this;
        Sound sound;
        switch (mainElement) {
            case FIRE -> sound = Sound.BLOCK_FIRE_AMBIENT;
            case EARTH -> sound = Sound.MUSIC_UNDER_WATER;
            case WATER -> sound = Sound.MUSIC_UNDER_WATER;
            default -> sound = Sound.ENTITY_GENERIC_EXPLODE;
        }
        int tick = (int) Math.round(5 - this.speed + 0);
        switch (this.areaEffect) {
            case DIRECT -> {
                this.spellRun.add(new BukkitRunnable() {
                    private double distance = 0;
                    @Override
                    public void run() {
                        if (distance > travDis) {
                            s.uncast();
                            cancel();
                            s.castLocation = null;
                        }
                        distance += spellSpeed;
                        loc.add(direction);
                        Objects.requireNonNull(loc.getWorld()).playSound(loc, sound, 1, 1);
                        Objects.requireNonNull(loc.getWorld()).spawnParticle(finalP, loc, Math.round(1*multiplier), 0, 0, 0, 0);
                        List<Entity> nearbyEntities = (List<Entity>) Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc.clone().add(0.5, 0.3, 0.5), 0.5, 0.3, 0.5);
                        if (loc.getBlock().getType() != Material.AIR || !nearbyEntities.isEmpty()) {
                            for (Entity e : nearbyEntities) {
                                if (e instanceof LivingEntity liv) {
                                    if (liv.getUniqueId() == event.getPlayer().getUniqueId()) continue;
                                    liv.damage(s.getBaseDamage() * multiplier);
                                    Objects.requireNonNull(loc.getWorld()).spawnParticle(finalP, loc, 10, 1, 1, 1, 0.3);
                                    s.uncast();
                                    cancel();
                                }
                            }
                        }
                        s.attemptLvlUP();
                    }
                }.runTaskTimer(plugin, 0, tick));
            }
            case WIDE -> {
                this.spellRun.add(new BukkitRunnable() {
                    private double distance = 0;
                    private final Location sloc = loc;
                    private final List<Integer> hitInts = new ArrayList<>();
                    @Override
                    public void run() {
                        if (distance > travDis) {
                            s.uncast();
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
                        s.attemptLvlUP();
                        distance += spellSpeed;
                        loc.add(direction.multiply(spellSpeed));
                    }
                }.runTaskTimer(plugin, 0, tick));
            }
            case AREA -> {
                this.spellRun.add(new BukkitRunnable() {
                    private double distance = 0;
                    private final List<Integer> hitInts = new ArrayList<>();
                    @Override
                    public void run() {
                        if (distance > travDis) {
                            s.uncast();
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
                            s.attemptLvlUP();
                        }
                        distance += spellSpeed;
                        loc.add(direction.multiply(spellSpeed));
                    }
                }.runTaskTimer(plugin, 0, tick));
            }
        }
        event.getPlayer().sendMessage("Gained " + (this.XP - bxp) + " xp");
        attemptLvlUP();
    }

    public void uncast() {
        this.isCast = false;
    }

    public void onCollide(SpellCollideEvent event) {

    }

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
        m.setDisplayName(c+this.name);
        int nxtlxp = XP - (xpsum(this.level-1));
        int nxtLvlXP = lvlXPcalc(this.level-1);
        int perc = (int) (((float) nxtlxp / nxtLvlXP) * 100);
        m.setLore(List.of(sc+"Level "+c+this.level, c + "[" +
                        "-".repeat(Math.max(0, perc/10)) +
                        sc + "-".repeat(Math.max(0, 10 - perc/10)) +
                        c + "]" + (nxtlxp >= 1000 ? BigDecimal.valueOf((float) nxtlxp / 1000).setScale(2, RoundingMode.HALF_EVEN) + "k" : nxtlxp) +
                        sc + "/" + c + (nxtLvlXP >= 1000 ? BigDecimal.valueOf((float) nxtLvlXP / 1000).setScale(2, RoundingMode.HALF_EVEN) + "k" : nxtLvlXP),
                ChatColor.GRAY + "Total XP: " + this.XP));
        item.setItemMeta(m);
        return item;
    }
    private int xpsum(int lvl) {
        int s = 0;
        for (int i = 0; i < lvl; i++) s += lvlXPcalc(i);
        return s;
    }
    private static int lvlXPcalc(int lvl) {
        return (int) (lvl < 0 ? 0 : Math.round(((Math.pow(14, 1+lvl))/Math.pow(10, lvl)-4)));
    }


    @Override
    public String toString() {
        return "Spell{" +
                "id=" + id +
                ", handler=" + handler +
                ", name='" + name + '\'' +
                ", castAreaEffect=" + areaEffect +
                ", mainElement=" + mainElement +
                ", secondaryElement=" + secondaryElement +
                ", level=" + level +
                ", castCost=" + castCost +
                ", spellType=" + weight +
                ", travel=" + travel +
                ", speed=" + speed +
                ", baseDamage=" + baseDamage +
                ", isCast=" + isCast +
                ", castLocation=" + castLocation +
                ", XP=" + XP +
                ", spellRun=" + spellRun +
                '}';
    }

    public String toJson() {
        return "\t\t{\n" +
                "\t\t\t\"id\":\"" + this.id + "\",\n" +
                "\t\t\t\"name\":\"" + this.name + "\",\n" +
                "\t\t\t\"level\":" + this.level + ",\n" +
                "\t\t\t\"experience\":" + this.XP + ",\n" +
                "\t\t\t\"cast_cost\":" + this.castCost + ",\n" +
                "\t\t\t\"main_element\":\"" + this.mainElement + "\",\n" +
                "\t\t\t\"secondary_element\":\"" + this.secondaryElement + "\",\n" +
                "\t\t\t\"cast_area_effect\":\"" + this.areaEffect + "\",\n" +
                "\t\t\t\"spell_type\":\"" + this.weight + "\",\n" +
                "\t\t\t\"base_damage\":" + this.baseDamage + ",\n" +
                "\t\t\t\"speed\":" + this.speed + ",\n" +
                "\t\t\t\"travel\":" + this.travel + "\n" +
                "\t\t}";
    }
}
