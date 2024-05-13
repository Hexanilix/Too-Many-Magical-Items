package org.tmmi.PotionOf;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class newSpring extends BukkitRunnable implements Listener {
    public static List<Location> growLocations = new ArrayList<>();
    public static Map<Location, Integer> growTime = new HashMap<>();
    public static Map<Location, Integer> growLevel = new HashMap<>();
    int i = 0;

    @EventHandler
    public void onPotBreak(@NotNull PotionSplashEvent event) {
        Location location = event.getPotion().getLocation();
        ThrownPotion potion = event.getPotion();
        Location loc = new Location(location.getWorld(), Math.floor(location.getX()) + 0.5, Math.floor(location.getY()), Math.floor(location.getZ()) + 0.5);
//        if (allPotOfGrowth.contains(event.getPotion().getItem())) {
//            event.setCancelled(true);
//            if (potion.getShooter() instanceof Player player) {
//                if (player.hasPermission(permission)) {
//                    if (!growLocations.contains(loc) || growTime.get(loc) == 0) {
//                        growLocations.add(loc);
//                        growTime.putIfAbsent(loc, 7);
//                        growTime.replace(loc, 7);
//                        growLevel.putIfAbsent(loc,
//                                Integer.parseInt(String.valueOf(Objects.requireNonNull(potion.getItem().getItemMeta()).getCustomModelData()).split("23")[1]));
//                    } else {
//                        player.sendMessage(ChatColor.YELLOW + "A potion is already active in that location");
//                        Objects.requireNonNull(loc.getWorld()).dropItem(loc, potion.getItem());
//                    }
//                } else {
//                    player.sendMessage(ChatColor.RED + "You can't use this");
//                    Objects.requireNonNull(loc.getWorld()).dropItem(loc, potion.getItem());
//                }
//            } else {
//                Objects.requireNonNull(loc.getWorld()).dropItem(loc, potion.getItem());
//            }
//        }
    }
    @Override
    public void run() {
        List<Block> grownBlocks = new ArrayList<>();
        if (!growLocations.isEmpty()) {
            i++;
            for (Location loc : growLocations) {
                int time = growTime.get(loc);
                if (time != 0) {
                    if (i >= 200) {
                        growTime.replace(loc, (Math.max(time - 1, 0)));
                    }
                    int size = 0;
                    double radius = 0;
                    switch (growLevel.get(loc)) {
                        case 1 -> {
                            size = 4;
                            radius = ((double) size / 2) + 0.9;
                            grownBlocks.add(Objects.requireNonNull(loc.getWorld()).getBlockAt(new Location(loc.getWorld(), loc.getX() + 2, loc.getY(), loc.getZ() + 2)));
                            grownBlocks.add(Objects.requireNonNull(loc.getWorld()).getBlockAt(new Location(loc.getWorld(), loc.getX() - 2, loc.getY(), loc.getZ() + 2)));
                            grownBlocks.add(Objects.requireNonNull(loc.getWorld()).getBlockAt(new Location(loc.getWorld(), loc.getX() - 2, loc.getY(), loc.getZ() - 2)));
                            grownBlocks.add(Objects.requireNonNull(loc.getWorld()).getBlockAt(new Location(loc.getWorld(), loc.getX() + 2, loc.getY(), loc.getZ() - 2)));
                        }
                        case 2 -> {
                            size = 8;
                            radius = ((double) size / 2) + 0.5;
                        }
                        case 3 -> {
                            size = 12;
                            radius = ((double) size / 2) + 0.5;
                        }
                        case 4 -> {
                            size = 16;
                            radius = ((double) size / 2) + 1;
                        }
                    }
                    World world = loc.getWorld();
                    int rawRadius = size;
                    int Radius = rawRadius / 2;
                    for (int radI = 0; radI < size; radI++) {
                        double angle = radI * ((2 * Math.PI) / size);
                        double x = loc.getX() + Radius * Math.cos(angle + i);
                        double z = loc.getZ() + Radius * Math.sin(angle + i);
                        Location particleLocation = new Location(world, x, loc.getY() + (i > 100 ? ((double) i / 100) - 1 : 0), z);
                        assert world != null;
                        world.spawnParticle(Particle.COMPOSTER, particleLocation, 1);
                    }
                    int x0 = loc.getBlockX();
                    int z0 = loc.getBlockZ();
                    for (double x = x0 - radius; x <= x0 + radius; x++) {
                        for (double z = z0 - radius; z <= z0 + radius; z++) {
                            double distanceSquared = Math.pow(x - x0, 2) + Math.pow(z - z0, 2);
                            if (distanceSquared <= radius * radius) {
                                assert world != null;
                                for (double y = loc.getY() - 2; y <= loc.getY() + 2; y++) {
                                    Block block = world.getBlockAt(new Location(world, x, y, z));
                                    Material material = block.getType();
                                    if (material == Material.WHEAT || material == Material.CARROTS || material == Material.POTATOES ||
                                            material == Material.BEETROOTS || material == Material.NETHER_WART || material == Material.PUMPKIN_STEM ||
                                            material == Material.MELON_STEM) {
//                                        if (!bannedMagicLocation.contains(block.getLocation())) {
//                                            if (!grownBlocks.contains(block)) {
//                                                double newX = Math.floor(x) + 0.5;
//                                                double newZ = Math.floor(z) + 0.5;
//                                                BlockState blockState = block.getState();
//                                                if (blockState.getBlockData() instanceof Ageable ageable) {
//                                                    if (ageable.getAge() != ageable.getMaximumAge()) {
//                                                        world.spawnParticle(Particle.WATER_SPLASH, new Location(world, newX, y, newZ), 1);
//                                                        grownBlocks.add(block);
//
//                                                        if (i >= 200) {
//                                                            ageable.setAge(ageable.getAge() + 1);
//                                                            blockState.setBlockData(ageable);
//                                                            blockState.update(true);
//                                                            Particle particle = Particle.COMPOSTER;
//                                                            double newY = (y - 1) + (double) ageable.getAge() / 10;
//                                                            double offsetX = 1;
//                                                            double offsetY = 1.7;
//                                                            double offsetZ = 1;
//                                                            for (int xAdd = 0; xAdd < 5; xAdd++) {
//                                                                world.spawnParticle(particle, new Location(world, newX - 0.5 + offsetX - (double) xAdd / 5, newY - 0.5 + offsetY, newZ - 0.5 + offsetZ), 1);
//                                                                world.spawnParticle(particle, new Location(world, newX - 0.5 + offsetX - (double) xAdd / 5, newY - 0.5 + offsetY, newZ + 0.5 - offsetZ), 1);
//                                                                world.spawnParticle(particle, new Location(world, newX - 0.5 + offsetX - (double) xAdd / 5, newY + 0.5 - offsetY, newZ - 0.5 + offsetZ), 1);
//                                                                world.spawnParticle(particle, new Location(world, newX - 0.5 + offsetX - (double) xAdd / 5, newY + 0.5 - offsetY, newZ + 0.5 - offsetZ), 1);
//                                                            }
//                                                            for (int zAdd = 0; zAdd < 5; zAdd++) {
//                                                                world.spawnParticle(particle, new Location(world, newX + 0.5 - offsetX + 1, newY - 0.5 + offsetY, newZ - 0.5 + offsetZ - (double) zAdd / 5), 1);
//                                                                world.spawnParticle(particle, new Location(world, newX + 0.5 - offsetX, newY - 0.5 + offsetY, newZ + 0.5 - offsetZ + (double) zAdd / 5), 1);
//                                                                world.spawnParticle(particle, new Location(world, newX + 0.5 - offsetX, newY + 0.5 - offsetY, newZ - 0.5 + offsetZ - (double) zAdd / 5), 1);
//                                                                world.spawnParticle(particle, new Location(world, newX + 0.5 - offsetX + 1, newY + 0.5 - offsetY, newZ + 0.5 - offsetZ + (double) zAdd / 5), 1);
//                                                            }
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (i >= 200) {
                i = 0;
            }
        }
    }
}
