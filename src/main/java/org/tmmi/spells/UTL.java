package org.tmmi.spells;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.items.ItemCommand;
import org.tmmi.Main;
import org.tmmi.spells.atributes.Weight;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.tmmi.Main.getSphere;
import static org.tmmi.Main.plugin;

public class UTL extends Spell {
    public enum Util {
        MINE,
        SMELT;

        @Contract(pure = true)
        public static @Nullable UTL.Util get(@NotNull String util) {
            switch (util.toUpperCase()) {
                case "MINE" -> {
                    return MINE;
                }
                default -> {
                    return null;
                }
            }
        }
    }
    private final Util util;
    public UTL(@NotNull UTL.Util u, @NotNull Player handler) {
        this(u, handler.getUniqueId(), 1, 0, 10);
    }
    public UTL(@NotNull UTL.Util u, UUID handler, int level, int XP, int castcost) {
        super(null, handler, "Mine", Weight.CANTRIP, level, XP, castcost, null);
        this.util = u;
        switch (u) {
            case MINE -> {
                this.setCastCost(10);
            }
        }
    }

    @Override
    public void onLevelUP() {

    }

    @Override
    public CastSpell cast(@NotNull PlayerInteractEvent event, Location castLocation, float multiplier) {
        Player player = event.getPlayer();
        switch (util) {
            case MINE ->
                    getSphere(player.getLocation(), 10).stream().filter(s -> s.getType().name().toLowerCase().contains("ore")).forEach(b -> {
                        new CastSpell(this, player.getEyeLocation(), getCastCost()) {

                            @Override
                            public BukkitTask cast(CastSpell casts) {
                                return new BukkitRunnable() {
                                    final Location loc = player.getEyeLocation();
                                    @Override
                                    public void run() {
                                        if (loc.distance(b.getLocation()) < 1) {
                                            cancel();
                                            org.bukkit.entity.Item i = b.getWorld().dropItem(b.getLocation(), new ItemStack(b.getType()));
                                            ItemCommand c = ItemCommand.getOrNew(i);
                                            c.moveTo(player, 0.1, 1);
                                            b.setType(Material.AIR);
                                        }
                                        loc.add(Main.genVec(loc, b.getLocation().clone().add(0.5, 0.5, 0.5)).multiply(0.4));
                                        Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.BLOCK, loc, 1, 0, 0, 0, 0, b.getBlockData());
                                    }
                                }.runTaskTimer(plugin, 0, 0);
                            }
                        };
                    });
//            case SMELT -> {
//                List<Furnace> fur = new ArrayList<>(getSphere(Objects.requireNonNull(player.getTargetBlockExact(10)).getLocation(), 10)
//                        .stream()
//                        .filter(b -> b.getType() == Material.FURNACE)
//                        .map(f -> (Furnace) f.getState())
//                        .toList());
//                for (org.bukkit.entity.Item item : selItms) {
//                    ItemCommand c = ItemCommand.getOrNew(item);
//                    c.moveTo(player, 0.5, 4, () -> {
//                        if (!fur.isEmpty() && !c.item.isDead()) {
//                            Furnace f = fur.stream()
//                                    .filter(fr -> fr.getInventory().getSmelting() == null ||
//                                            (fr.getInventory().getSmelting().getType() == item.getItemStack().getType() &&
//                                                    fr.getInventory().getSmelting().getAmount() + item.getItemStack().getAmount() < 64)
//                                    )
//                                    .toList()
//                                    .getFirst();
//                            if (f != null) {
//                                c.moveTo(f.getLocation().clone().add(0.5, 0.5, 0.5), 0.5, () -> {
//                                            f.getInventory().setSmelting(item.getItemStack());
//                                            item.remove();
//                                        }
//                                );
//                            }
//                        }
//                    });
//                }
//                getSphere(player.getLocation(), 10).stream().filter(s -> s.getType().name().toLowerCase().contains("ore")).forEach(b -> {
//                    new CastSpell(this, player.getEyeLocation(), getCastCost()) {
//
//                        @Override
//                        public BukkitTask cast(CastSpell casts) {
//                            return new BukkitRunnable() {
//                                final Location loc = player.getEyeLocation();
//
//                                @Override
//                                public void run() {
//                                    if (loc.distance(b.getLocation()) < 1) {
//                                        cancel();
//                                        org.bukkit.entity.Item i = b.getWorld().dropItem(b.getLocation(), new ItemStack(b.getType()));
//                                        Main.ItemCommand c = Main.ItemCommand.getOrNew(i);
//                                        Location loc = player.getLocation().clone();
//                                        c.moveTo(player, 0.5, 3, () -> c.revolve(loc));
//                                        b.setType(Material.AIR);
//                                        new Thread(() -> {
//                                            try {
//                                                Thread.sleep(4000);
//                                            } catch (InterruptedException e) {
//                                                throw new RuntimeException(e);
//                                            }
//                                            loc.add(2, 0, 0);
//                                        });
//                                    }
//                                    loc.add(Main.genVec(loc, b.getLocation().clone().add(0.5, 0.5, 0.5)).multiply(0.4));
//                                    Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.BLOCK, loc, 1, 0, 0, 0, 0, b.getBlockData());
//                                }
//                            }.runTaskTimer(plugin, 0, 0);
//                        }
//                    };
//                });
//            }
        }
        return null;
    }

    @Override
    public ItemStack toItem() {
        ChatColor c;
        switch (util) {
            default -> c = ChatColor.GRAY;
        }
        ChatColor sc;
        switch (util) {
            default -> sc = ChatColor.DARK_GRAY;
        }
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta m = item.getItemMeta();
        assert m != null;
        m.setDisplayName(c+this.getName() + " (" + this.getWeight() + ")");
        int nxtlxp = this.getXP() - (xpsum(this.getLevel()-1));
        int nxtLvlXP = lvlXPcalc(this.getLevel()-1);
        int perc = (int) (((float) nxtlxp / nxtLvlXP) * 100);
        m.setLore(List.of(sc+"Level "+c+this.getLevel(), c + "[" +
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
                "\t\"type\":\"UTL\",\n" +
                "\t\"id\":\"" + this.getId() + "\",\n" +
                "\t\"name\":\"" + this.getName() + "\",\n" +
                "\t\"level\":" + this.getLevel() + ",\n" +
                "\t\"experience\":" + this.getXP() + ",\n" +
                "\t\"cast_cost\":" + this.getCastCost() + ",\n" +
                "\t\"weight\":\"" + this.getWeight() + "\",\n" +
                "\t\"util\":\"" + this.util.name() + "\"\n" +
                "}";
    }
}
