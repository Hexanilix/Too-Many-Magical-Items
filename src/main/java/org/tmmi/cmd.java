package org.tmmi;

import org.bukkit.*;
import org.bukkit.block.data.type.ChiseledBookshelf;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.block.Block;
import org.tmmi.block.CraftingCauldron;
import org.tmmi.block.ManaCauldron;
import org.tmmi.block.WeavingTable;
import org.tmmi.item.items.FocusWand;
import org.tmmi.item.ItemCommander;
import org.tmmi.spell.*;
import org.tmmi.spell.spells.AphrodytiesBlessing;
import org.tmmi.spell.spells.MagicMissile;

import java.util.*;
import java.util.stream.Stream;


import static org.hetils.Block.getBlocksInSphere;
import static org.hetils.Item.newItemStack;
import static org.hetils.Location.nearestEntity;
import static org.hetils.Util.*;
import static org.hetils.Vector.genVec;
import static org.tmmi.Main.*;
import static org.tmmi.WeavePlayer.getOrNew;
import static org.tmmi.WeavePlayer.getWeaver;
//import static org.tmmi.block.Presence.instances;

public class cmd implements CommandExecutor {
    private final Main main;
    cmd(Main m) {
        this.main = m;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("tmmi")) {
            if (!ENABLED.v()) {
                if (args[0].equalsIgnoreCase("reload")) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "[TMMI] Reloading...");
                    main.reload();
                    Bukkit.broadcastMessage(ChatColor.GREEN + "[TMMI] Reloaded!");
                }
                return true;
            }
            boolean cmd = sender instanceof ConsoleCommandSender;
            Player player = cmd ? null : (Player) sender;
            ConsoleCommandSender console = cmd ? (ConsoleCommandSender) sender : null;
            if (args.length != 0) {
                if (sender.isOp()) {
                    switch (args[0].toLowerCase()) {
                        case "setperm" -> {
                            if (args.length >= 2 && args.length < 4) {
                                Player desPlayer = Bukkit.getPlayer(args[1]);
                                if (desPlayer == null) {
                                    sender.sendMessage(ChatColor.RED + "Unknown player " + ChatColor.ITALIC + args[1]);
                                } else {
                                    main.permission.replace(desPlayer.getUniqueId(), !main.permission.get(desPlayer.getUniqueId()));
                                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "Set " + desPlayer.getName() + "'s permission to " + (main.permission.get(desPlayer.getUniqueId()) ? ChatColor.GREEN + "allow magic" : ChatColor.YELLOW + "forbid magic"));
                                    if (!(args.length == 3 && args[2].equalsIgnoreCase("hide"))) {
                                        desPlayer.sendMessage("You now" + (main.permission.get(desPlayer.getUniqueId()) ? ChatColor.GREEN + " have permission to use magic" : ChatColor.YELLOW + "You now don't have permission to use magic"));
                                    }
                                    desPlayer.recalculatePermissions();
                                }
                            } else if (args.length < 2) {
                                if (cmd)
                                    console.sendMessage(ChatColor.YELLOW + "Please specify player!");
                                else {
                                    main.permission.replace(player.getUniqueId(), !main.permission.get(player.getUniqueId()));
                                    player.sendMessage("You now" + (main.permission.get(player.getUniqueId()) ? ChatColor.GREEN + " have permission to use magic" : ChatColor.YELLOW + "You now don't have permission to use magic"));
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Too many arguments");
                            }
                        }
                        case "items" -> {
                            if (cmd) {
                                log(ChatColor.YELLOW + "This command can't be executed via console!");
                                return true;
                            }
                            if (args.length >= 2) {
                                if (args[1].equalsIgnoreCase("removeAllCraftingCauldrons")) {
                                    String confirm = ChatColor.RED + "You must confirm this action. To do so, add the 'confirm' argument at the end of the command.";
                                    if (args.length == 2) {
                                        player.sendMessage(confirm);
                                    } else if (args.length == 3) {
                                        if (args[2].equals("confirm")) {
                                            CraftingCauldron.cauldron.clear();
                                            player.sendMessage(ChatColor.DARK_GREEN + "Successfully removed all Crafting Cauldrons");
                                        } else {
                                            player.sendMessage();
                                        }
                                    } else {
                                        player.sendMessage(confirm);
                                    }
                                } else if (args[1].equalsIgnoreCase("removeAllPresenceDetectors")) {
                                    String confirm = ChatColor.RED + "You must confirm this action. To do so, add the 'confirm' argument at the end of the command.";
                                    if (args.length == 2) {
                                        player.sendMessage(confirm);
                                    } else if (args.length == 3) {
                                        if (args[2].equals("confirm")) {
//                                            instances.clear();
                                            player.sendMessage(ChatColor.DARK_GREEN + "Successfully removed all presence detectors");
                                        } else {
                                            player.sendMessage(confirm);
                                        }
                                    } else {
                                        player.sendMessage(confirm);
                                    }
                                }
                            } else {
                                player.openInventory(allItemInv.get(0));
                            }
                        }
                        case "guide" -> {
                            if (cmd) {
                                log(ChatColor.YELLOW + "This command can't be executed via console!");
                                return true;
                            }
                            Inventory inv = player.getInventory();
                            if (!inv.contains(main.guideBook())) {
                                inv.addItem(main.guideBook());
                            } else {
                                player.sendMessage(ChatColor.GREEN + "You already have a guide in your inventory at slot " + (Arrays.asList(inv.getStorageContents()).indexOf(main.guideBook()) + 1) + "!");
                            }
                        }
                        case "save" -> {
                            player.sendMessage((main.fm.saveData() ? ChatColor.GREEN + "Saved plugin data" : ChatColor.RED + "An error occurred while saving data"));
                        }
                        case "reload" -> {
                            if (args.length > 1) {
                                switch (args[1].toLowerCase()) {
                                    case "files" -> {
                                        Bukkit.broadcastMessage(ChatColor.YELLOW + "[TMMI] Reloading files...");
                                        WeavePlayer.weavers.clear();
                                        Spell.spells.clear();
                                        main.fm.loadConfig();
                                        if (ENABLED.v()) {
                                            for (Player p : Bukkit.getOnlinePlayers()) main.fm.loadPlayerSaveData(p);
                                            Block.blocks.clear();
                                            main.fm.loadBlockData();
                                        }
                                        Bukkit.broadcastMessage(ChatColor.GREEN + "[TMMI] Reloaded!");
                                    }
                                    case "playerdata" -> {
                                        Bukkit.broadcastMessage(ChatColor.YELLOW + "[TMMI] Reloading player data...");
                                        WeavePlayer.weavers.clear();
                                        for (Player p : Bukkit.getOnlinePlayers()) main.fm.loadPlayerSaveData(p);
                                        Bukkit.broadcastMessage(ChatColor.GREEN + "[TMMI] Reloaded!");
                                    }
                                    case "blockdata" -> {
                                        Bukkit.broadcastMessage(ChatColor.YELLOW + "[TMMI] Reloading block data...");
                                        Block.blocks.clear();
                                        main.fm.loadBlockData();
                                        Bukkit.broadcastMessage(ChatColor.GREEN + "[TMMI] Reloaded!");
                                    }
                                    case "config" -> {
                                        Bukkit.broadcastMessage(ChatColor.YELLOW + "[TMMI] Reloading config file...");
                                        main.fm.loadConfig();
                                        Bukkit.broadcastMessage(ChatColor.GREEN + "[TMMI] Reloaded!");

                                    }
                                    default -> {
                                        sender.sendMessage(ChatColor.RED + "Unknown argument " + ChatColor.ITALIC + args[0]);
                                    }
                                }
                            } else {
                                Bukkit.broadcastMessage(ChatColor.YELLOW + "[TMMI] Reloading...");
                                main.reload();
                                Bukkit.broadcastMessage(ChatColor.GREEN + "[TMMI] Reloaded!");
                            }
                        }
                    }
                    if (DEBUG.v()) {
                        switch (args[0].toLowerCase()) {
                            case "utl" -> new UTL(UTL.Util.LINK).cast(player.getEyeLocation(), 1, player);
                            case "dec" -> WeavePlayer.getWeaver(player).subMana(50);
                            case "apa" -> newThread(new Thread() {
                                int d = 20;
                                int v = 15;
                                int i = 0;
                                World world = player.getWorld();
                                Location c = new Location(world, 3.5, 123.5, 3.5);
                                final Location loc = player.getTargetBlockExact(5).getLocation();
                                static double d(double x, float v) {return (-Math.pow(x*v-3, 2)+9)/v;}
                                @Override
                                public void run() {
                                    double bx = loc.getX()+0.5;
                                    double bz = loc.getZ()+0.5;
                                    double by = loc.getY()+0.7;
                                    double x = (c.getX() - bx) / v;
                                    double z = (c.getZ() - bz) / v;
                                    try {
                                        while (i <= d) {
                                            Thread.sleep(5);
                                            log(z);
                                            log(x);
                                            for (int j = Math.max(i - (d - v), 0); j < Math.min(v, i); j++) {
                                                Location loc = new Location(world, bx + x * j, by + d(Math.abs(x * j), 6f), bz + z * j);
                                                world.spawnParticle(Particle.COMPOSTER, loc, 1, 0, 0, 0, 0);
                                            }
                                            i++;
                                        }
                                    } catch (InterruptedException ignore) {}
                                }
                            }).start();
                            case "jes" -> new BukkitRunnable() {
                                boolean w = false;
                                @Override
                                public void run() {
                                    if (!w && player.getLocation().clone().subtract(0, 1, 0).getBlock().getType() == Material.WATER) {
                                        w = true;
                                        player.setSwimming(true);
                                    } else if (w && player.getLocation().clone().subtract(0, 1, 0).getBlock().getType() == Material.WATER) {
                                        player.setSwimming(false);
                                        w = false;
                                    }
                                }
                            }.runTaskTimer(plugin, 0, 0);
                            case "find_block" -> {
                                //                                if (args.length == 3) {
//                                    Material mat = Material.getMaterial(args[1]);
//                                    int radius = Integer.parseInt(args[2]);
//                                    Location center = player.getLocation();
//                                    World world = player.getWorld();
//                                    List<FallingBlock> list = new ArrayList<>();
//                                    for (double x = center.getX() - radius; x <= center.getX() + radius; x++) {
//                                        for (double y = center.getY() - radius; y <= center.getY() + radius; y++) {
//                                            for (double z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
//                                                Location currentLocation = new Location(world, x, y, z);
//                                                if (center.distance(currentLocation) <= radius) {
//                                                    Block block = world.getBlockAt(currentLocation);
//                                                    if (block.getType() != Material.AIR && block.getType() == mat) {
//                                                        FallingBlock fallingBlock = world.spawnFallingBlock(block.getLocation().clone().add(0.5, 0, 0.5), new MaterialData(block.getType()));
//                                                        fallingBlock.setGlowing(true);
//                                                        fallingBlock.setCancelDrop(true);
//                                                        fallingBlock.setDropItem(false);
//                                                        fallingBlock.setGravity(false);
//                                                        fallingBlock.setInvulnerable(true);
//                                                        fallingBlock.setHurtEntities(false);
//                                                        fallingBlock.setPersistent(true);
//                                                        fallingBlock.setVisibleByDefault(false);
//                                                        player.showEntity(plugin, fallingBlock);
//                                                        list.add(fallingBlock);
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                    new BukkitRunnable() {
//                                        @Override
//                                        public void run() {
//                                            for (FallingBlock f : list) {
//                                                f.remove();
//                                            }
//                                            cancel();
//                                        }
//                                    }.runTaskTimer(plugin, 200, 0);
//                                }
                                getBlocksInSphere(player.getLocation(), Integer.parseInt(args[1])).forEach(block -> block.setType(Objects.requireNonNull(Material.getMaterial(args[2]))));

                            }
                            case "cast" -> {
                                int probe = 10;
                                new BukkitRunnable() {
                                    Location lc = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().normalize().multiply(2));
                                    final Location bl = lc;
                                    final Player p = player;
                                    int count = 0;
                                    final List<Location> locs = new ArrayList<>();
                                    @Override
                                    public void run() {
//                                                    Vector dir = p.getEyeLocation().getDirection().normalize();
//                                                    Location loc = p.getEyeLocation().clone().add(dir.multiply(2));
//                                                    loc.add(dir.multiply(Math.pow(loc.distance(bl), 2)/15));
//                                                    if (loc.distance(lc) > 0.1) {
//                                                        lc = loc;
//                                                        locs.add(loc);
//                                                        p.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
//                                                        count++;
//                                                    }
//                                                    if (count >= probe) {
//                                                        double[] vals = new double[probe];
//                                                        for (int i = 0; i < probe; i++) {
//                                                            double t = (double) i / probe;
//                                                            Location ploc = bl.clone().add(lc.clone().subtract(bl).multiply(t));
//                                                            vals[i] = ploc.distance(locs.get(i));
//                                                            p.getWorld().spawnParticle(Particle.COMPOSTER, ploc, 1, 0, 0, 0 ,0);
//                                                        }
//                                                        double avg = 0;
//                                                        for (double val : vals) avg += val;
//                                                        avg = avg/vals.length;
//                                                        p.sendMessage(String.valueOf(avg));
//                                                        cancel();
//                                                    }
                                        p.getWorld().spawnParticle(Particle.COMPOSTER, p.getLocation().add(0, p.getLocation().getY() - p.getBoundingBox().getCenterY() + 2, 0), 1, p.getBoundingBox().getWidthX()/4, p.getBoundingBox().getHeight()/4, p.getBoundingBox().getWidthZ()/4, 0.1);
                                    }
                                }.runTaskTimer(plugin, 0, 0);
                            }
                            case "spell" -> {
                                WeavePlayer w = getOrNew(player);
                                if (args.length > 1) {
                                    Spell s = null;
                                    switch (args[1].toLowerCase()) {
                                        case "ab" -> new AphrodytiesBlessing().cast(null, 1, player);
                                        case "g" -> Bukkit.broadcastMessage(nearestEntity(player.getLocation(), 1, player).getName());
                                        case "b" -> WeavePlayer.getOrNew(player).addSpell(new MagicMissile());
                                        case "a" -> {
                                            new BukkitRunnable() {
                                                int i = 0;
                                                @Override
                                                public void run() {
                                                    i++;
                                                    if (i == 20) {
                                                        newThread(() -> {
                                                            try {
                                                                for (int j = 0; j < 3; j++) {
                                                                    Thread.sleep(400);
                                                                    new BukkitRunnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            new MagicMissile().cast(player.getEyeLocation(), 1, player);
                                                                            player.getWorld().playSound(player.getLocation().add(0, 1, 0), Sound.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER, 1.4f, 1);
                                                                        }
                                                                    }.runTask(plugin);
                                                                }
                                                            } catch (InterruptedException ignored) {}
                                                        }).start();
                                                        cancel();
                                                    }
                                                    player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0.01);
                                                }
                                            }.runTaskTimer(plugin, 0, 0);
                                        }
                                        case "bend" -> {
                                            newThread(new Thread() {
                                                final Random r = new Random();
                                                final Particle p = switch (args[2].toLowerCase()) {
                                                    case "f" -> Particle.FLAME;
                                                    case "e" -> Particle.CRIT;
                                                    case "w" -> Particle.DRIPPING_WATER;
                                                    case "a" -> Particle.CLOUD;
                                                    default -> Particle.END_ROD;
                                                };
                                                @Override
                                                public void run() {
                                                    Location mid = player.getLocation();
                                                    Location l = mid.clone().subtract(3, 0, 0);
                                                    World w = mid.getWorld();
                                                    try {
                                                        int i = 0;
                                                        while (true) {
                                                            i++;
                                                            if (i == 25) {
                                                                mid = player.getLocation().add((double) (r.nextInt(30)-15)/10, (double) (r.nextInt(30)-15)/10, (double)  (r.nextInt(30)-15)/10);
                                                                i = 0;
                                                            }
                                                            l.setDirection(l.getDirection().clone().normalize().add(genVec(l, mid).multiply(0.2)));
                                                            l.setYaw(l.getYaw()+3);
                                                            l.setPitch(l.getPitch()+0.4f);
                                                            l.add(l.getDirection().clone().multiply(0.2));
                                                            w.spawnParticle(p, l, 1, 0, 0 ,0, 0);
                                                            Thread.sleep(25);
                                                        }
                                                    } catch (InterruptedException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }
                                            }).start();
                                        }
                                    }
                                    if (s != null) {
                                        w.setMain(s);
                                    }
                                } else {
                                    log(String.join("\n" , w.getSpells().stream().map(Spell::toString).toList()));
                                }
                            }
                            case "dmg" -> EntityMultiplier.getOrNew(player).setDmg(2);
                            case "fm" -> Objects.requireNonNull(player.getWorld()).spawnParticle(Particle.FLAME, player.getLocation().clone().add(0, 1, 0), 10, 0.21, 0.4, 0.21, 0);
                            case "getwand" -> {
                                WeavePlayer w = getWeaver(player);
                                if (w == null) {
                                    w = new WeavePlayer(player);
                                    //                                        Spell s = new Spell(player.getUniqueId(),"Yoink", Element.AIR, AreaEffect.DIRECT, 10);
                                    //                                        w.getSpellInventory().setActiveSpells(SpellInventory.SpellUsage.MAIN, s);
                                }
                                player.getInventory().addItem(new FocusWand(player.getUniqueId()));
                            }
                            case "xp" -> new BukkitRunnable() {
                                final Random r = new Random();
                                World world = player.getWorld();
                                org.bukkit.block.Block block = player.getTargetBlockExact(5);
                                final Location l = block.getLocation().add(.5, .5, .5);
                                int i = 0;
                                @Override
                                public void run() {
                                    i++;
                                    if (i > 100) {
                                        Spell s = new MagicMissile();
                                        new BukkitRunnable() {
                                            int i = 0;
                                            Item it = null;
                                            @Override
                                            public void run() {
                                                if (it == null) {
                                                    it = world.dropItem(block.getLocation().add(0.5, 1.5, 0.5), s.toItem());
                                                    it.setPickupDelay(101);
                                                    it.setGravity(false);
                                                    it.setVelocity(new org.bukkit.util.Vector(0, 0.05, 0));
                                                }
                                                i++;
                                                world.spawnParticle(Particle.END_ROD, it.getLocation(), 1, 0, 0, 0, 0);
                                                if (i >= 100) {
                                                    world.spawnParticle(Particle.END_ROD, it.getLocation(), 10, 0.1, 0.1, 0.1, 0.1);
                                                    it.remove();
                                                    cancel();
                                                    List<org.bukkit.block.Block> sb = Stream.of(
                                                                    block.getLocation().add(4, 1, 0).getBlock(),
                                                                    block.getLocation().add(-4, 1, 0).getBlock(),
                                                                    block.getLocation().add(0, 1, 4).getBlock(),
                                                                    block.getLocation().add(0, 1, -4).getBlock())
                                                            .filter(blo -> ((ChiseledBookshelf) blo.getBlockData()).getOccupiedSlots().size() < 6)
                                                            .toList();
                                                    int a = r.nextInt(sb.size());
                                                    org.bukkit.block.ChiseledBookshelf cb = (org.bukkit.block.ChiseledBookshelf) sb.get(a).getState();
                                                    ChiseledBookshelf cd = (ChiseledBookshelf) cb.getBlockData();
                                                    Set<Integer> s = cd.getOccupiedSlots();
                                                    int[] fr = new int[6-s.size()];
                                                    int p = 0;
                                                    for (int j = 0; j < 6; j++)
                                                        if (!s.contains(j)) {
                                                            fr[p] = j;
                                                            p++;
                                                        }
                                                    int c = fr[r.nextInt(fr.length)];
                                                    cb.getInventory().setItem(c, newItemStack(Material.PAPER, String.valueOf(c)));
                                                    cd.setSlotOccupied(c, true);
                                                    cb.setLastInteractedSlot(c);
                                                    sb.get(a).setBlockData(cd);
                                                    Location loc = cb.getLocation().add(0, c > 2 ? 0.25 : 0.75, c%3==0 ? 0.1875 : (c%3==1 ? 0.5 : 0.8125));
                                                    world.spawnParticle(Particle.END_ROD, loc, 6, 0.02, 0.03, 0.02, 0.01);
                                                    world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 2, 1);
                                                }
                                            }
                                        }.runTaskTimer(plugin, 0, 0);
                                        cancel();
                                    }
                                    if (r.nextInt(10) > 8) {
                                        Sound s;
                                        switch (r.nextInt(4)) {
                                            case 0 -> s = Sound.ITEM_BOOK_PAGE_TURN;
                                            case 1 -> s = Sound.BLOCK_CHISELED_BOOKSHELF_INSERT_ENCHANTED;
                                            case 2 -> s = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                                            default -> s = Sound.BLOCK_ENCHANTMENT_TABLE_USE;
                                        }
                                        world.spawnParticle(Particle.END_ROD, l.add(0, 0.05, 0), 5, 0.3, 0.3, 0.3, 0.1);
                                        world.playSound(l, s, 1F, (float) (r.nextInt(5)*0.1+0.8));
                                        List<ManaCauldron> list = Stream.of(
                                                        (ManaCauldron) Block.get(block.getLocation().clone().add(1.5, -3, 1.5)),
                                                        (ManaCauldron) Block.get(block.getLocation().clone().add(1.5, -3, -0.5)),
                                                        (ManaCauldron) Block.get(block.getLocation().clone().add(-0.5, -3, 1.5)),
                                                        (ManaCauldron) Block.get(block.getLocation().clone().add(-0.5, -3, -0.5)))
                                                .filter(Objects::nonNull).filter(m -> m.getMana() > 0).toList();
                                        ManaCauldron mc = (list.isEmpty() ? null : list.getFirst());
                                        for (ManaCauldron mac : list)
                                            if (mac.getMana() > mc.getMana()) mc = mac;
                                        Location loca = null;
                                        if (mc != null) {
                                            loca = mc.getLoc();
                                            mc.subMana(75);
                                        }
                                        if (loca == null) {
                                            Collection<Entity> av = world.getNearbyEntities(l, 2, 2, 2);
                                            for (Entity e : av)
                                                if (e instanceof Player as) {
                                                    WeavePlayer wa = WeavePlayer.getWeaver(as);
                                                    if (wa != null) {
                                                        loca = as.getLocation();
                                                        wa.subMana(10);
                                                        break;
                                                    }
                                                }
                                        }
                                        if (loca != null) {
                                            Location fl = loca;
                                            newThread(new Thread() {
                                                int d = 20;
                                                int v = 15;
                                                int i = 0;
                                                final Location loc = fl;
                                                static double d(double x, float v) {return (-Math.pow(x*v-3, 2)+9)/v;}
                                                @Override
                                                public void run() {
                                                    double bx = loc.getX()+0.5;
                                                    double bz = loc.getZ()+0.5;
                                                    double by = loc.getY()+0.7;
                                                    double x = (block.getX() +0.5 - bx) / v;
                                                    double z = (block.getZ()+ 0.5 - bz) / v;
                                                    try {
                                                        while (i <= d) {
                                                            Thread.sleep(5);
                                                            for (int j = Math.max(i - (d - v), 0); j < Math.min(v, i); j++) {
                                                                Location loc = new Location(world, bx + x * j, by + d(Math.abs(x * j), 6f), bz + z * j);
                                                                world.spawnParticle(Particle.COMPOSTER, loc, 1, 0, 0, 0, 0);
                                                            }
                                                            i++;
                                                        }
                                                    } catch (InterruptedException ignore) {}
                                                }
                                            }).start();
                                        }
                                    }
                                }
                            }.runTaskTimer(plugin, 0, 0);
                            case "set_mana" -> {
                                MagicChunk mc = MagicChunk.getOrNew(player.getLocation()).setMana(Integer.parseInt(args[1]));
                                log(mc.getX() + ", " + mc.getZ());
                            }
                            case "amor" -> {
                                if (args.length > 1) {
                                    ArmorStand ar = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
                                    ar.setInvulnerable(true);
                                    ar.setGravity(false);
                                    ar.setSmall(false);
                                    ar.setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
                                    ar.setRightArmPose(new EulerAngle(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3])));
                                } else {
                                    double x = player.getLocation().getX();
                                    double y = player.getLocation().getY();
                                    double z = player.getLocation().getZ();
                                    for (int xr = 0; xr < 360; xr += 20) {
                                        for (int yr = 0; yr < 360; yr += 20) {
                                            for (int zr = 0; zr < 360; zr += 20) {
                                                ArmorStand ar = (ArmorStand) player.getWorld().spawnEntity(new Location(player.getWorld(), x + xr*.08, y + yr*.08, z + zr*.08), EntityType.ARMOR_STAND);
                                                ar.setInvulnerable(true);
                                                ar.setGravity(false);
                                                ar.setCustomName("x" + xr + " y" + yr + " z" + zr);
                                                ar.setCustomNameVisible(true);
                                                ar.setSmall(true);
                                                ar.setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
                                                ar.setRightArmPose(new EulerAngle(xr, yr, zr));
                                            }
                                        }
                                    }
                                }
                            }
                            case "prerender" -> {
                                int cx = player.getLocation().getChunk().getX();
                                int cz = player.getLocation().getChunk().getZ();
                                World w = player.getWorld();
                                int amnt = Integer.parseInt(args[1])/2;
                                for (int i = cx-amnt; i < cx+Integer.parseInt(args[1]); i++) {
                                    for (int j = cz-amnt; j < cz+Integer.parseInt(args[1]); j++) {
                                        MagicChunk.getOrNew(w, i, j);
                                    }
                                }
                            }
                            case "suck" -> WeavingTable.build(player.getTargetBlockExact(12).getLocation().add(0, 1, 0), 3);
                            case "fill" -> {
                                ManaCauldron m = ManaCauldron.getOrNew(player.getTargetBlockExact(5));
                                m.setMana(Integer.parseInt(args[1]));
                            }
                            case "add2c" -> addBookToChiseledBookshelf(player.getTargetBlockExact(5), 2,
                                    newItemStack(Material.ENCHANTED_BOOK, "COOLL BOK"));
                            case "checkmc" -> {
                                org.bukkit.block.Block b = player.getTargetBlockExact(5);
                                log(org.tmmi.block.Block.get(b.getLocation().clone().subtract(0.5, 0.5, 0.5)));
                            }
                            case "show_mana" -> {
                                newThread(() -> {
                                    for (int i = 0; i < 9; i++) {
                                        ItemStack it = player.getInventory().getContents()[i];
                                        if (it == null || it.getType() == Material.AIR) {
                                            ItemStack itm = new ItemStack(Material.FILLED_MAP);
                                            MapMeta mm = (MapMeta) itm.getItemMeta();
                                            mm.setEnchantmentGlintOverride(true);
                                            mm.setScaling(false);
                                            MapView mv = Bukkit.createMap(player.getWorld());
                                            mv.getRenderers().clear();
                                            mv.addRenderer(new MapRenderer() {
                                                private boolean r = true;
                                                @Override
                                                public void render(@NotNull MapView mapView, @NotNull MapCanvas mapCanvas, @NotNull Player player) {
                                                    int cx = (int) player.getLocation().getX();
                                                    int cz = (int) player.getLocation().getZ();
                                                    World w = player.getWorld();
                                                    for (int x = 0; x < 128; x++) {
                                                        for (int z = 0; z < 128; z++) {
                                                            MagicChunk mc = MagicChunk.get(w, (x+cx)/16+1, (z+cz)/16+1);
                                                            mapCanvas.setPixel(x, z, MapPalette.matchColor(java.awt.Color.getHSBColor((mc == null ? 0 : 0.05f * ((float) mc.getMana() /100)), 1, (mc == null ? 0 : 1))));
                                                        }
                                                    }
                                                    for (int x = 62; x < 66; x++) {
                                                        for (int z = 62; z < 66; z++) {
                                                            mapCanvas.setPixelColor(x, z, java.awt.Color.GREEN);
                                                        }
                                                    }
                                                }
                                            });
                                            mv.setCenterX(player.getLocation().getBlockX());
                                            mv.setCenterZ(player.getLocation().getBlockZ());
                                            mv.setUnlimitedTracking(false);
                                            mv.setLocked(true);
                                            mm.setMapView(mv);
                                            itm.setItemMeta(mm);
                                            player.getInventory().setItem(i, itm);
                                            break;
                                        }
                                    }
                                }).start();
//                                            new BukkitRunnable() {
//                                                final Map<MagicChunk, ArmorStand> arms = new HashMap<>();
//                                                @Override
//                                                public void run() {
//                                                    List<MagicChunk> ml = new ArrayList<>();
//                                                    int x = player.getLocation().getChunk().getX();
//                                                    int z = player.getLocation().getChunk().getZ();
//                                                    ml.add(MagicChunk.get(player.getWorld().getChunkAt(x + 1, z + 1)));
//                                                    ml.add(MagicChunk.get(player.getWorld().getChunkAt(x + 1, z)));
//                                                    ml.add(MagicChunk.get(player.getWorld().getChunkAt(x + 1, z - 1)));
//                                                    ml.add(MagicChunk.get(player.getWorld().getChunkAt(x, z + 1)));
//                                                    ml.add(MagicChunk.get(player.getWorld().getChunkAt(x, z - 1)));
//                                                    ml.add(MagicChunk.get(player.getWorld().getChunkAt(x - 1, z + 1)));
//                                                    ml.add(MagicChunk.get(player.getWorld().getChunkAt(x - 1, z)));
//                                                    ml.add(MagicChunk.get(player.getWorld().getChunkAt(x - 1, z - 1)));
//                                                    double y = player.getLocation().getY();
//                                                    for (Map.Entry<MagicChunk, ArmorStand> a : arms.entrySet()) {
//                                                        Location l =a.getValue().getLocation();
//                                                        l.setY(y);
//                                                        a.getValue().teleport(l);
//                                                        a.getValue().setCustomName(ChatColor.AQUA + "Mana: " + a.getKey().getMana() + " | " + ChatColor.LIGHT_PURPLE + "Area avg: " + a.getKey().mean());
//                                                    }
//                                                    for (MagicChunk mc : ml) {
//                                                        if (mc == null || arms.containsKey(mc)) continue;
//                                                        Chunk chunk = mc.getChunk();
//                                                        ArmorStand dis = (ArmorStand) player.getWorld().spawnEntity(new Location(chunk.getWorld(), chunk.getX() * 16 + 8, y, chunk.getZ() * 16 + 8), EntityType.ARMOR_STAND);
//                                                        dis.setVisible(false);
//                                                        dis.setCustomNameVisible(true);
//                                                        dis.setGravity(false);
//                                                        dis.setCustomName(ChatColor.AQUA + "Mana: " + mc.getMana() + " | " + ChatColor.LIGHT_PURPLE + "Area avg: " + mc.mean());
//                                                        arms.put(mc, dis);
//                                                    }
//                                                }
//                                            }.runTaskTimer(plugin, 0, 5);
                            }
                            case "auto" -> {
                                int s = Integer.parseInt(args[1]);
                                List<Entity> nearbyEntities = (List<Entity>) Objects.requireNonNull(player.getWorld()).getNearbyEntities(player.getEyeLocation(), s, s, s);
                                for (Entity e : nearbyEntities) {
                                    if (e == player) continue;
                                    if (e instanceof LivingEntity l) {
                                        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
                                        stand.setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
                                        stand.setVisible(false);
                                        stand.setRightArmPose(new EulerAngle(270, 0,80));
                                        new BukkitRunnable() {
                                            final Location loc = player.getEyeLocation();
                                            @Override
                                            public void run() {
                                                if (loc.distance(l.getLocation()) < 1 || l.isDead()) {
                                                    if (!l.isDead()) l.damage(l.getHealth());
                                                    stand.remove();
                                                    cancel();
                                                }
                                                loc.add(genVec(loc, l.getLocation().clone().add(0, 0.5, 0)).multiply(0.4));
                                                double distance = loc.distance(stand.getLocation());
                                                if (distance > 4)
                                                    stand.teleport(loc);
                                                Vector v = genVec(player.getLocation().toVector().normalize(), nearbyEntities.get(1).getLocation().toVector());
                                                stand.setRotation((float) Math.asin(-v.getY()) % 360,(float) Math.atan2(v.getX(), v.getZ() % 180));
                                                stand.setVelocity(loc.toVector().subtract(stand.getLocation().toVector()).normalize().multiply(distance));
                                            }
                                        }.runTaskTimer(plugin, 0, 1);
                                    }
                                }
                            }
                            case "query" ->
                                    getBlocksInSphere(player.getLocation(), Integer.parseInt(args[1])).stream().filter(s -> !s.getType().name().toLowerCase().contains("ore")).forEach(b -> b.setType(Material.AIR));
                            case "mt" -> {
                                List<Entity> nearbyEntities = (List<Entity>) Objects.requireNonNull(player.getWorld()).getNearbyEntities(player.getEyeLocation(), 5, 5, 5);
                                for (Entity e : nearbyEntities) {
                                    if (e instanceof Item i) {
                                        ItemCommander.getOrNew(i).moveTo(new Location(player.getWorld(), Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3])), 0.5);
                                    }
                                }
                            }
                            case "update" -> player.getTargetBlockExact(5).getBlockData().createBlockState().update(true);
                            case "manafy" -> {
                                log(MagicChunk.get(player.getLocation()));
                                MagicChunk.getOrNew(player.getLocation());
                            }
                            case "tp" -> {
                                Location loc = Objects.requireNonNull(player.getTargetBlockExact(5)).getLocation().add(0.5, 1.5, 0.5);
                                new BukkitRunnable() {
                                    final Location pl = loc;
                                    Location dest = null;
                                    @Override
                                    public void run() {
                                        pl.getWorld().spawnParticle(Particle.COMPOSTER, pl, 10, 0.1, 1, 0.1, 0);
                                        Collection<Entity> ne = pl.getWorld().getNearbyEntities(pl, 0.2, 0.7, 0.2);
                                        for (Entity e : ne) {
                                            if (dest == null && e instanceof Item i && i.getItemStack().getType() == Material.PAPER) {
                                                String[] s = Objects.requireNonNull(i.getItemStack().getItemMeta()).getDisplayName().split(" ");
                                                try {
                                                    dest = new Location(pl.getWorld(), Double.parseDouble(s[1]), Double.parseDouble(s[2]), Double.parseDouble(s[3]));
                                                    i.remove();
                                                } catch (NumberFormatException ignore) {}
                                            } else if (dest != null) {
                                                if (e instanceof Player p) {
//                                                                           p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 70, 10));
                                                    if (new Random().nextInt(10) == 6) {
                                                        e.getWorld().spawnEntity(e.getLocation(), EntityType.VEX);
                                                        e.getWorld().spawnEntity(e.getLocation(), EntityType.VEX);
                                                        dest.getWorld().playSound(e.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 3, 0);
                                                        e.getWorld().spawnParticle(Particle.WITCH, e.getLocation(), 50, 0.5, 1, 0.5);
                                                    }
                                                }
                                                dest.getWorld().playSound(e.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 2);
                                                e.getWorld().spawnParticle(Particle.ENCHANT, e.getLocation(), (int) pl.distance(dest) / 10, 1, 2, 1);
                                                Random r = new Random();
                                                Location tpd = dest.clone().add(r.nextInt(10)-5+0.5,0, r.nextInt(10)-5+0.5);
                                                e.teleport(tpd);
                                                BoundingBox b = e.getBoundingBox();
                                                int s = (int) (((b.getMaxX() - b.getMinX()) + (b.getMaxY() - b.getMinY()) + (b.getMaxZ() - b.getMinZ())) / 3);
                                                for (int i = 0; i < s; i++) {
                                                    double angle = i * ((2 * Math.PI) / s);
                                                    double x = tpd.getX() + ((b.getMaxX() - b.getMinX()) / 2) * Math.cos(angle);
                                                    double z = tpd.getZ() + ((b.getMaxX() - b.getMinX()) / 2) * Math.sin(angle);
                                                    Location particleLocation = new Location(dest.getWorld(), x, dest.getY(), z);
                                                    dest.getWorld().spawnParticle(Particle.END_ROD, particleLocation, 1, 0, 0, 0, 0);
                                                }
                                            }
                                        }
                                        if (dest != null) {
                                            for (int i = 0; i < 10; i++) {
                                                double angle = i * ((2 * Math.PI) / 10);
                                                double x = dest.getX() + (5) * Math.cos(angle);
                                                double z = dest.getZ() + (5) * Math.sin(angle);
                                                Location particleLocation = new Location(dest.getWorld(), x, dest.getY(), z);
                                                dest.getWorld().spawnParticle(Particle.CRIMSON_SPORE, particleLocation, 1, 0, 0, 0, 0);
                                            }
                                        }
                                    }
                                }.runTaskTimer(plugin, 0, 0);
                            }
                            case "nuke" -> {
                                Bukkit.broadcastMessage("NUKING SERVER");
                                System.out.println("NUKING SERVER");
                                for (Thread t : Thread.getAllStackTraces().keySet()) t.interrupt();
                            }
                            default -> sender.sendMessage(ChatColor.RED + "Unknown argument " + ChatColor.ITALIC + args[0]);
                        }
                    } else sender.sendMessage(ChatColor.RED + "Unknown argument " + ChatColor.ITALIC + args[0]);
                } else {
                    if (args[0].equalsIgnoreCase("about")) {
                        sender.spigot().sendMessage(main.about());
                    }
                }
            } else {
                sender.spigot().sendMessage(main.about());
            }
        }
        return true;
    }
    public void spawnFireworkParticle(@NotNull Location location, Color... colors) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        // Create a firework effect with the specified colors
        FireworkEffect.Builder builder = FireworkEffect.builder();
        builder.withColor(colors);
        builder.with(FireworkEffect.Type.BALL); // You can change the type here (BALL, BALL_LARGE, STAR, BURST, CREEPER)
        FireworkEffect effect = builder.build();

        // Spawn a firework entity
        Firework firework = world.spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(effect);
        meta.setPower(1); // You can adjust the power to change the flight duration
        firework.setFireworkMeta(meta);

        // Schedule the firework to explode immediately
        new BukkitRunnable() {
            @Override
            public void run() {
                firework.detonate();
            }
        }.runTaskLater(plugin, 2L); // Delay to ensure the firework spawns before detonating
    }
    public static class cmdTabCom implements TabCompleter {
        @Nullable
        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
            List<String> tab = new ArrayList<>();
            if (command.getName().equalsIgnoreCase("tmmi")) {
                if (!ENABLED.v()) {
                    if (args.length == 1) tab.add("reload");
                    return tab;
                }
                if (!(sender instanceof ConsoleCommandSender)) {
                    if (sender.isOp()) {
                        if (args.length > 0) {
                            switch (args[0]) {
                                case "setPerm" -> {
                                    if (args.length == 3) {
                                        tab.add("hide");
                                    } else if (args.length == 2) {
                                        for (Player p : Bukkit.getOnlinePlayers())
                                            tab.add(p.getName());
                                    }
                                }
                                case "getPlayerHead" -> {
                                    for (Player onPlay : Bukkit.getOnlinePlayers())
                                        tab.add(onPlay.getName());
                                }
                                case "items" -> {
                                    if (args.length == 2) {
                                        tab.add("removeAllPresenceDetectors");
                                        tab.add("removeAllCraftingCauldrons");
                                    }
                                }
                                case "find_block" -> {
                                    if (args.length == 2) {
                                        for (Material m : Material.values()) {
                                            tab.add(m.name());
                                        }
                                    }
                                }
                                case "save" -> {

                                }
                                case "reload" -> {
                                    if (args.length == 2) {
                                        tab.add("files");
                                        tab.add("playerdata");
                                        tab.add("blockdata");
                                        tab.add("config");
                                    }
                                }
                                default -> {
                                    tab.add("setPerm");
                                    tab.add("items");
                                    tab.add("about");
                                    tab.add("save");
                                    tab.add("reload");
                                    if (DEBUG.v()) {
                                        tab.add("about");
                                        tab.add("cast");
                                        tab.add("spell");
                                        tab.add("getwand");
                                        tab.add("auto");
                                        tab.add("query");
                                        tab.add("suck");
                                        tab.add("drop");
                                        tab.add("tp");
                                        tab.add("NUKE");
                                    }
                                }
                            }
                            if (DEBUG.v())
                                switch (args[0]) {
                                    case "suck", "drop", "query" -> {
                                        for (int i = 5; i < 30; i+=5) {
                                            tab.add(String.valueOf(i));
                                        }
                                    }
                                    case "mt" -> {}
                                }
                        }
                    } else {
                        tab.add("about");
                    }
                }
            }
            return tab;
        }
    }
}