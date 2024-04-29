package org.tmmi;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.events.SpellCollideEvent;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

import static org.tmmi.Main.*;

public class Spell {
    public static List<Spell> spells = new ArrayList<>();
    public static ArrayList<UUID> disabled = new ArrayList<>();

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
        public static final ItemStack DIRECT_ITEM = newItem(Material.WRITABLE_BOOK, ChatColor.DARK_AQUA + "Direct", 824574);
        public static final ItemStack WIDE_ITEM = newItem(Material.WRITABLE_BOOK, ChatColor.DARK_AQUA + "Wide", 824575);
        public static final ItemStack AREA_ITEM = newItem(Material.WRITABLE_BOOK, ChatColor.DARK_AQUA + "Area", 824576);
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
            if (isSim(item, DIRECT_ITEM)) return DIRECT;
            else if (isSim(item, WIDE_ITEM)) return WIDE;
            else if (isSim(item, AREA_ITEM)) return AREA;
            return null;
        }
        public static @Nullable ItemStack getItem(CastAreaEffect effect) {
            if (effect == null) return null;
            switch (effect) {
                case DIRECT -> {
                    return DIRECT_ITEM;
                }
                case AREA -> {
                    return AREA_ITEM;
                }
                default -> {
                    return WIDE_ITEM;
                }
            }
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
        public static final ItemStack FIRE_ITEM = newItem(Material.FIRE_CHARGE, ChatColor.DARK_AQUA + "Fire", 245723);
        public static final ItemStack EARTH_ITEM = newItem(Material.GRASS_BLOCK, ChatColor.DARK_GREEN + "Earth", 245724);
        public static final ItemStack WATER_ITEM = newItem(Material.WATER_BUCKET, ChatColor.DARK_GREEN + "Water", 245725);
        public static final ItemStack AIR_ITEM = newItem(Material.FEATHER, ChatColor.WHITE + "Air", 245726);
        public static final List<Biome> FIRE_BIOMES = new ArrayList<>();
        public static final List<Biome> EARTH_BIOMES = new ArrayList<>();
        public static final List<Biome> WATER_BIOMES = new ArrayList<>();
        public static final List<Biome> AIR_BIOMES = new ArrayList<>();
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
            if (isSim(item, FIRE_ITEM)) return FIRE;
            else if (isSim(item, EARTH_ITEM)) return EARTH;
            else if (isSim(item, WATER_ITEM)) return WATER;
            else if (isSim(item, AIR_ITEM)) return AIR;
            return null;
        }
        @Contract(pure = true)
        public static ItemStack getItem(Element element) {
            ItemStack i = null;
            if (element != null)
                switch (element) {
                    case AIR -> i = AIR_ITEM;
                    case EARTH -> i = EARTH_ITEM;
                    case WATER -> i = WATER_ITEM;
                    default -> i = FIRE_ITEM;
                }
            return i;
        }

        @Contract(pure = true)
        public static List<Biome> getOptimalBiomes(Element element) {
            List<Biome> b = new ArrayList<>();
            if (element != null)
                switch (element) {
                    case AIR -> b = AIR_BIOMES;
                    case EARTH -> b = EARTH_BIOMES;
                    case WATER -> b = WATER_BIOMES;
                    default -> b = FIRE_BIOMES;
                }
            return b;
        }
    }

    private final UUID id;
    private final UUID handler;
    private final String name;
    private final CastAreaEffect castAreaEffect;
    private final Element mainElement;
    private final Element secondaryElement;
    private int level;
    private final int castCost;
    private final SpellType spellType;
    private double travel;
    private double speed;
    private double baseDamage;
    private final Sound castSound;
    private boolean isCast;
    private Location castLocation;
    private int XP;

    private BukkitTask spellRun;

    private @NotNull UUID uuid(CastAreaEffect effect, @NotNull Element main, Element second) {
        String u = UUID_SEQUENCE + IntToHex(spells.size(), 4) +
                '-' +
                toHex(effect, 4) +
                '-' +
                toHex(main.name(), 4) +
                '-' +
                (second != null ? toHex(second, 4) : "0000") +
                String.valueOf(digits.charAt(new Random().nextInt(16))).repeat(8);
        return UUID.fromString(u);
    }
    public Spell(UUID handler, String name, @NotNull Element mainElement, Element secondaryElement, @NotNull CastAreaEffect castAreaEffect, int usedMagicules) {
        this(null, handler, name, 1, 0, 10, mainElement, secondaryElement, castAreaEffect, SpellType.CANTRIP, (double) usedMagicules /100, 0.3, 4);
    }
    public Spell(UUID id, UUID handler, String name, int level, int XP, int castCost, @NotNull Element mainElement, Element secondaryElement, CastAreaEffect castAreaEffect, SpellType spellType, double baseDamage, double speed, double travel) {
        this.id = (id == null ? uuid(castAreaEffect, mainElement, secondaryElement) : id);
        this.handler = handler;
        this.name = name;
        this.castCost = castCost;
        this.castAreaEffect = castAreaEffect;
        this.mainElement = mainElement;
        this.secondaryElement = secondaryElement;
        this.level = level;
        this.XP = XP;
        this.spellType = spellType;
        this.travel = travel;
        this.speed = speed;
        switch (mainElement) {
            case FIRE -> this.castSound = Sound.BLOCK_FIRE_AMBIENT;
            case EARTH -> this.castSound = Sound.MUSIC_UNDER_WATER;
            case WATER -> this.castSound = Sound.MUSIC_UNDER_WATER;
            default -> this.castSound = Sound.ENTITY_GENERIC_EXPLODE;
        }
        this.isCast = false;
        this.baseDamage = baseDamage;
        if (!disabled.contains(this.id)) spells.add(this);
    }
    public Spell(UUID handler, String name, @NotNull Spell.Element mainElement, CastAreaEffect castAreaEffect, int usedMagicules) {
        this(handler, name, mainElement, null, castAreaEffect, usedMagicules);
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
        if (XP - lvlXPcalc(this.level-1) >= lvlXPcalc(this.level)) {
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
        log("Cast spell " + this.name);
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
                p = Particle.BLOCK_DUST;
            }
            case WATER -> {
                p = Particle.WATER_DROP;
            }
        }
        boolean opB = (!Element.getOptimalBiomes(this.mainElement).contains(castLocation.getWorld().getBiome(castLocation)));
        this.XP += Math.round((float) (this.level * this.level) /10)+1;
        Particle finalP = p;
        double travDis = this.travel + (opE ? 1 : 0) + (opB ? 1.7 : 0);
        double spellSpeed = BigDecimal.valueOf((this.speed - Math.floor(this.speed)) / 2).round(MathContext.DECIMAL32).doubleValue() + 0.3;
        Vector direction = castLocation.getDirection();
        Location loc = castLocation.clone();
        double speed = this.speed;
        loc.add(direction.multiply(spellSpeed));
        Spell s = this;
        Sound sound = this.castSound;
        int tick = (int) Math.round(5 - this.speed + 0);
        switch (this.castAreaEffect) {
            case DIRECT -> {
                this.spellRun = new BukkitRunnable() {
                    private double distance = 0;
                    @Override
                    public void run() {
                        if (distance > travDis) {
                            s.uncast();
                            cancel();
                            s.castLocation = null;
                        }
                        log(spellSpeed);
                        distance += spellSpeed;
                        loc.add(direction.multiply(spellSpeed));
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
                }.runTaskTimer(plugin, 0, tick);
            }
            case WIDE -> {
                this.spellRun = new BukkitRunnable() {
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
                }.runTaskTimer(plugin, 0, tick);
            }
            case AREA -> {
                this.spellRun = new BukkitRunnable() {
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
                }.runTaskTimer(plugin, 0, tick);
            }
        }
        event.getPlayer().sendMessage("Gained " + (this.XP - bxp) + " xp");
        attemptLvlUP();
    }

    public void uncast() {
        this.spellRun.cancel();
        this.isCast = false;
    }

    private void castOnBlock() {

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
        int nxtlxp = XP - (lvlXPcalc(this.level-1));
        int nxtLvlXP = lvlXPcalc(this.level);
        int perc = (int) (((float) nxtlxp / nxtLvlXP) * 100);
        m.setLore(List.of(c+"Level "+this.level, c + "[" +
                        "-".repeat(Math.max(0, perc/10)) +
                        sc + "-".repeat(Math.max(0, 10 - perc/10)) +
                        c + "]" + (nxtlxp >= 1000 ? BigDecimal.valueOf((float) nxtlxp / 1000).setScale(2, RoundingMode.HALF_EVEN) + "k" : nxtlxp) +
                        "/" + (nxtLvlXP >= 1000 ? BigDecimal.valueOf((float) nxtLvlXP / 1000).setScale(2, RoundingMode.HALF_EVEN) + "k" : nxtLvlXP),
                ChatColor.GRAY + "Total XP: " + this.XP));
        item.setItemMeta(m);
        return item;
    }
    private static int lvlXPcalc(int lvl) {
        int j = (lvl > 0 ? 10 : 0);
        for (int i = 1; i < lvl; i++) j = (int) (j * 1.5);
        return j;
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
