package org.tmmi.PotionOf;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class GustWithABot implements Listener {
//    @EventHandler
//    public void onSplash(@NotNull PotionSplashEvent event) {
//        ItemStack item = event.getPotion().getItem();
//        if (allItems.contains(item)) {
//            if (allGustWithABot.contains(item)) {
//                event.setCancelled(true);
//                if (!bannedMagicLocation.contains(event.getPotion().getWorld().getBlockAt(event.getPotion().getLocation()).getLocation())) {
//                    int Tier = Integer.parseInt(String.valueOf(Objects.requireNonNull(item.getItemMeta()).getCustomModelData()).split("53")[1]);
//                    int Radius = Tier * 4;
//                    World world = event.getPotion().getWorld();
//                    Location loc = event.getPotion().getLocation();
//                    List<Entity> nearbyEntities = (List<Entity>) world.getNearbyEntities(loc, Radius, Radius, Radius);
//                    for (Entity entity : nearbyEntities) {
//                        Vector direction = loc.clone().subtract(0, 0.5, 0).toVector().subtract(entity.getLocation().toVector()).normalize();
//                        double flip = 0;
//                        for (int i = 1; i <= Radius; i++) {
//                            flip = (Radius - (entity.getLocation().distance(loc) - 1)) / Radius;
//                        }
//                        entity.setVelocity(entity.getVelocity().add(direction.multiply(-(flip * Tier))));
//                    }
//                }
//            }
//        }
//    }
}
