package org.tmmi.spell;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.*;
import org.tmmi.spell.atributes.Weight;

import java.util.List;
import java.util.UUID;

import static org.hetils.Util.nearestEntity;
import static org.tmmi.Main.*;

public class STI extends Spell {
    public enum Stat {
        DMG,
        MARG;

        @Contract(pure = true)
        public static @Nullable Stat get(@NotNull String util) {
            switch (util.toUpperCase()) {
                case "DMG" -> {
                    return DMG;
                }
                case "MARG" -> {
                    return MARG;
                }
                default -> {
                    return null;
                }
            }
        }
    }

    private final Stat stat;
    private int effectTime;
    private double multiplier;
    public STI(@NotNull Stat stat, UUID id, int level, int XP, int castCost, int effectTime, double multiplier) {
        super(id, "Stat inc", Weight.CANTRIP, Element.EARTH,level, XP, castCost);
        this.stat = stat;
        this.effectTime = effectTime;
        this.multiplier = multiplier;
        this.attemptLvlUP();
    }
    public STI(Stat stat) {
        this(stat, null, 1, 0, 10, 10000, 1.1);
    }

    @Override
    public void onLevelUP() {
        this.effectTime += 500;
        this.multiplier += 0.05;
    }

    @Override
    public CastSpell cast(@NotNull Location castLocation, float multiplier, Entity e) {
        Player p = (Player) e;
        Location loc = castLocation.clone();
        double multi = this.multiplier;
        log(multi);
        int eft = this.effectTime;
        switch (stat) {
            case DMG -> {
                for (int i = 0; i < 20; i++) {
                    loc.add(loc.getDirection());
                    Entity ne = nearestEntity(loc, 0.25, List.of(p));
                    if (ne != null) {
                        EntityMultiplier em = EntityMultiplier.getOrNew(ne);
                        em.addDmg(multi);
                        BukkitTask task = new BukkitRunnable() {
                            @Override
                            public void run() {
                                ne.getWorld().spawnParticle(Particle.COMPOSTER,
                                        ne.getLocation().add(0, ne.getLocation().getY() - ne.getBoundingBox().getCenterY() + (ne.getBoundingBox().getHeight()), 0),
                                        5, ne.getBoundingBox().getWidthX()/4, ne.getBoundingBox().getHeight()/4, ne.getBoundingBox().getWidthZ()/4, 0.1);
                            }
                        }.runTaskTimer(plugin, 0, 0);
                        newThread(() -> {
                            try {
                                Thread.sleep(eft);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            ne.setGlowing(false);
                            em.subDmg(multi);
                            task.cancel();
                            STI.this.addXP((int) Math.floor((eft*multi)/10000));
                        }).start();
                        break;
                    }
                }
            }
        }
        return null;
    }

    @Contract(value = " -> new", pure = true)
    private @NotNull List<String> extraItemLore() {
        ChatColor c;
        switch (stat) {
            default -> c = ChatColor.AQUA;
        }
        ChatColor sc;
        switch (stat) {
            default -> sc = ChatColor.DARK_AQUA;
        }
        return List.of(c + "Multiplier: " + sc + this.multiplier + 'x',
                c + "Effect time: " + sc + ((double) this.effectTime / 1000) + 's');

    }

    @Override
    public String toJson() {
        return  "\t\t{\n" +
                "\t\"type\":\"STI\",\n" +
                "\t\"id\":\"" + this.getId() + "\",\n" +
                "\t\"name\":\"" + this.getName() + "\",\n" +
                "\t\"level\":" + this.getLevel() + ",\n" +
                "\t\"experience\":" + this.getXP() + ",\n" +
                "\t\"cast_cost\":" + this.getCc() + ",\n" +
                "\t\"stat\":\"" + this.stat + "\",\n" +
                "\t\"effect_time\":" + this.effectTime + ",\n" +
                "\t\"multiplier\":" + this.multiplier + "\n" +
                "}";
    }
}
