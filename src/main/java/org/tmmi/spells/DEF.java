package org.tmmi.spells;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.tmmi.Element;
import org.tmmi.spells.atributes.AreaEffect;
import org.tmmi.spells.atributes.Weight;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.tmmi.Main.plugin;

public class DEF extends Spell {
    private final Element element;
    private final AreaEffect effect;
    private int holdtime;
    private int durability;
    public DEF(UUID id, UUID handler, String name, Weight weight, int level, int XP, int castCost, Element element, AreaEffect effect, int holdTime, int durability) {
        super(id, handler, name, weight, level, XP, castCost, new Vector(1, 1, 1));
        this.element = element;
        this.effect = effect;
        this.holdtime = holdTime;
        this.durability = durability;
        this.attemptLvlUP();
    }
    public DEF(UUID handler, String name, Weight weight, Element element, AreaEffect effect) {
        this(null, handler, name, weight, 1, 0, 10, element, effect, 5000, 10);
    }

    public Element getElement() {
        return element;
    }

    public AreaEffect getEffect() {
        return effect;
    }

    public int getHoldtime() {
        return holdtime;
    }

    public int getDurability() {
        return durability;
    }
    public void addDurability(int durability) {
        this.durability += durability;
    }

    @Override
    public void onLevelUP() {
        durability = (int) (Math.pow(durability, 2) / 10);
        holdtime = (int) (Math.pow(durability, 2) / 20);
    }

    @Override
    public CastSpell cast(Location castLocation, float multiplier) {
        boolean opE = true;
        Particle p = null;
        switch (element) {
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
        boolean opB = (!Element.getOptimalBiomes(element).contains(castLocation.getWorld().getBiome(castLocation)));
        Particle finalP = p;
        Location loc = castLocation.clone();
        loc.add(castLocation.getDirection().multiply(2));
        Sound sound;
        switch (element) {
            case FIRE -> sound = Sound.BLOCK_FIRE_AMBIENT;
            case EARTH -> sound = Sound.MUSIC_UNDER_WATER;
            case WATER -> sound = Sound.MUSIC_UNDER_WATER;
            default -> sound = Sound.ENTITY_GENERIC_EXPLODE;
        }
        Objects.requireNonNull(loc.getWorld()).playSound(loc, sound, 1, 1);
        switch (effect) {
            case DIRECT -> {
                switch (element) {
                    case EARTH -> {
                        return new CastSpell(this, loc, this.getCastCost()) {
                            @Override
                            public BukkitTask cast(CastSpell casts) {
                                Location mid = loc.clone();
                                Collection<Location> locs = new ArrayList<>();
                                World world = loc.getWorld();
                                for (float i = loc.getYaw() - 45; i < loc.getYaw() + 45; i+=15) {
                                    loc.setYaw(i);
                                    Location l = loc.clone().add(loc.getDirection().normalize().multiply(2));
                                    if (!locs.contains(l)) {
                                        l.add(l);
                                        Location lo = l.clone().subtract(0, 5, 0);
                                        for (int j = 0; j < 5; j++) {
                                            world.getBlockAt(lo.add(0, 1, 0)).setBlockData(l.add(0, 1, 0).getBlock().getBlockData());
                                        }
                                    }
                                }

                                return new BukkitRunnable() {
                                    private int time = 0;
                                    private final World w = loc.getWorld();
                                    private boolean render = true;
                                    private int dur = DEF.this.durability;
                                    @Override
                                    public void run() {

                                        cancel();
                                    }
                                }.runTaskTimer(plugin, 0, 0);
                            }
                        };
                    }
                    default -> {
                        return new CastSpell(this, loc, this.getCastCost()) {
                            @Override
                            public BukkitTask cast(CastSpell casts) {
                                Location mid = loc.clone().add(loc.getDirection().multiply(1.8));
                                Collection<Location> locs = new ArrayList<>();
                                for (float y = loc.getYaw()-60; y <= loc.getYaw()+60; y+=30) {
                                    for (float p = loc.getPitch()-60; p <= loc.getPitch()+60; p+=30) {
                                        double xz = Math.cos(Math.toRadians(p));
                                        Vector v = new Vector(-xz * Math.sin(Math.toRadians(y)), -Math.sin(Math.toRadians(p)), xz * Math.cos(Math.toRadians(y)));
                                        Location sl = loc.clone().add(v.multiply(2));
                                        locs.add(sl.add(v.multiply(Math.pow(sl.distance(mid), 2)/6)));
                                    }
                                }
                                return new BukkitRunnable() {
                                    private int time = 0;
                                    private final World w = loc.getWorld();
                                    private boolean render = true;
                                    private int dur = DEF.this.durability;
                                    @Override
                                    public void run() {
                                        if (time > DEF.this.getHoldtime()) {
                                            casts.uncast();
                                            DEF.this.attemptLvlUP();
                                        }
                                        Collection<CastSpell> cl = new ArrayList<>();
                                        locs.forEach(l -> cl.addAll(CastSpell.getNearbyCasts(l, 0.8, casts)));
                                        for (CastSpell c : cl) {
                                            w.spawnParticle(Particle.CLOUD, c.getLoc(), 3, 0.1, 0.1, 0.1, 0.05);
                                            DEF.this.addXP((int) ((Math.pow(c.getS().getLevel(), 2)/5) + c.getCastCost()));
                                            if (c.getS() instanceof ATK a) dur -= (int) a.getBaseDamage();
                                            c.uncast();
                                            if (dur <= 0) {
                                                casts.uncast();
                                                DEF.this.attemptLvlUP();
                                                break;
                                            }
                                        }
                                        if (render) {
                                            for (float p = loc.getPitch() - 60; p <= loc.getPitch() + 60; p += 3) {
                                                for (float y = loc.getYaw() - 60; y <= loc.getYaw() + 60; y += 3) {
                                                    double xz = Math.cos(Math.toRadians(p));
                                                    Vector v = new Vector(-xz * Math.sin(Math.toRadians(y)), -Math.sin(Math.toRadians(p)), xz * Math.cos(Math.toRadians(y)));
                                                    Location sl = loc.clone().add(v.multiply(2));
                                                    w.spawnParticle(finalP, sl.add(v.multiply(Math.pow(sl.distance(mid), 2) / 6)), 1, 0, 0, 0, 0);
                                                }
                                            }
                                        }
                                        render = !render;
                                        time++;
                                    }
                                }.runTaskTimer(plugin, 0, 0);
                            }
                        };
                    }

                }
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    public ItemStack toItem() {
        ChatColor c;
        switch (element) {
            case AIR -> c = ChatColor.WHITE;
            case FIRE -> c = ChatColor.RED;
            case EARTH -> c = ChatColor.GREEN;
            default -> c = ChatColor.AQUA;
        }
        ChatColor sc;
        switch (element) {
            case AIR -> sc = ChatColor.GRAY;
            case FIRE -> sc = ChatColor.DARK_RED;
            case EARTH -> sc = ChatColor.DARK_GREEN;
            default -> sc = ChatColor.DARK_AQUA;
        }
        ItemStack item = Element.getItem(element);
        ItemMeta m = item.getItemMeta();
        assert m != null;
        m.setDisplayName(c+this.getName() + " (" + this.getWeight() + ")");
        int nxtlxp = this.getXP() - (xpsum(this.getLevel()-1));
        int nxtLvlXP = lvlXPcalc(this.getLevel()-1);
        int perc = (int) (((float) nxtlxp / nxtLvlXP) * 100);
        m.setLore(List.of(c+"Hold time: " + sc + holdtime,
                c + "Durability: " + sc + durability,
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
                "\t\"type\":\"DEF\",\n" +
                "\t\"id\":\"" + this.getId() + "\",\n" +
                "\t\"name\":\"" + this.getName() + "\",\n" +
                "\t\"level\":" + this.getLevel() + ",\n" +
                "\t\"experience\":" + this.getXP() + ",\n" +
                "\t\"cast_cost\":" + this.getCastCost() + ",\n" +
                "\t\"weight\":\"" + this.getWeight() + "\",\n" +
                "\t\"element\":\"" + this.element + "\",\n" +
                "\t\"area_effect\":\"" + this.effect + "\",\n" +
                "\t\"hold_time\":" + this.holdtime + ",\n" +
                "\t\"durability\":" + this.durability + "\n" +
                "}";
    }
}
