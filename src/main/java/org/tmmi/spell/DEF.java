package org.tmmi.spell;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.tmmi.Element;
import org.tmmi.spell.atributes.AreaEffect;
import org.tmmi.spell.atributes.Weight;

import java.util.*;

import static org.tmmi.Main.plugin;

public class DEF extends Spell {
    private final Element element;
    private final AreaEffect effect;
    private int holdtime;
    private int durability;
    public DEF(UUID id, String name, Weight weight, int level, int XP, int castCost, Element element, AreaEffect effect, int holdTime, int durability) {
        super(id, name, weight, Element.AIR, level, XP, castCost);
        this.element = element;
        this.effect = effect;
        this.holdtime = holdTime;
        this.durability = durability;
        this.attemptLvlUP();
    }
    public DEF(String name, Weight weight, Element element, AreaEffect effect) {
        this(null, name, weight, 1, 0, 10, element, effect, 5000, 10);
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
    public CastSpell cast(@NotNull Location castLocation, float multiplier, Entity e) {
        Player player = (Player) e;
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
        return new CastSpell(this, loc, this.getCc()) {
            @Override
            public Thread cast() {
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

                return new Thread() {
                    private int time = 0;
                    private final World w = loc.getWorld();
                    private boolean render = true;
                    private int dur = DEF.this.durability;
                    @Override
                    public void run() {
                        interrupt();
                    }
                };
            }
        };
    }
    @Contract(pure = true)
    private @NotNull @Unmodifiable List<String> extraItemLore() {
        final ChatColor c = switch (this.element) {
            case AIR -> ChatColor.WHITE;
            case FIRE -> ChatColor.RED;
            case EARTH -> ChatColor.GREEN;
            default -> ChatColor.AQUA;
        };
        final ChatColor sc = switch (this.element) {
            case AIR -> ChatColor.GRAY;
            case FIRE -> ChatColor.DARK_RED;
            case EARTH -> ChatColor.DARK_GREEN;
            default -> ChatColor.DARK_AQUA;
        };
        return List.of(c+"Hold time: " + sc + holdtime,
                c + "Durability: " + sc + durability);
    }

    @Override
    public String toJson() {
        return  "\t\t{\n" +
                "\t\"type\":\"DEF\",\n" +
                "\t\"id\":\"" + this.getId() + "\",\n" +
                "\t\"name\":\"" + this.getName() + "\",\n" +
                "\t\"level\":" + this.getLevel() + ",\n" +
                "\t\"experience\":" + this.getXP() + ",\n" +
                "\t\"cast_cost\":" + this.getCc() + ",\n" +
                "\t\"weight\":\"" + this.getWeight() + "\",\n" +
                "\t\"element\":\"" + this.element + "\",\n" +
                "\t\"area_effect\":\"" + this.effect + "\",\n" +
                "\t\"hold_time\":" + this.holdtime + ",\n" +
                "\t\"durability\":" + this.durability + "\n" +
                "}";
    }
}
