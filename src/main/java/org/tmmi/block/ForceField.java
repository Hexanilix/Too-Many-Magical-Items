package org.tmmi.block;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.tmmi.InteractiveBlock;
import org.tmmi.events.PlayerBlockInteractEvent;

import java.util.*;

import static org.tmmi.Main.plugin;

public class ForceField extends InteractiveBlock {
    public static ItemStack item;
    public static List<ForceField> forceFields = new ArrayList<>();

    private BukkitTask task;

    public ForceField(Location loc) {
        super(Material.CAULDRON, loc);
        this.onPlace(loc);
        forceFields.add(this);
    }

    @Override
    public void onPlace(Location location) {
//        if (!forceFieldLocations.isEmpty()) {
//            i++;
//            for (Location loc : forceFieldLocations) {
//                World world = loc.getWorld();
//                if (i == 10) {
//                    for (int iter = 0; iter < 36; iter++) {
//                        double angle = iter * (2 * Math.PI) / 36;
//                        double x = loc.getX() + Math.cos(angle);
//                        double z = loc.getZ() + Math.sin(angle);
//                        Location particleLocation = new Location(world, x, loc.getY(), z);
//                        assert world != null;
//                        if (bannedMagicLocation.contains(world.getBlockAt(particleLocation).getLocation())) {
//                            continue;
//                        }
//                        if (forcefieldTriggers.get(loc).contains(iter)) {
//                            Objects.requireNonNull(world).spawnParticle(Particle.REDSTONE, particleLocation.add(0.5, 0.8, 0.5), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 1));
//                        } else {
//                            Objects.requireNonNull(world).spawnParticle(Particle.END_ROD, particleLocation.add(0.5, 0.6, 0.5), 1, 0, 0, 0, 0);
//                        }
//                    }
//                    forcefieldTriggers.get(loc).clear();
//                    i = 0;
//                }
//                for (Player player : Objects.requireNonNull(world).getPlayers()) {
//                    if (player.getGameMode() != GameMode.SPECTATOR) {
//                        if (bannedMagicLocation.contains(player.getWorld().getBlockAt(player.getLocation()).getLocation())) {
//                            continue;
//                        }
//                        if (forcefieldWhitelist.get(loc).contains(player.getUniqueId())) {
//                            if (isPlayerWithinLocation(player, loc.clone(), 10, true)) {
////                                double distance = loc.distance(player.getLocation());
////                                double amount = distance + distance;
////                                double currentX = loc.getX() + 0.5;
////                                double currentY = loc.getY() + 0.5;
////                                double currentZ = loc.getZ() + 0.5;
////                                double deltaX = (player.getLocation().getX() - loc.getX()) / amount;
////                                double deltaY = ((player.getLocation().getY() + 1) - loc.getY()) / amount;
////                                double deltaZ = (player.getLocation().getZ() - loc.getZ()) / amount;
////                                for (int i = 0; i < amount; i++) {
////                                    world.spawnParticle(Particle.END_ROD, currentX, currentY, currentZ, 1, 0, 0, 0, 0);
////                                    currentX += deltaX;
////                                    currentY += deltaY;
////                                    currentZ += deltaZ;
////                                }
//                                Vector direction = loc.clone().add(0.5, 0.5, 0.5).toVector().subtract(player.getLocation().toVector()).normalize();
//                                double flip = 0;
//                                for (int i = 1; i <= 10; i++) {
//                                    flip = (10 - (player.getLocation().distance(loc) - 1)) / 10;
//                                }
//                                player.setVelocity(player.getVelocity().add(direction.multiply(-flip)));
//                                world.spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0), (int) player.getLocation().distance(loc), 1, 1, 1, 0);
//                                Location closestLoc = null;
//                                for (Location actLoc : forcefieldActuationPoints.get(loc)) {
//                                    if (closestLoc == null) {
//                                        closestLoc = actLoc;
//                                    } else if (player.getLocation().distance(actLoc) < player.getLocation().distance(closestLoc)) {
//                                        closestLoc = actLoc;
//                                    }
//                                }
//                                int index = forcefieldActuationPoints.get(loc).indexOf(closestLoc);
//                                if (!forcefieldTriggers.get(loc).contains(index)) {
//                                    forcefieldTriggers.get(loc).add(index);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    @Override
    public void onBreak(Location location) {

    }

    @Override
    public void onClick(Action action, Player player, PlayerBlockInteractEvent event) {

    }

    @Override
    public void onGUIClick(ItemStack item, Player player) {

    }
}
