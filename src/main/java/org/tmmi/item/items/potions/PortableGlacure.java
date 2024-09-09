package org.tmmi.item.items.potions;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.tmmi.Main.plugin;


public class PortableGlacure implements Listener {
    private final Random random = new Random();
    private final List<Block> GlobalBlocksToRemove = new ArrayList<>();
    Map<Block, Material> blockMaterial = new HashMap<>();
    @EventHandler
    public void onPotBreak(@NotNull PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        if (Objects.requireNonNull(potion.getItem().getItemMeta()).getDisplayName().equalsIgnoreCase("banban")) {
            Location loc = potion.getLocation();
            World world = loc.getWorld();
            assert world != null;
            int radius = 10;
            List<Block> blocksToRemove = new ArrayList<>();
            int x0 = loc.getBlockX();
            int z0 = loc.getBlockZ();
            for (int x = x0 - radius; x <= x0 + radius; x++) {
                for (int z = z0 - radius; z <= z0 + radius; z++) {
                    double distanceSquared = Math.pow(x - x0, 2) + Math.pow(z - z0, 2);
                    if (distanceSquared <= radius * radius) {
                        Block block = world.getHighestBlockAt(x, z);
                        if (!(block.getY() > loc.getY() + 3 || block.getY() < loc.getY() - 3)) {
                            block = world.getBlockAt(x, (int) loc.getY(), z);
                        }
                        for (int y = block.getY(); y < y + random.nextInt(3); y++) {
                            Block setingBlock = world.getBlockAt(x, y, z);
                            if (!GlobalBlocksToRemove.contains(setingBlock)) {
                                GlobalBlocksToRemove.add(setingBlock);
                                blocksToRemove.add(setingBlock);
                                blockMaterial.putIfAbsent(setingBlock, setingBlock.getType());
                                setingBlock.setType(Material.BLUE_ICE);
                            }
                        }
                    }
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Block block : blocksToRemove) {
                        Material mat = blockMaterial.get(block);
                        if (mat == Material.WATER || mat == Material.LAVA) {
                            block.setType((mat == Material.WATER ? Material.ICE : Material.OBSIDIAN));
                        } else {
                            block.setType(mat);
                        }
                        GlobalBlocksToRemove.remove(block);
                    }
                    blocksToRemove.clear();
                    blockMaterial.clear();
                    cancel();
                }
            }.runTaskTimer(plugin, 300, 0);
        }
    }
    @EventHandler
    public void onBreak(@NotNull BlockBreakEvent event) {
        Block block = event.getBlock();
        if (GlobalBlocksToRemove.contains(block)) {
            block.setType(blockMaterial.get(block));
            GlobalBlocksToRemove.remove(block);
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        if (event.getAction().name().contains("RIGHT_CLICK") && item != null && item.getType() == Material.SPLASH_POTION) {
            if (Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals("banban")) {
                new BukkitRunnable() {
                    public void getPot() {
                        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation().clone().add(0, 1, 0), 1.5, 1.5, 1.5)) {
                            if (entity instanceof ThrownPotion potion) {
                                pot = potion;
                                break;
                            }
                        }
                        if (pot == null) {
                            cancel();
                        }
                    }
                    Location lastLoc;
                    ThrownPotion pot;
                    @Override
                    public void run() {
                        if (pot == null) {
                            getPot();
                        } else {
                            if (pot.getLocation() == lastLoc) {
                                cancel();
                            }
                            lastLoc = pot.getLocation();
                            pot.getWorld().spawnParticle(Particle.WHITE_SMOKE, pot.getLocation(), 1, 0, 0, 0, 0);
                            //new Particle.DustOptions(Color.AQUA, 1)
                        }
                    }
                }.runTaskTimer(plugin, 1, 0);
            }
        }
    }
    public static int flipValue(int maxValue, double ogDV) {
        int originalValue = (int) Math.round(ogDV);
        int minValue = 1;
        return maxValue - (originalValue - minValue);
    }
}
