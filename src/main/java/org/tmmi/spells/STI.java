package org.tmmi.spells;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.*;
import org.tmmi.spells.atributes.Weight;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static org.tmmi.Main.log;
import static org.tmmi.Main.plugin;

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
    public STI(@NotNull Stat stat, UUID id, UUID handler, int level, int XP, int castCost, int effectTime, double multiplier) {
        super(id, handler, "Stat inc", Weight.CANTRIP, level, XP, castCost, null);
        this.stat = stat;
        this.effectTime = effectTime;
        this.multiplier = multiplier;
        this.attemptLvlUP();
    }
    public STI(Stat stat, UUID handler) {
        this(stat, null, handler, 1, 0, 10, 10000, 1.1);
    }

    @Override
    public void onLevelUP() {
        this.effectTime += 500;
        this.multiplier += 0.05;
    }

    @Override
    public CastSpell cast(@NotNull PlayerInteractEvent event, @NotNull Location castLocation, float multiplier) {
        Player p = event.getPlayer();
        Location loc = castLocation.clone();
        double multi = this.multiplier;
        log(multi);
        int eft = this.effectTime;
        switch (stat) {
            case DMG -> {
                for (int i = 0; i < 20; i++) {
                    loc.add(loc.getDirection());
                    Entity e = Main.nearestEntity(loc, 0.25, List.of(p));
                    if (e != null) {
                        EntityMultiplier em = EntityMultiplier.getOrNew(e);
                        em.addDmg(multi);
                        BukkitTask task = new BukkitRunnable() {
                            @Override
                            public void run() {
                                e.getWorld().spawnParticle(Particle.COMPOSTER,
                                        e.getLocation().add(0, e.getLocation().getY() - e.getBoundingBox().getCenterY() + (e.getBoundingBox().getHeight()), 0), 
                                        5, e.getBoundingBox().getWidthX()/4, e.getBoundingBox().getHeight()/4, e.getBoundingBox().getWidthZ()/4, 0.1);
                            }
                        }.runTaskTimer(plugin, 0, 0);

                        new Thread(() -> {
                            try {
                                Thread.sleep(eft);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            e.setGlowing(false);
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

    @Override
    public ItemStack toItem() {
        ChatColor c;
        switch (stat) {
            default -> c = ChatColor.AQUA;
        }
        ChatColor sc;
        switch (stat) {
            default -> sc = ChatColor.DARK_AQUA;
        }
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta m = item.getItemMeta();
        assert m != null;
        m.setDisplayName(c+this.getName());
        int nxtlxp = this.getXP() - (xpsum(this.getLevel()-1));
        int nxtLvlXP = lvlXPcalc(this.getLevel()-1);
        int perc = (int) (((float) nxtlxp / nxtLvlXP) * 100);
        m.setLore(List.of(c + "Multiplier: " + sc + this.multiplier + 'x',
                c + "Effect time: " + sc + ((double) this.effectTime / 1000) + 's',
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
                "\t\"type\":\"STI\",\n" +
                "\t\"id\":\"" + this.getId() + "\",\n" +
                "\t\"name\":\"" + this.getName() + "\",\n" +
                "\t\"level\":" + this.getLevel() + ",\n" +
                "\t\"experience\":" + this.getXP() + ",\n" +
                "\t\"cast_cost\":" + this.getCastCost() + ",\n" +
                "\t\"stat\":\"" + this.stat + "\",\n" +
                "\t\"effect_time\":" + this.effectTime + ",\n" +
                "\t\"multiplier\":" + this.multiplier + "\n" +
                "}";
    }
}
