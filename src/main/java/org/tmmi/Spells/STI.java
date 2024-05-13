package org.tmmi.Spells;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.Element;
import org.tmmi.EntityMultiplier;
import org.tmmi.Main;
import org.tmmi.WeavePlayer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

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
    }
    public STI(Stat stat, UUID handler) {
        this(stat, null, handler, 1, 0, 10, 10000, 0.2);
    }

    @Override
    public void onLevelUP() {
        effectTime = (int) (Math.pow(effectTime, 2) / 10);
        multiplier = (int) (Math.pow(this.getLevel(), 2) / 200)+0.2;
    }

    @Override
    public CastSpell cast(@NotNull PlayerInteractEvent event, @NotNull Location castLocation, float multiplier) {
        Player p = event.getPlayer();
        Location loc = castLocation.clone();
        switch (stat) {
            case DMG -> {
                for (int i = 0; i < 20; i++) {
                    loc.add(loc.getDirection());
                    Entity e = Main.nearestEntity(loc, 0.25, List.of(p));
                    if (e != null) {
                        EntityMultiplier em = EntityMultiplier.getOrNew(e);
                        em.addDmg(multiplier);
                        e.setGlowing(true);
                        STI.this.addXP((int) ((STI.this.effectTime*STI.this.multiplier)/10000));
                        new Thread(() -> {
                            try {
                                Thread.sleep(STI.this.effectTime);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            e.setGlowing(false);
                            em.addDmg(-STI.this.multiplier);
                            STI.this.attemptLvlUP();
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
        m.setDisplayName(c+this.getName() + " (" + WeavePlayer.getOrNew(Bukkit.getPlayer(getHandler())).getSpells().indexOf(this) + ")");
        m.setCustomModelData(WeavePlayer.getOrNew(Bukkit.getPlayer(getHandler())).getSpells().indexOf(this));
        int nxtlxp = this.getXP() - (xpsum(this.getLevel()-1));
        int nxtLvlXP = lvlXPcalc(this.getLevel()-1);
        int perc = (int) (((float) nxtlxp / nxtLvlXP) * 100);
        m.setLore(List.of(c + "Multiplier: " + sc + this.multiplier,
                c + "Effect time: " + sc + this.effectTime / 1000,
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
