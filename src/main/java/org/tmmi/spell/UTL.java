package org.tmmi.spell;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.Element;
import org.tmmi.ManaBar;
import org.tmmi.WeavePlayer;
import org.tmmi.items.ItemCommand;
import org.tmmi.spell.atributes.Weight;

import java.util.*;
import static org.hetils.Util.*;
import static org.tmmi.Main.*;

public class UTL extends Spell {
    public enum Util {
        MINE,
        SMELT,
        LINK;

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
    public UTL(@NotNull UTL.Util u) {
        this(u, 1, 0, 10);
    }
    public UTL(@NotNull UTL.Util u, int level, int XP, int castcost) {
        super(null, "Mine", Weight.CANTRIP, org.tmmi.Element.EARTH, level, XP, castcost);
        this.util = u;
        switch (u) {
            case MINE -> {
                this.setCc(10);
            }
        }
        this.attemptLvlUP();
    }

    @Override
    public void onLevelUP() {

    }

    @Override
    public CastSpell cast(@NotNull Location castLocation, float multiplier, Entity entity) {
        Player player = (Player) entity;
        WeavePlayer wp = WeavePlayer.getWeaver(player);
        switch (util) {
            case MINE ->
                    getSphere(player.getLocation(), 10).stream().filter(s -> s.getType().name().toLowerCase().contains("ore")).forEach(b -> {
                        new CastSpell(this, player.getEyeLocation(), getCc()) {

                            @Override
                            public Thread cast() {
                                return new Thread() {
                                    final Location loc = player.getEyeLocation();
                                    @Override
                                    public void run() {
                                        if (loc.distance(b.getLocation()) < 1) {
                                            interrupt();
                                            org.bukkit.entity.Item i = b.getWorld().dropItem(b.getLocation(), new ItemStack(b.getType()));
                                            ItemCommand c = ItemCommand.getOrNew(i);
                                            c.moveTo(player, 0.1, 1);
                                            b.setType(Material.AIR);
                                        }
                                        loc.add(genVec(loc, b.getLocation().clone().add(0.5, 0.5, 0.5)).multiply(0.4));
                                        Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.BLOCK, loc, 1, 0, 0, 0, 0, b.getBlockData());
                                    }
                                };
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
//                                        newThread(() -> {
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
            case LINK -> {
                Location loc = player.getEyeLocation().clone();
                for (int it = 0; it < 60; it++) {
                    loc.add(loc.getDirection().multiply(0.1));
                    loc.getWorld().spawnParticle(Particle.COMPOSTER, loc, 1, 0, 0 ,0 ,0);
                    Entity e = nearestEntity(loc, 0.15);
                    if (e instanceof Player pl && pl != player) {
                        WeavePlayer we = WeavePlayer.getWeaver(pl);
                        newThread(new Thread() {
                            private final Map<WeavePlayer, ManaBar> list = Map.of(
                                    wp, wp.getManaBar(),
                                    we, we.getManaBar()
                            );
                            @Override
                            public void run() {
                                try {
                                    log(wp.getManaBar().limit + we.getManaBar().limit);
                                    ManaBar mb = new ManaBar(wp.getManaBar().limit + we.getManaBar().limit, we.getMana() + wp.getMana());
                                    wp.setManaBar(mb);
                                    we.setManaBar(mb);
                                    Thread.sleep(10000);
                                    for (Map.Entry<WeavePlayer, ManaBar> set : list.entrySet())
                                        set.getKey().setManaBar(new ManaBar(set.getValue().limit, 0));
                                } catch (InterruptedException ex) {}
                            }
                        }).start();
                        break;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String toJson() {
        return  "\t\t{\n" +
                "\t\"type\":\"UTL\",\n" +
                "\t\"id\":\"" + this.getId() + "\",\n" +
                "\t\"name\":\"" + this.getName() + "\",\n" +
                "\t\"level\":" + this.getLevel() + ",\n" +
                "\t\"experience\":" + this.getXP() + ",\n" +
                "\t\"cast_cost\":" + this.getCc() + ",\n" +
                "\t\"weight\":\"" + this.getWeight() + "\",\n" +
                "\t\"util\":\"" + this.util.name() + "\"\n" +
                "}";
    }
}
