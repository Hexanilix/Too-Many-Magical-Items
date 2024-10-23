//package org.tmmi.item.items.potions;
//
//import org.bukkit.*;
//import org.bukkit.block.Block;
//import org.bukkit.entity.ThrownPotion;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.entity.PotionSplashEvent;
//import org.bukkit.scheduler.BukkitRunnable;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.*;
//
//import static org.hetils.minecraft.Material.isCrop;
//
//public class newSpring extends BukkitRunnable implements Listener {
//    public static List<Location> growLocations = new ArrayList<>();
//    public static Map<Location, Integer> growTime = new HashMap<>();
//    public static Map<Location, Integer> growLevel = new HashMap<>();
//    int i = 0;
//
//    @EventHandler
//    public void onPotBreak(@NotNull PotionSplashEvent event) {
//        Location location = event.getPotion().getLocation();
//        ThrownPotion potion = event.getPotion();
//        Location loc = new Location(location.getWorld(), Math.floor(location.getX()) + 0.5, Math.floor(location.getY()), Math.floor(location.getZ()) + 0.5);
////        if (allPotOfGrowth.contains(event.getPotion().getItem())) {
////            event.setCancelled(true);
////            if (potion.getShooter() instanceof Player player) {
////                if (player.hasPermission(permission)) {
////                    if (!growLocations.contains(loc) || growTime.get(loc) == 0) {
////                        growLocations.add(loc);
////                        growTime.putIfAbsent(loc, 7);
////                        growTime.replace(loc, 7);
////                        growLevel.putIfAbsent(loc,
////                                Integer.parseInt(String.valueOf(Objects.requireNonNull(potion.getItem().getItemMeta()).getCustomModelData()).split("23")[1]));
////                    } else {
////                        player.sendMessage(ChatColor.YELLOW + "A potion is already active in that location");
////                        Objects.requireNonNull(loc.getWorld()).dropItem(loc, potion.getItem());
////                    }
////                } else {
////                    player.sendMessage(ChatColor.RED + "You can't use this");
////                    Objects.requireNonNull(loc.getWorld()).dropItem(loc, potion.getItem());
////                }
////            } else {
////                Objects.requireNonNull(loc.getWorld()).dropItem(loc, potion.getItem());
////            }
////        }
//    }
//
//}
