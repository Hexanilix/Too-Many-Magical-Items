package org.tmmi.PotionOf;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.tmmi.Main.permission;
import static org.tmmi.Main.plugin;

public class Displacement implements Listener {
    private final Map<Player, Integer> portalTier = new HashMap<>();
    private final Map<Player, Integer> timers = new HashMap<>();
    private final Random random = new Random();
    @EventHandler
    public void onPotionConsume(@NotNull PlayerItemConsumeEvent event) {
        if (Objects.requireNonNull(event.getItem().getItemMeta()).getDisplayName().contains("Potion of Displacement")) {
            Player player = event.getPlayer();
            if (permission.get(player.getUniqueId())) {
                player.sendMessage(ChatColor.GOLD + "Provide the x and z cords to teleport to:");
                BukkitRunnable runnable = new BukkitRunnable() {
                    int i = 0;
                    @Override
                    public void run() {
                        i++;
                        if (portalTier.get(player).equals(0)) {
                            cancel();
                        }
                        if (i == 30) {
                            portalTier.replace(player, 0);
                            player.playSound(player, Sound.ENTITY_ENDERMAN_STARE, 1, -3);
                            player.sendMessage(ChatColor.RED + "The potion expired");
                            cancel();
                        }
                    }
                };
                timers.put(player, runnable.runTaskTimer(plugin, 0, 20).getTaskId());
                portalTier.putIfAbsent(player, 0);
                portalTier.replace(player, portalTier.get(player), Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getCustomModelData());
            } else {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Some unknown force is stopping you from drinking this...");
            }
        }
    }
    @EventHandler
    public void onPlayerChat(@NotNull PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (portalTier.containsKey(player)) {
            int tier = Integer.parseInt(String.valueOf(portalTier.get(player)).split("45")[1]);
            if (tier != 0) {
                event.setCancelled(true);
                String[] msg = event.getMessage().split(" ");
                Location dest = new Location(player.getWorld(), Integer.parseInt(msg[0]) - 0.5, player.getLocation().getY(), Integer.parseInt(msg[1]) - 0.5);
                int maxDis = 0;
                switch (tier) {
                    case 1 -> maxDis = 1000;
                    case 2 -> maxDis = 2000;
                    case 3 -> maxDis = 3500;
                    case 4 -> maxDis = 5000;
                }
                if (dest.distance(player.getLocation()) > maxDis) {
                    player.sendMessage(ChatColor.YELLOW + "Your desired location is far to great for your mind to fathom");
                } else {
                    Bukkit.getScheduler().cancelTask(timers.get(player));
                    tpLogic(player, event.getMessage().split(" "), tier);
                }
            }
        }
    }

    public void tpLogic(@NotNull Player player, String @NotNull [] msg, int lvl) {
        World world = player.getWorld();
        float yaw = player.getLocation().getYaw();
        Location tpPortal = new Location(world,
                player.getLocation().getX() + (yaw > 45 && yaw <= 135 ? -3 : 0) + (yaw < -45 && yaw >= -135 ? 3 : 0),
                player.getLocation().getY() + 1,
                player.getLocation().getZ() + (yaw > -45 && yaw <= 135 ? 0 : -3) + (yaw < 45 && yaw >= -135 ? 3 : 0));
        world.spawnParticle(Particle.ENCHANT, player.getLocation(), 50, 0, 1, 0);
        world.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 0);
        Location startLoc = player.getLocation();
        portalTier.replace(player, 0);
        player.sendMessage(ChatColor.GOLD + "Opened Portal");
        Location dest = new Location(player.getWorld(), Integer.parseInt(msg[0]) - 0.5, startLoc.getY(), Integer.parseInt(msg[1]) - 0.5);
        int rawDistance = (int) dest.distance(player.getLocation());
        new BukkitRunnable() {
            private int i = 0;
            private int i2 = 0;
            private int pitch = 0;
            private int yaw = 0;

            @Override
            public void run() {
                i++;
                i2++;
                pitch += 3;
                yaw += 6;
                switch (lvl) {
                    case 1 -> {
                        int rawRadius = 40;
                        int Radius = rawRadius / 2;
                        int distance = rawDistance / 20;
                        world.spawnParticle(Particle.COMPOSTER, tpPortal, 10, 0.1, 0.3, 0.1);
                        world.spawnParticle(Particle.COMPOSTER, tpPortal, 7, 0.05, 0.5, 0.05);
                        world.spawnParticle(Particle.COMPOSTER, tpPortal, 3, 0.025, 1, 0.025);
                        world.spawnParticle(Particle.ENCHANT, tpPortal, 1, 0.2, 0.5, 0.2);
                        for (int i = 0; i < 40; i++) {
                            double angle = i * ((2 * Math.PI) / 40);
                            double x = dest.getX() + (Radius + distance) * Math.cos(angle);
                            double z = dest.getZ() + (Radius + distance) * Math.sin(angle);
                            Location particleLocation = new Location(world, x, dest.getY(), z);
                            world.spawnParticle(Particle.CRIMSON_SPORE, particleLocation, 1);
                        }
                        List<Entity> nearbyEntities = (List<Entity>) world.getNearbyEntities(tpPortal.clone().subtract(0, 0.5, 0), 0.5, 0.5, 0.5);
                        for (Entity entity : nearbyEntities) {
                            entity.teleport(new Location(entity.getWorld(), Integer.parseInt(msg[0]) + (random.nextInt(rawRadius + distance) - (Radius + distance)) + 0.5,
                                    dest.getY(),
                                    Integer.parseInt(msg[1]) + (random.nextInt(rawRadius + distance) - (Radius + distance)) + 0.5));
                            if (entity instanceof Player p) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 70, 10));
                                if (random.nextInt(10) == 3) {
                                    entity.getWorld().spawnEntity(entity.getLocation(), EntityType.VEX);
                                    entity.getWorld().spawnEntity(entity.getLocation(), EntityType.VEX);
                                    world.playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 3, 0);
                                    entity.getWorld().spawnParticle(Particle.WITCH, entity.getLocation(), 50, 0.5, 1, 0.5);
                                }
                            }
                            world.playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 2);
                            entity.getWorld().spawnParticle(Particle.ENCHANT, entity.getLocation(), 50, 1, 2, 1);
                        }
                    }
                    case 2 -> {
                        int rawRadius = 30;
                        int Radius = rawRadius / 2;
                        int distance = rawDistance / 40;
                        world.spawnParticle(Particle.COMPOSTER, tpPortal, 15, 0.2, 0.3, 0.2);
                        world.spawnParticle(Particle.WAX_ON, tpPortal.clone().add(0, 2, 0), 1);
                        world.spawnParticle(Particle.WAX_OFF, tpPortal.clone().subtract(0, 2, 0), 1);
                        world.spawnParticle(Particle.COMPOSTER, tpPortal, 10, 0.1, 0.5, 0.1);
                        world.spawnParticle(Particle.COMPOSTER, tpPortal, 5, 0.05, 0.7, 0.05);
                        world.spawnParticle(Particle.ENCHANT, tpPortal, 2, 0.2, 0.5, 0.2);
                        List<Entity> nearbyEntities = (List<Entity>) world.getNearbyEntities(tpPortal.clone().subtract(0, 0.5, 0), 0.5, 0.5, 0.5);
                        for (Entity entity : nearbyEntities) {
                            entity.teleport(new Location(entity.getWorld(), Integer.parseInt(msg[0]) + (random.nextInt(rawRadius + distance) - (Radius + distance)) + 0.5,
                                    dest.getY(),
                                    Integer.parseInt(msg[1]) + (random.nextInt(rawRadius + distance) - (Radius + distance)) + 0.5));
                            if (entity instanceof Player p) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 70, 10));
                                if (random.nextInt(20) == 19) {
                                    entity.getWorld().spawnEntity(entity.getLocation(), EntityType.VEX);
                                    entity.getWorld().spawnEntity(entity.getLocation(), EntityType.VEX);
                                    world.playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 3, 0);
                                    entity.getWorld().spawnParticle(Particle.WITCH, entity.getLocation(), 50, 0.5, 1, 0.5);
                                }
                            }
                            world.playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 2);
                            entity.getWorld().spawnParticle(Particle.ENCHANT, entity.getLocation(), 50, 1, 2, 1);
                        }
                    }
                    case 3 -> {
                        int rawRadius = 20;
                        int Radius = rawRadius / 2;
                        int distance = rawDistance / 60;
                        world.spawnParticle(Particle.COMPOSTER, tpPortal, 10, 0.2, 0.3, 0.2);
                        world.spawnParticle(Particle.COMPOSTER, tpPortal, 7, 0.1, 0.5, 0.1);
                        world.spawnParticle(Particle.COMPOSTER, tpPortal, 3, 0.05, 1, 0.05);
                        World world = tpPortal.getWorld();
                        double portalX = tpPortal.getX();
                        double portalY = tpPortal.getY();
                        double portalZ = tpPortal.getZ();

                        double offsetX = 1;
                        double offsetY = 1.7;
                        double offsetZ = 1;

                        double halfSize = 0.5;
                        assert world != null;
                        int thickness = 10;
                        if (i2 == 10) {
                            {
                                Particle particle = Particle.WAX_ON;
                                for (int xAdd = 0; xAdd < thickness; xAdd++) {
                                    world.spawnParticle(particle, new Location(world, portalX - halfSize + offsetX - (double) xAdd / thickness, portalY - halfSize + offsetY, portalZ - halfSize + offsetZ), 1, 0, 0, 0, 0);
                                    world.spawnParticle(particle, new Location(world, portalX - halfSize + offsetX - (double) xAdd / thickness, portalY - halfSize + offsetY, portalZ + halfSize - offsetZ), 1, 0, 0, 0, 0);
                                    world.spawnParticle(particle, new Location(world, portalX - halfSize + offsetX - (double) xAdd / thickness, portalY + halfSize - offsetY, portalZ - halfSize + offsetZ), 1, 0, 0, 0, 0);
                                    world.spawnParticle(particle, new Location(world, portalX - halfSize + offsetX - (double) xAdd / thickness, portalY + halfSize - offsetY, portalZ + halfSize - offsetZ), 1, 0, 0, 0, 0);
                                }
                            }
                            {
                                Particle particle = Particle.WAX_OFF;
                                for (int zAdd = 0; zAdd < thickness; zAdd++) {
                                    world.spawnParticle(particle, new Location(world, portalX + halfSize - offsetX + 1, portalY - halfSize + offsetY, portalZ - halfSize + offsetZ - (double) zAdd / thickness), 1, 0, 0, 0, 0);
                                    world.spawnParticle(particle, new Location(world, portalX + halfSize - offsetX, portalY - halfSize + offsetY, portalZ + halfSize - offsetZ + (double) zAdd / thickness), 1, 0, 0, 0, 0);
                                    world.spawnParticle(particle, new Location(world, portalX + halfSize - offsetX, portalY + halfSize - offsetY, portalZ - halfSize + offsetZ - (double) zAdd / thickness), 1, 0, 0, 0, 0);
                                    world.spawnParticle(particle, new Location(world, portalX + halfSize - offsetX + 1, portalY + halfSize - offsetY, portalZ + halfSize - offsetZ + (double) zAdd / thickness), 1, 0, 0, 0, 0);
                                }
                            }
                        }
                        for (int i = 0; i < rawRadius; i++) {
                            double angle = i * ((2 * Math.PI) / rawRadius);
                            double x = dest.getX() + (Radius + distance) * Math.cos(angle);
                            double z = dest.getZ() + (Radius + distance) * Math.sin(angle);
                            Location particleLocation = new Location(world, x, dest.getY(), z);
                            world.spawnParticle(Particle.CRIMSON_SPORE, particleLocation, 1);
                        }
                        List<Entity> nearbyEntities = (List<Entity>) world.getNearbyEntities(tpPortal.clone().subtract(0, 0.5, 0), 0.5, 0.5, 0.5);
                        for (Entity entity : nearbyEntities) {
                            entity.teleport(new Location(entity.getWorld(), Integer.parseInt(msg[0]) + (random.nextInt(rawRadius + distance) - (Radius + distance)) + 0.5,
                                    dest.getY(),
                                    Integer.parseInt(msg[1]) + (random.nextInt(rawRadius + distance) - (Radius + distance)) + 0.5));
                            if (entity instanceof Player p) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 70, 10));
                                if (random.nextInt(30) == 21) {
                                    entity.getWorld().spawnEntity(entity.getLocation(), EntityType.VEX);
                                    entity.getWorld().spawnEntity(entity.getLocation(), EntityType.VEX);
                                    world.playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 3, 0);
                                    entity.getWorld().spawnParticle(Particle.WITCH, entity.getLocation(), 50, 0.5, 1, 0.5);
                                }
                            }
                            world.playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 2);
                            entity.getWorld().spawnParticle(Particle.ENCHANT, entity.getLocation(), 50, 1, 2, 1);
                        }
                    }
                    case 4 -> {
                        int rawRadius = 10;
                        int Radius = rawRadius / 2;
                        int distance = rawDistance / 80;
                        world.spawnParticle(Particle.COMPOSTER, tpPortal, 25, 0.2, 0.5, 0.2);
                        world.spawnParticle(Particle.COMPOSTER, tpPortal, 20, 0.1, 0.7, 0.1);
                        world.spawnParticle(Particle.COMPOSTER, tpPortal, 10, 0.05, 1.3, 0.05);
                        world.spawnParticle(Particle.ENCHANT, tpPortal, 4, 0.2, 0.5, 0.2);
                        World world = tpPortal.getWorld();
                        double portalX = tpPortal.getX();
                        double portalY = tpPortal.getY();
                        double portalZ = tpPortal.getZ();

                        double offsetX = 1;
                        double offsetY = 1.7;
                        double offsetZ = 1;

                        double halfSize = 0.5;
                        assert world != null;
                        int thickness = 5;
                        {
                            Particle particle = Particle.WAX_ON;
                            world.spawnParticle(particle, new Location(world, portalX - halfSize + offsetX - (double) i2 / thickness, portalY - halfSize + offsetY, portalZ - halfSize + offsetZ), 1, 0, 0, 0, 0.1);
                            world.spawnParticle(particle, new Location(world, portalX - halfSize + offsetX - (double) i2 / thickness, portalY - halfSize + offsetY, portalZ + halfSize - offsetZ), 1, 0, 0, 0, 0.1);
                            world.spawnParticle(particle, new Location(world, portalX - halfSize + offsetX - (double) i2 / thickness, portalY + halfSize - offsetY, portalZ - halfSize + offsetZ), 1, 0, 0, 0, 0.1);
                            world.spawnParticle(particle, new Location(world, portalX - halfSize + offsetX - (double) i2 / thickness, portalY + halfSize - offsetY, portalZ + halfSize - offsetZ), 1, 0, 0, 0, 0.1);
                        }
                        {
                            Particle particle = Particle.WAX_OFF;
                            world.spawnParticle(particle, new Location(world, portalX + halfSize - offsetX + 1, portalY - halfSize + offsetY, portalZ - halfSize + offsetZ - (double) i2 / thickness), 1, 0, 0, 0, 0.1);
                            world.spawnParticle(particle, new Location(world, portalX + halfSize - offsetX, portalY - halfSize + offsetY, portalZ + halfSize - offsetZ + (double) i2 / thickness), 1, 0, 0, 0, 0.1);
                            world.spawnParticle(particle, new Location(world, portalX + halfSize - offsetX, portalY + halfSize - offsetY, portalZ - halfSize + offsetZ - (double) i2 / thickness), 1, 0, 0, 0, 0.1);
                            world.spawnParticle(particle, new Location(world, portalX + halfSize - offsetX + 1, portalY + halfSize - offsetY, portalZ + halfSize - offsetZ + (double) i2 / thickness), 1, 0, 0, 0, 0.1);
                        }
                        tpPortal.setPitch(pitch);
                        tpPortal.setYaw(yaw);
                        world.spawnParticle(Particle.WAX_OFF, tpPortal.clone().add(tpPortal.getDirection().multiply(1)), 1, 0, 0, 0, 0);
                        tpPortal.setPitch(pitch + 180);
                        tpPortal.setYaw(-yaw);
                        world.spawnParticle(Particle.WAX_ON, tpPortal.clone().add(tpPortal.getDirection().multiply(1)), 1, 0, 0, 0, 0);
                        if (pitch >= 360) {
                            pitch = 0;
                        }
                        if (yaw >= 360) {
                            yaw = 0;
                        }
                        for (int i = 0; i < rawRadius; i++) {
                            double angle = i * ((2 * Math.PI) / rawRadius);
                            double x = dest.getX() + (Radius + distance) * Math.cos(angle);
                            double z = dest.getZ() + (Radius + distance) * Math.sin(angle);
                            Location particleLocation = new Location(world, x, dest.getY(), z);
                            world.spawnParticle(Particle.CRIMSON_SPORE, particleLocation, 1, 0, 0, 0, 0);
                        }
                        List<Entity> nearbyEntities = (List<Entity>) world.getNearbyEntities(tpPortal.clone().subtract(0, 0.5, 0), 0.5, 0.5, 0.5);
                        for (Entity entity : nearbyEntities) {
                            entity.teleport(new Location(entity.getWorld(), Integer.parseInt(msg[0]) + (random.nextInt(rawRadius + distance) - (Radius + distance)) + 0.5,
                                    dest.getY(),
                                    Integer.parseInt(msg[1]) + (random.nextInt(rawRadius + distance) - (Radius + distance)) + 0.5));
                            if (entity instanceof Player p) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 70, 10));
                                if (random.nextInt(40) == 14) {
                                    entity.getWorld().spawnEntity(entity.getLocation(), EntityType.VEX);
                                    entity.getWorld().spawnEntity(entity.getLocation(), EntityType.VEX);
                                    world.playSound(entity.getLocation(), Sound.ENTITY_VILLAGER_NO, 3, 0);
                                    entity.getWorld().spawnParticle(Particle.WITCH, entity.getLocation(), 50, 0.5, 1, 0.5);
                                }
                            }
                            world.playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 2);
                            entity.getWorld().spawnParticle(Particle.ENCHANT, entity.getLocation(), 50, 1, 2, 1);
                        }
                    }
                }
                if (i2 == 5) {
                    i2 = 0;
                }
                if (i == 400) {
                    world.spawnParticle(Particle.COMPOSTER, tpPortal, 100, 1, 2, 1);
//                    locType.get(time).remove();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 0);
    }
}