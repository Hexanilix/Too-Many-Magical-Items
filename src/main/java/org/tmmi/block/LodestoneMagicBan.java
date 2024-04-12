package org.tmmi.block;

import org.bukkit.event.Listener;

public class LodestoneMagicBan implements Listener {
//    @EventHandler
//    public void onLodePlace(@NotNull BlockPlaceEvent event) {
//        Block block = event.getBlock();
//        if (block.getType().equals(Material.LODESTONE)) {
//            Location loc = block.getLocation().clone().add(0.5, 0.5, 0.5);
//            int x0 = loc.getBlockX();
//            int z0 = loc.getBlockZ();
//            int radius = 10;
//            for (double x = x0 - radius; x <= x0 + radius; x++) {
//                for (double z = z0 - radius; z <= z0 + radius; z++) {
//                    double distanceSquared = Math.pow(x - x0, 2) + Math.pow(z - z0, 2);
//                    if (distanceSquared <= radius * radius) {
//                        for (int y = block.getY() - 5; y < block.getY() + 5; y++) {
//                            Location bannedLoc = new Location(block.getWorld(), x, y, z);
//                            if (!bannedMagicLocation.contains(bannedLoc)) {
//                                bannedMagicLocation.add(bannedLoc);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    @EventHandler
//    public void onLodePlace(@NotNull BlockBreakEvent event) {
//        Block block = event.getBlock();
//        if (block.getType().equals(Material.LODESTONE)) {
//            Location loc = block.getLocation().clone().add(0.5, 0.5, 0.5);
//            int x0 = loc.getBlockX();
//            int z0 = loc.getBlockZ();
//            int radius = 10;
//            for (double x = x0 - radius; x <= x0 + radius; x++) {
//                for (double z = z0 - radius; z <= z0 + radius; z++) {
//                    double distanceSquared = Math.pow(x - x0, 2) + Math.pow(z - z0, 2);
//                    if (distanceSquared <= radius * radius) {
//                        for (int y = block.getY() - 5; y < block.getY() + 5; y++) {
//                            Location bannedLoc = new Location(block.getWorld(), x, y, z);
//                            bannedMagicLocation.remove(bannedLoc);
//                        }
//                    }
//                }
//            }
//        }
//    }
}
