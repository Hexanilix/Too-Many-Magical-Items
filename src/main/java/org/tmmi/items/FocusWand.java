package org.tmmi.items;

import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class FocusWand extends Wand {

    public FocusWand() {
        this(null);
    }
    public FocusWand(UUID id) {
        super(id);
        ItemMeta m = this.getItemMeta();
        this.setItemMeta(m);
    }





//    public void SpawnOptions(@NotNull Player player, @NotNull List<ItemStack> optionItems, String MetaDataValueName) {
//        Location playerLocation = player.getEyeLocation();
//        World world = player.getWorld();
//        for (int i = 0; i < optionItems.size(); i++) {
//            Location spawnLocation = playerLocation.clone().subtract(0, 0.5, 0);
//            spawnLocation.setPitch(0);
//            spawnLocation.setYaw(playerLocation.getYaw() + ((i - (float) optionItems.size() / 2) * 20) + (optionItems.size() % 2 == 1 ? 10 : 0));
//            spawnLocation = spawnLocation.add(spawnLocation.getDirection().multiply(2));
//            Item item = world.dropItem(spawnLocation.clone().add(0, 0.15, 0), optionItems.get(i));
//            item.setCustomName(Objects.requireNonNull(optionItems.get(i).getItemMeta()).getDisplayName());
//            item.setMetadata(MetaDataValueName, new FixedMetadataValue(plugin, Objects.requireNonNull(optionItems.get(i).getItemMeta()).getCustomModelData()));
//            item.setCustomNameVisible(true);
//            item.setInvulnerable(true);
//            item.setGravity(false);
//            item.setPickupDelay(Integer.MAX_VALUE);
//            item.setUnlimitedLifetime(true);
//            item.setGlowing(false);
//            item.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
//            options.get(player).add(item);
//        }
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                if (options.get(player).isEmpty()) {
//                    weaving_players.remove(player);
//                    cancel();
//                } else {
//                    for (int i = 0; i < options.get(player).size(); i++) {
//                        Location spawnLocation = player.getEyeLocation().clone().subtract(0, 0.5, 0);
//                        if (options.get(player).size() > 6) {
//                            spawnLocation.subtract(0, 0.1, 0);
//                        }
//                        int i2 = i;
//                        if (i > 6) {
//                            i2 -= 7;
//                            spawnLocation.add(0, 0.6, 0);
//                        }
//                        spawnLocation.setPitch(0);
//                        spawnLocation.setYaw(player.getEyeLocation().getYaw() + ((i2 - ((float) Math.min(optionItems.size(), 6) / 2)) * 15));
//                        spawnLocation = spawnLocation.add(spawnLocation.getDirection().multiply(2));
//                        Item it = options.get(player).get(i);
//                        Vector direction = spawnLocation.toVector().subtract(it.getLocation().toVector()).normalize();
//                        double distance = spawnLocation.distance(it.getLocation());
//                        if (distance > 4) {
//                            options.get(player).get(i).teleport(spawnLocation);
//                        }
//                        it.setVelocity(direction.multiply(distance));
//                    }
//                }
//            }
//        }.runTaskTimer(plugin, 0, 0);
//        Item ite = options.get(player).get(selectedOption.get(player));
//        ite.setGlowing(true);
//        ite.setCustomName(ChatColor.BOLD + ite.getCustomName());
//        select_cooldown.replace(player, 2);
//    }
}
