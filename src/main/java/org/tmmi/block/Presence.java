package org.tmmi.block;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.tmmi.Main.plugin;

public class Presence extends InteractiveBlock {
    public static List<Presence> instances = new ArrayList<>();

    public static Inventory inv;
    private ItemStack item;
    private Location location;
    private Inventory gui;
    private BukkitTask task;
    private int size;
    private final List<UUID> whitelist = new ArrayList<>();
    private String name;
    private int timeout;

    public Presence(ItemStack item, Material material, Location loc) {
        super(material, loc, inv);
        this.item = item;
    }

    @Override
    public void onPlace(@NotNull Location location) {
        this.location = location;
        this.task = new BukkitRunnable() {
            private int i = 0;
            @Override
            public void run() {
                Particle.DustOptions dustOptions;
                Location loc = location.getWorld().getBlockAt(location).getLocation().add(0.5, 0, 0.5);
                if (timeout > 0) {
                    dustOptions = new Particle.DustOptions(Color.RED, 1);
                    World world = loc.getWorld();
                    assert world != null;
                    world.spawnParticle(Particle.END_ROD, new Location(world, loc.getX(), (loc.getY() + 2.05) - (double) timeout / 2200, loc.getZ()), 1, 0, 0, 0, 0);
                    for (double x = loc.getX() - 0.2; x < (loc.getX() + 0.2); x += 0.05) {
                        world.spawnParticle(Particle.COMPOSTER, new Location(world, x, loc.getY() + 1.05, loc.getZ() + 0.2), 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.COMPOSTER, new Location(world, x, loc.getY() + 1.05, loc.getZ() - 0.2), 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.COMPOSTER, new Location(world, x, loc.getY() + 1.95, loc.getZ() + 0.2), 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.COMPOSTER, new Location(world, x, loc.getY() + 1.95, loc.getZ() - 0.2), 1, 0, 0, 0, 0);
                    }
                    for (double z = loc.getZ() - 0.2; z < (loc.getZ() + 0.2); z += 0.05) {
                        world.spawnParticle(Particle.COMPOSTER, new Location(world, loc.getX() + 0.2, loc.getY() + 1.05, z), 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.COMPOSTER, new Location(world, loc.getX() - 0.2, loc.getY() + 1.05, z), 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.COMPOSTER, new Location(world, loc.getX() + 0.2, loc.getY() + 1.95, z), 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.COMPOSTER, new Location(world, loc.getX() - 0.2, loc.getY() + 1.95, z), 1, 0, 0, 0, 0);
                    }
                    timeout--;
                } else {
                    dustOptions = new Particle.DustOptions(Color.GREEN, 1);
                    int radius = size / 2;
                    {
                        double angle = i * ((2 * Math.PI) / 360);
                        double x = loc.getX() + radius * Math.cos(angle);
                        double z = loc.getZ() + radius * Math.sin(angle);
                        Location particleLocation = new Location(loc.getWorld(), x, loc.getY() + 0.1, z);
                        Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.END_ROD, particleLocation, 1, 0, 0, 0, 0);
                        double angle2 = (i - 180) * ((2 * Math.PI) / 360);
                        double x2 = loc.getX() + radius * (Math.cos(angle2));
                        double z2 = loc.getZ() + radius * (Math.sin(angle2));
                        Location particleLocation2 = new Location(loc.getWorld(), x2, loc.getY() + 0.1, z2);
                        Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.END_ROD, particleLocation2, 1, 0, 0, 0, 0);
                    }
                    for (Player p : Objects.requireNonNull(loc.getWorld()).getPlayers()) {
                        if (p.getGameMode() != GameMode.SPECTATOR) {
                            if (!whitelist.contains(p.getUniqueId())) {
                                if (p.getLocation().distance(loc) < radius) {
                                    for (UUID notifiers : whitelist) {
                                        Player onlinePLayer = Bukkit.getPlayer(notifiers);
                                        if (onlinePLayer != null)
                                            onlinePLayer.sendMessage(ChatColor.BOLD + ChatColor.RED.toString() + "[" + name + "]" + " A player presence has been detected!");
                                    }
                                    p.getWorld().spawnParticle(Particle.DUST, p.getLocation().clone().add(0, 1, 0), 10, 0.5, 1, 0.5, 0, new Particle.DustOptions(Color.RED, 1));
                                    p.getWorld().playSound(p.getEyeLocation(), Sound.ENTITY_GHAST_HURT, 1, 1);
                                    {
                                        for (int i = 0; i < 360; i++) {
                                            double angle = i * ((2 * Math.PI) / 360);
                                            double x = loc.getX() + radius * Math.cos(angle);
                                            double z = loc.getZ() + radius * Math.sin(angle);
                                            Location particleLocation = new Location(loc.getWorld(), x, loc.getY(), z);
                                            Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.DUST, particleLocation.add(0, 1, 0), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 1));
                                        }
                                    }
                                    timeout = 1800;
                                    break;
                                }
                            }
                        }
                    }
                }
                World world = loc.getWorld();
                double y = location.getY();
                assert world != null;
                Particle particle = Particle.DUST;
                for (double x = loc.getX() - 0.5; x < (loc.getX() + 0.5); x += 0.1) {
                    world.spawnParticle(particle, new Location(world, x, y, loc.getZ() + 0.5), 1, 0, 0, 0, 0, dustOptions);
                    world.spawnParticle(particle, new Location(world, x, y, loc.getZ() - 0.5), 1, 0, 0, 0, 0, dustOptions);
                    world.spawnParticle(particle, new Location(world, x, y + 1, loc.getZ() + 0.5), 1, 0, 0, 0, 0, dustOptions);
                    world.spawnParticle(particle, new Location(world, x, y + 1, loc.getZ() - 0.5), 1, 0, 0, 0, 0, dustOptions);
                }
                for (double z = loc.getZ() - 0.5; z < (loc.getZ() + 0.5); z += 0.1) {
                    world.spawnParticle(particle, new Location(world, loc.getX() + 0.5, y, z), 1, 0, 0, 0, 0, dustOptions);
                    world.spawnParticle(particle, new Location(world, loc.getX() - 0.5, y, z), 1, 0, 0, 0, 0, dustOptions);
                    world.spawnParticle(particle, new Location(world, loc.getX() + 0.5, y + 1, z), 1, 0, 0, 0, 0, dustOptions);
                    world.spawnParticle(particle, new Location(world, loc.getX() - 0.5, y + 1, z), 1, 0, 0, 0, 0, dustOptions);
                }
                i++;
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    @Override
    public void onBreak(Location location) {

    }

    @Override
    public void onClick(Action action, Player player, @NotNull PlayerInteractEvent event) {
        if (action == Action.RIGHT_CLICK_BLOCK) {
            player.openInventory(this.gui);
        }
    }

    @Override
    public void onGUIClick(InventoryAction action, ItemStack item, Player player, InventoryClickEvent event) {

    }

//    @Override
//    public void onGUIClick(ItemStack item, Action action, Player player) {
//        if (detectorWhitelist.get(currentDetector.get(player)).get(0).equals(player.getUniqueId()) && !item.equals(background)) {
//                switch (Objects.requireNonNull(event.getCurrentItem().getItemMeta()).getCustomModelData()) {
//                    case 99276535 -> {
//                        setNewName.replace(player, true);
//                        player.closeInventory();
//                        player.sendMessage(ChatColor.GOLD + "Please enter in the new name for " + ChatColor.ITALIC + detectorName.get(currentDetector.get(player)) + " (type 'cancel' to abort");
//                    }
//                    case 8725345 -> {
//                        if (event.getClick() == ClickType.LEFT) {
//                            Inventory inv = addPlayersToWhitelist;
//                            inv.clear();
//                            for (int i = 0; i < 45; i++) {
//                                if (i < 9 || i > 35) {
//                                    inv.setItem(i, background);
//                                }
//                            }
//                            for (Player Onplayer : Bukkit.getOnlinePlayers()) {
//                                if (Onplayer != player && !detectorWhitelist.get(currentDetector.get(player)).contains(Onplayer.getUniqueId())) {
//                                    inv.addItem(getPlayerHead(Onplayer.getUniqueId()));
//                                }
//                            }
//                            player.openInventory(inv);
//                        } else if (event.getClick() == ClickType.RIGHT) {
//                            Inventory inv = removePlayersFromWhitelist;
//                            inv.clear();
//                            for (int i = 0; i < 45; i++) {
//                                if (i < 9 || i > 35) {
//                                    inv.setItem(i, background);
//                                }
//                            }
//                            for (UUID playUUID : detectorWhitelist.get(currentDetector.get(player))) {
//                                if (!player.getUniqueId().equals(playUUID)) {
//                                    inv.addItem(getPlayerHead(playUUID));
//                                }
//                            }
//                            player.openInventory(inv);
//                            player.openInventory(inv);
//                        } else {
//                            Inventory inv = whitelistedPlayers;
//                            inv.clear();
//                            for (int i = 0; i < 45; i++) {
//                                if (i < 9 || i > 35) {
//                                    inv.setItem(i, background);
//                                }
//                            }
//                            for (UUID playUUID : detectorWhitelist.get(currentDetector.get(player))) {
//                                inv.addItem(getPlayerHead(playUUID));
//                            }
//                            player.openInventory(inv);
//                        }
//                    }
//                    case 6972352 -> {
//                        ClickType clickType = event.getClick();
//                        if (clickType == ClickType.LEFT || clickType == ClickType.RIGHT || clickType == ClickType.MIDDLE) {
//                            event.setCancelled(true);
//                            Location loc = currentDetector.get(player);
//                            if (clickType == ClickType.LEFT) {
//                                detectorSize.replace(loc, Math.min(detectorSize.get(loc) + GUIsizeInterval.get(player), 200));
//                                player.openInventory(presenceGUI(detectorWhitelist.get(loc).get(0), loc));
//                            } else if (clickType == ClickType.RIGHT) {
//                                detectorSize.replace(loc, Math.max(detectorSize.get(loc) - GUIsizeInterval.get(player), 2));
//                                player.openInventory(presenceGUI(detectorWhitelist.get(loc).get(0), loc));
//                            } else {
//                                switch (GUIsizeInterval.get(player)) {
//                                    case 1 -> GUIsizeInterval.replace(player, 5);
//                                    case 5 -> GUIsizeInterval.replace(player, 10);
//                                    case 10 -> GUIsizeInterval.replace(player, 50);
//                                    case 50 -> GUIsizeInterval.replace(player, 1);
//                                }
//                                player.openInventory(presenceGUI(detectorWhitelist.get(loc).get(0), loc));
//                            }
//                            for (int i = 0; i < 360; i++) {
//                                double angle = i * ((2 * Math.PI) / 360);
//                                double x = loc.getX() + ((double) detectorSize.get(loc) / 2) * Math.cos(angle);
//                                double z = loc.getZ() + ((double) detectorSize.get(loc) / 2) * Math.sin(angle);
//                                Location particleLocation = new Location(loc.getWorld(), x, loc.getY(), z);
//                                Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.END_ROD, particleLocation.add(0.5, 1, 0.5), 1, 0, 0, 0, 0);
//                            }
//                        }
//                    }
//                }
//        } else {
//            event.setCancelled(true);
//            player.sendMessage(ChatColor.YELLOW + "You can't change the settings of " + detectorName.get(currentDetector.get(player)));
//        }
//    }

    @EventHandler
    public void onBlockCLick(@NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

        }
    }

    @EventHandler
    public void invClickEvent(@NotNull InventoryClickEvent event) {
//        Player player = (Player) event.getWhoClicked();
//        if (event.getInventory().getSize() == 45) {
//            if (event.getInventory().equals(addPlayersToWhitelist)) {
//                if (Objects.requireNonNull(event.getCurrentItem()).getType() == Material.PLAYER_HEAD) {
//                    SkullMeta skull = (SkullMeta) event.getCurrentItem().getItemMeta();
//                    event.setCancelled(true);
//                    assert skull != null;
//                    detectorWhitelist.get(currentDetector.get(player)).add(Objects.requireNonNull(skull.getOwningPlayer()).getUniqueId());
//                    player.sendMessage(ChatColor.GREEN + "Successfully added " + skull.getOwningPlayer().getName() + " to the notifier list!");
//                    Location loc = currentDetector.get(player);
//                    player.openInventory(presenceGUI(detectorWhitelist.get(loc).get(0), loc));
//                }
//            } else if (event.getInventory().equals(removePlayersFromWhitelist)) {
//                if (Objects.requireNonNull(event.getCurrentItem()).getType() == Material.PLAYER_HEAD) {
//                    SkullMeta skull = (SkullMeta) event.getCurrentItem().getItemMeta();
//                    event.setCancelled(true);
//                    assert skull != null;
//                    detectorWhitelist.get(currentDetector.get(player)).remove(Objects.requireNonNull(skull.getOwningPlayer()).getUniqueId());
//                    player.sendMessage(ChatColor.YELLOW + "Successfully removed " + skull.getOwningPlayer().getName() + " from the notifier list!");
//                    Location loc = currentDetector.get(player);
//                    player.openInventory(presenceGUI(detectorWhitelist.get(loc).get(0), loc));
//                }
//            } else if (event.getInventory().equals(whitelistedPlayers)) {
//                event.setCancelled(true);
//            } else {
//                if (event.getCurrentItem() != null) {
//                    if (Objects.requireNonNull(event.getClickedInventory()).getType() != InventoryType.PLAYER) {
//                        if (detectorWhitelist.containsKey(currentDetector.get(player))) {
//                            if (Objects.equals(event.getInventory().getItem(31), description())) {
//
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    @Override
    public String toJSON() {
        return  "\t\t{\n" +
                "\t\"type\":\"SPELL_WEAVER\",\n" +
                "\t\"world\":\"" + this.getWorld() + "\",\n" +
                "\t\"x\":\"" + this.getLoc().getX() + "\",\n" +
                "\t\"y\":\"" + this.getLoc().getY() + "\",\n" +
                "\t\"z\":\"" + this.getLoc().getZ() + "\",\n" +
                "}";

    }
}
