package org.tmmi.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class placeAndBreakLogic implements Listener {
//    public static boolean isPlayerWithinLocation(@NotNull Player player, Location targetLocation, int distance, boolean IncludeY) {
//        if (!IncludeY) {
//            targetLocation.setY(player.getLocation().getY());
//        }
//        double outDistance = player.getLocation().distance(targetLocation.clone());
//        return distance > outDistance;
//    }
//    private static final String permission = "tmmi.craft." + new NamespacedKey(hexplug, "Crafting_Cauldron");
//    @EventHandler
//    public void onPlace(@NotNull BlockPlaceEvent event) {
//        Player player = event.getPlayer();
//        ItemStack item = player.getInventory().getItemInMainHand();
//        Material itemType = item.getType();
//        Location loc = event.getBlock().getLocation();
//        if (itemType.equals(presenceDetector().getType()) && Objects.requireNonNull(item.getItemMeta()).getCustomModelData() == Objects.requireNonNull(presenceDetector().getItemMeta()).getCustomModelData()) {
//            if (player.hasPermission(permission)) {
//                detectorLocations.add(loc);
//                detectorName.put(loc, "Detector");
//                ArmorStand armorStand = (ArmorStand) Objects.requireNonNull(loc.getWorld()).spawnEntity(loc.clone().add(0.5, 0, 0.5), EntityType.ARMOR_STAND);
//                armorStand.setCustomName("Detector");
//                armorStand.setGravity(false);
//                armorStand.setVisible(false);
//                armorStand.setMarker(false);
//                armorStand.setSmall(true);
//                armorStand.setInvisible(true);
//                armorStand.setInvulnerable(true);
//                armorStand.setCustomNameVisible(true);
//                detectorVisualName.putIfAbsent(loc, armorStand);
//                detectorSize.put(loc, 10);
//                detectorWhitelist.put(loc, new ArrayList<>(List.of(player.getUniqueId())));
//                detectorTimeout.putIfAbsent(player.getUniqueId(), 1800);
//                detectorTimeout.replace(player.getUniqueId(), 1800);
//            } else {
//                event.setCancelled(true);
//                player.sendMessage(ChatColor.YELLOW + "You can't use this item");
//            }
//        } else if (item.getType().equals(craftCauldron().getType()) && Objects.requireNonNull(item.getItemMeta()).getCustomModelData() == Objects.requireNonNull(craftCauldron().getItemMeta()).getCustomModelData()) {
//            craftingCauldronLocations.add(loc);
//        } else if (item.getType().equals(forcefield().getType()) && Objects.requireNonNull(item.getItemMeta()).getCustomModelData() == Objects.requireNonNull(forcefield().getItemMeta()).getCustomModelData()) {
//            forceFieldLocations.add(loc);
//            forcefieldWhitelist.put(loc, List.of(player.getUniqueId()));
//            forcefieldTriggers.putIfAbsent(loc, new ArrayList<>());
//            forcefieldActuationPoints.putIfAbsent(loc, new ArrayList<>());
//            for (int i = 0; i < 36; i++) {
//                double angle = i * (2 * Math.PI) / 36;
//                Location actLoc = new Location(loc.getWorld(), loc.getX() + Math.cos(angle), loc.getY(), loc.getZ() + Math.sin(angle));
//                if (!forcefieldActuationPoints.get(loc).contains(actLoc)) {
//                    forcefieldActuationPoints.get(loc).add(actLoc);
//                }
//            }
//        }
//    }

    @EventHandler
    public void onBreak(@NotNull BlockBreakEvent event) {
//        Location loc = event.getBlock().getLocation();
//        if (event.getBlock().getType().equals(presenceDetector().getType()) && detectorLocations.contains(loc)) {
//            event.setCancelled(true);
//            detectorLocations.remove(loc);
//            detectorWhitelist.remove(loc);
//            detectorName.remove(loc);
//            detectorVisualName.get(loc).remove();
//            detectorVisualName.remove(loc);
//            detectorSize.remove(loc);
//            Location brLoc = event.getBlock().getLocation();
//            Objects.requireNonNull(brLoc.getWorld()).getBlockAt(brLoc).setType(Material.AIR);
//            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
//                Objects.requireNonNull(brLoc.getWorld()).dropItem(brLoc.add(0.5, 0.5, 0.5), presenceDetector());
//            }
//        } else if (event.getBlock().getType().equals(craftCauldron().getType()) && craftingCauldronLocations.contains(loc)) {
//            event.setCancelled(true);
//            Location brLoc = event.getBlock().getLocation();
//            craftingCauldronLocations.remove(brLoc);
//            Objects.requireNonNull(brLoc.getWorld()).getBlockAt(brLoc).setType(Material.AIR);
//            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
//                Objects.requireNonNull(brLoc.getWorld()).dropItem(brLoc.add(0.5, 0.5, 0.5), craftCauldron());
//            }
//        } else if (event.getBlock().getType().equals(forcefield().getType()) && forceFieldLocations.contains(loc)) {
//            forceFieldLocations.remove(loc);
//            forcefieldWhitelist.remove(loc);
//        }
    }
}
