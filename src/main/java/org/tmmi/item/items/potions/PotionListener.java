package org.tmmi.item.items.potions;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.hetils.Pair;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Main;

import java.util.*;

import static org.hetils.minecraft.Material.isCrop;
import static org.tmmi.Main.log;
import static org.tmmi.Main.plugin;

public class PotionListener implements Listener {
    public static final NamespacedKey POTION_LEVEL = new NamespacedKey(Main.name+"-potion", "level");

    public static final NamespacedKey PORTABLE_GLACIER = new NamespacedKey(Main.name+"-potion", "portable_glacier");

    public static final NamespacedKey NEW_SPRING = new NamespacedKey(Main.name+"-potion", "new_spring");
    public static final NamespacedKey NEW_SPRING_LIFE = new NamespacedKey(Main.name+"-potion", "new_spring-life");

    public static final NamespacedKey GUST_WITHIN_A_BOTTLE = new NamespacedKey(Main.name+"-potion", "gust_within_a_bottle");

    public enum NoiseType {
        Hextrics,
        Perlin,
        Random,
        LimitedRandom;
    }
    public static double[][] generateHeightMap(int width, int height, @NotNull NoiseType type, Object... args) {
        double[][] map;
        try {
            map = new double[width][height];
        } catch (NegativeArraySizeException e) {
            throw new RuntimeException("Negative number for array size: width="+width+" height="+height, e);
        }
        Random r = new Random();
        switch (type) {
            case Hextrics -> {
                return map;
            }
            case Perlin -> {
                return map;
            }
            case LimitedRandom -> {
                double mv = 0;
                for (int x = 0; x < width; x++)
                    for (int z = 0; z < height; z++) {
                        double v = r.nextDouble();
                        map[x][z] = v;
                        mv+=v;
                    }
                mv -= args.length > 0 && args[0] instanceof Double ? (double) args[0] : 0;
                int rs = r.nextInt(1, width*height+1);
                double mvs = mv / rs;
                if (mv > 0) {
                    for (int i = 0; i < rs-1; i++)
                        map[r.nextInt(0, height)][r.nextInt(0, width)] -= mvs;
                    map[r.nextInt(0, height)][r.nextInt(0, width)] -= mv - (mvs*(rs-1));
                } else if (mv < 0) {
                    for (int i = 0; i < rs-1; i++)
                        map[r.nextInt(0, height)][r.nextInt(0, width)] += mvs;
                    map[r.nextInt(0, height)][r.nextInt(0, width)] += mv - (mvs*(rs-1));
                }
                return map;
            }
            default -> {
                for (int x = 0; x < width; x++)
                    for (int z = 0; z < height; z++)
                        map[x][z] = r.nextDouble();
                return map;
            }
        }
    }

    @EventHandler
    public void onPotBreak(@NotNull PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        ItemMeta meta = potion.getItem().getItemMeta();
        if (meta == null) return;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (data.has(PORTABLE_GLACIER)) {

        } else if (data.has(NEW_SPRING)) {
            log("NEWSPRING");
            new BukkitRunnable() {
                final int life = data.get(NEW_SPRING_LIFE, PersistentDataType.INTEGER);
                final int level = data.get(NEW_SPRING, PersistentDataType.INTEGER);
                final int radius = level*3;
                final Location loc = potion.getLocation();
                final World w = loc.getWorld();
                final List<Block> crops = new ArrayList<>();
                {
                    crops.addAll(org.hetils.minecraft.Block.getBlocksInCircle(loc, radius).stream()
                            .filter(block -> isCrop(block.getType()))
                            .toList());
                    loc.setY(loc.getY()-1);
                    crops.addAll(org.hetils.minecraft.Block.getBlocksInCircle(loc, radius).stream()
                            .filter(block -> isCrop(block.getType()))
                            .toList());
                    loc.setY(loc.getY()+2);
                    crops.addAll(org.hetils.minecraft.Block.getBlocksInCircle(loc, radius).stream()
                            .filter(block -> isCrop(block.getType()))
                            .toList());
                }
                int i = 0;
                int grows = 0;
                @Override
                public void run() {
                    i++;
                    if (grows >= life) {
                        cancel();
                    }
                    for (int r = 0; r < radius; r++) {
                        double angle = r * ((2 * Math.PI) / radius);
                        double x = loc.getX() + radius * Math.cos(angle + i);
                        double z = loc.getZ() + radius * Math.sin(angle + i);
                        Location particleLocation = new Location(w, x, loc.getY() + (i > 70 ? ((double) i / 70) - 1 : 0), z);
                        w.spawnParticle(Particle.COMPOSTER, particleLocation, 1, 0, 0, 0, 0);
                    }
                    if (i >= 100) {
                        for (Block b : crops) {
                            Ageable age = (Ageable) b.getBlockData();
                            age.setAge(Math.min(age.getAge() + 1, age.getMaximumAge()));
                            Particle p = Particle.COMPOSTER;
                            double newY = (double) age.getAge() / age.getMaximumAge();
                            double x0 = b.getX();
                            double z0 = b.getZ();
                            double y = b.getY() + newY;
                            for (double a = 0; a < 1; a += .2f) {
                                w.spawnParticle(p, x0 + a, y, z0, 1, 0, 0, 0, 0);
                                w.spawnParticle(p, x0 + a, y, z0 + 1, 1, 0, 0, 0, 0);
                                w.spawnParticle(p, x0, y, z0 + a, 1, 0, 0, 0, 0);
                                w.spawnParticle(p, x0 + 1, y, z0 + a, 1, 0, 0, 0, 0);
                            }
                            b.setBlockData(age);
                            b.getState().update();
                        }
                        grows++;
                        i = 0;
                    }
                }
            }.runTaskTimer(plugin, 0, 2);
        }
    }
}
