package org.tmmi;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.ChiseledBookshelf;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.*;
import org.bukkit.util.Vector;
import org.hetils.FileVersion;
import org.hetils.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.spells.*;
import org.tmmi.spells.atributes.AreaEffect;
import org.tmmi.block.*;
import org.tmmi.items.FocusWand;
import org.tmmi.items.ItemCommand;
import org.tmmi.items.SpellBook;
import org.tmmi.spells.atributes.Weight;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

import static org.hetils.Util.*;
import static org.tmmi.Element.getItem;
import static org.tmmi.Structure.setData;
import static org.tmmi.WeavePlayer.getOrNew;
import static org.tmmi.WeavePlayer.getWeaver;
import static org.tmmi.block.Presence.*;

public class Main extends JavaPlugin {
    public Map<UUID, Boolean> permission = new HashMap<>();
    public static FileVersion FILES_VERSION = new FileVersion(1,0,0);
    public static FileVersion PLUGIN_VERSION = new FileVersion(1,0,0);
    public static String UUID_SEQUENCE = toHex(Main.class.getPackage().getSpecificationVersion(), 4)
            +toHex(PLUGIN_VERSION, 4)+'-';
    public static int unclickable = 2147837;
    public static Plugin plugin;
    public Collection<Class<?>> classes = new HashSet<>();
    public static boolean DISABLED;

    public static final Property<FileVersion> FILE_VERSION = new Property<>("FILE_VERSION", Main.PLUGIN_VERSION);
    public static final Property<Boolean> ENABLED = new Property<>("ENABLED", true);
    public static final Property<Boolean> GARBAGE_COLLECTION = new Property<>("GARBAGE_COLLECTION", true);
    public static final Property<Boolean> AUTOSAVE = new Property<>("AUTOSAVE", true);
    public static final Property<Boolean> AUTOSAVE_MSG = new Property<>("AUTOSAVE_MSG", true);
    public static final Property<String> AUTOSAVE_MSG_VALUE = new Property<>("AUTOSAVE_MSG_VALUE", "Autosaving...");
    public static final Property<Integer> AUTOSAVE_FREQUENCY = new Property<>("AUTOSAVE_FREQUENCY", 1800);
    public static final Property<Boolean> CUSTOM_SPELLS = new Property<>("CUSTOM_SPELLS", false);
    public static final Property<List<UUID>> DISABLED_SPELLS = new Property<>("DISABLED_SPELLS", new ArrayList<>());
    public static final Property<Double> SPELL_SPEED_CAP = new Property<>("SPELL_SPEED_CAP", 20d);
    public static final Property<Double> SPELL_TRAVEL_CAP = new Property<>("SPELL_TRAVEL_CAP", 20d);
    public static final Property<Double> SPELL_DAMAGE_CAP = new Property<>("SPELL_DAMAGE_CAP", 20d);
    public static final Property<Integer> CHUNK_TREE_DEPTH = new Property<>("CHUNK_TREE_DEPTH", 3);
    public static final Property<Boolean> LEGACY_STI_SPELL = new Property<>("LEGACY_STI_SPELL", false);

    public static final Property<Boolean> DEBUG = new Property<>("DEBUG_n_TEST", false);
    public static Map<String, Object> properties = new HashMap<>();

    public static Thread autosave;

    public static Map<UUID, Boolean> spellPerms = new HashMap<>();
    public static List<Inventory> allItemInv = List.of(Bukkit.createInventory(null, 54, "pg1"));

    public static ItemStack background;
    private @NotNull ItemStack guideBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        assert bookMeta != null;
        bookMeta.setTitle("TMMI guide");
        bookMeta.setAuthor("Hexanilix");
        List<String> pages = new ArrayList<>();
        pages.add(ChatColor.DARK_AQUA +
                "-=-=-=-=-=-=-=-=-=-" +
                "Welcome to the TMMI guide! In this guide you'll be shown all the features of this plugin.\n" +
                "Flip to next page for 'All Items', or select a chapter:\n" + ChatColor.GOLD + ChatColor.BOLD +
                "All Items\n" +
                "Item Usages\n" +
                "Moderation with TMMI\n");
        bookMeta.setPages(pages);
        bookMeta.addEnchant(Enchantment.LOYALTY, 1, false);
        book.setItemMeta(bookMeta);
        return book;
    }
    private BaseComponent[] about() {
        ComponentBuilder message = new ComponentBuilder(ChatColor.AQUA +
                "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n" +
                "        Too Many Magical Item " + ChatColor.RED +
                "(" + this.getDescription().getVersion() + ")" + "\n" + ChatColor.AQUA +
                "Welcome to the Too Many Magical Item plugin, or\nTMMI for short. This plugin adds a variety of\n" +
                "different magical items and blocks, at least as\nof now. This plugin is still in early stages of\n" +
                "development so it may have a lot of bugs and\nunpolished features. It's normal, but not" +
                "intentional\nso for any bugs or unwanted behaviour you may\nencounter I ask to please " +
                "report\nthem ");
        ComponentBuilder clickableWord = new ComponentBuilder("here");
        clickableWord.color(ChatColor.GREEN.asBungee());
        clickableWord.event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://google.com"));
        message.append(clickableWord.getCurrentComponent());
        ComponentBuilder message2 = new ComponentBuilder("\n" + ChatColor.GOLD + "~ For a guide to start ");
        ComponentBuilder quickGuide = new ComponentBuilder("click me!");
        quickGuide.color(ChatColor.LIGHT_PURPLE.asBungee());
        quickGuide.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tmmi guide"));
        message2.append(ChatColor.AQUA + "\n-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        message.append(message2.create());
        return message.create();
    }

    public static void log(Object msg) {
        log(Level.INFO, msg);
    }
    public static void log(Level lv, Object message) {
        Bukkit.getLogger().log(lv, "[TMMI] " + message);
    }
    public static Collection<Thread> threads = new ArrayList<>();
    public static @NotNull Thread newThread(Runnable run) {
        Thread t = new Thread(run);
        threads.add(t);
        return t;
    }
    public final FileManager fm = new FileManager(this);

    @Override
    public void onEnable() {
        if (!this.getFile().canRead() && !this.getFile().canWrite()) {
            if (!this.getFile().canRead()) {
                log(Level.SEVERE, "Plugin does not have required permission to create necessary files. Please grant appropriate permissions to this file to continue");
                super.onDisable();
            } else {
                log(Level.SEVERE, "Plugin does not have required permission to read necessary files. Please grant appropriate permissions to this file to continue");
                super.onDisable();
            }
        } else {
            loadClasses();
            plugin = this;
            fm.createFiles();
            fm.loadConfig();
            Objects.requireNonNull(Bukkit.getPluginCommand("tmmi")).setExecutor(new cmd());
            Objects.requireNonNull(Bukkit.getPluginCommand("tmmi")).setTabCompleter(new cmd.cmdTabCom());
            if (ENABLED.v()) {
                MagicChunk.treeDepth = CHUNK_TREE_DEPTH.v();
                Spell.mxS = SPELL_SPEED_CAP.v();
                Spell.mxT = SPELL_TRAVEL_CAP.v();
                Spell.mxD = SPELL_DAMAGE_CAP.v();
                if (fm.checkFilesAndCreate()) {
                    if (loadClasses()) {
                        if (AUTOSAVE_FREQUENCY.v() < 10) {
                            AUTOSAVE_FREQUENCY.setV(10);
                            fm.updateConfig();
                        }
                        if (AUTOSAVE.v()) {
                            autosave = newThread(() -> {
                                try {
                                    while (true) {
                                        Thread.sleep(AUTOSAVE_FREQUENCY.v() * 1000L);
                                        fm.saveData();
                                        if (GARBAGE_COLLECTION.v()) System.gc();
                                        if (AUTOSAVE_MSG.v()) log(AUTOSAVE_MSG_VALUE.v());
                                    }
                                } catch (InterruptedException e) {
                                    if (!DISABLED) log(Level.WARNING, "Autosave interrupted. Plugin data will not be saved until plugin disable and any new data acquired this point will be lost in case of a unprecedented stop");
                                }
                            });
                            autosave.start();
                        }
                        fm.loadBlockData();
                        background = newItemStack(Material.BLACK_STAINED_GLASS_PANE, " ", unclickable);
                        setItems();
                        Bukkit.getPluginManager().registerEvents(new MainListener(), this);
                        for (Player p : Bukkit.getOnlinePlayers()) fm.loadPlayerSaveData(p);
                        log("Plugin loaded successfully");
                    }
                }
            } else log(Level.WARNING, "Plugin is soft disabled in config, make sure this is a change you wanted");
        }
    }

    //Element mainElement, Element secondaryElement, AreaEffect castAreaEffect , double baseDamage, double speed, double travel
//    private static Spell newSpell(@NotNull Type t, UUID id, UUID handler, String name, Weight weight, int level, int experience, int castCost, Object... param) {
//        Spell s = null;
//        switch (t) {
//            case ATK -> {
//                Element me = param[0] instanceof Element e ? e : null;
//                Element se = param[1] instanceof Element e ? e : null;
//                AreaEffect ef = param[2] instanceof AreaEffect a ? a : null;
//                assert me != null;
//                assert ef != null;
//                s = new ATK(id, handler, name, weight, level, experience, castCost, me, se, ef, 100);
//            }
//            case DEF -> {
//                AreaEffect ef = param[1] instanceof AreaEffect a ? a : null;
//                assert ef != null;
//                s = new DEF(id, handler, name, weight, level, experience, castCost, Element.AIR, ef, 100, 1000);
//            }
//        }
//        return s;
//    }

    public static void addBookToChiseledBookshelf(org.bukkit.block.@NotNull Block block, int slot, ItemStack book) {
        if (block.getType() == Material.CHISELED_BOOKSHELF) {
            if (block.getState() instanceof org.bukkit.block.ChiseledBookshelf bookshelf)
                bookshelf.getInventory().setItem(slot, book);
            block.getState().update();
        }
    }
    public static @NotNull ItemStack newBook(String name, List<String> lore, int data, String author, String title, BookMeta.Generation gen, String... pages) {
        ItemStack i = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta m = (BookMeta) i.getItemMeta();
        assert m != null;
        m.setDisplayName(name);
        if (data > 0)
            m.setCustomModelData(data);
        if (lore != null)
            m.setLore(lore);
        m.setAuthor(author);
        m.setTitle(title);
        m.setGeneration(gen);
        m.setPages(pages);
        i.setItemMeta(m);
        return i;
    }

    private class cmd implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (command.getName().equalsIgnoreCase("tmmi")) {
                if (!ENABLED.v()) {
                    if (args[0].equalsIgnoreCase("reload")) {
                        Bukkit.broadcastMessage(ChatColor.YELLOW + "[TMMI] Reloading...");
                        reload();
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
                                        permission.replace(desPlayer.getUniqueId(), !permission.get(desPlayer.getUniqueId()));
                                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Set " + desPlayer.getName() + "'s permission to " + (permission.get(desPlayer.getUniqueId()) ? ChatColor.GREEN + "allow magic" : ChatColor.YELLOW + "forbid magic"));
                                        if (!(args.length == 3 && args[2].equalsIgnoreCase("hide"))) {
                                            desPlayer.sendMessage("You now" + (permission.get(desPlayer.getUniqueId()) ? ChatColor.GREEN + " have permission to use magic" : ChatColor.YELLOW + "You now don't have permission to use magic"));
                                        }
                                        desPlayer.recalculatePermissions();
                                    }
                                } else if (args.length < 2) {
                                    if (cmd)
                                        console.sendMessage(ChatColor.YELLOW + "Please specify player!");
                                    else {
                                        permission.replace(player.getUniqueId(), !permission.get(player.getUniqueId()));
                                        player.sendMessage("You now" + (permission.get(player.getUniqueId()) ? ChatColor.GREEN + " have permission to use magic" : ChatColor.YELLOW + "You now don't have permission to use magic"));
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
                                                instances.clear();
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
                                if (!inv.contains(guideBook())) {
                                    inv.addItem(guideBook());
                                } else {
                                    player.sendMessage(ChatColor.GREEN + "You already have a guide in your inventory at slot " + (Arrays.asList(inv.getStorageContents()).indexOf(guideBook()) + 1) + "!");
                                }
                            }
                            case "save" -> {
                                player.sendMessage((fm.saveData() ? ChatColor.GREEN + "Saved plugin data" : ChatColor.RED + "An error occurred while saving data"));
                            }
                            case "reload" -> {
                                if (args.length > 1) {
                                    switch (args[1].toLowerCase()) {
                                        case "files" -> {
                                            Bukkit.broadcastMessage(ChatColor.YELLOW + "[TMMI] Reloading files...");
                                            WeavePlayer.weavers.clear();
                                            Spell.spells.clear();
                                            fm.loadConfig();
                                            if (ENABLED.v()) {
                                                for (Player p : Bukkit.getOnlinePlayers()) fm.loadPlayerSaveData(p);
                                                Block.blocks.clear();
                                                fm.loadBlockData();
                                            }
                                            Bukkit.broadcastMessage(ChatColor.GREEN + "[TMMI] Reloaded!");
                                        }
                                        case "playerdata" -> {
                                            Bukkit.broadcastMessage(ChatColor.YELLOW + "[TMMI] Reloading player data...");
                                            WeavePlayer.weavers.clear();
                                            for (Player p : Bukkit.getOnlinePlayers()) fm.loadPlayerSaveData(p);
                                            Bukkit.broadcastMessage(ChatColor.GREEN + "[TMMI] Reloaded!");
                                        }
                                        case "blockdata" -> {
                                            Bukkit.broadcastMessage(ChatColor.YELLOW + "[TMMI] Reloading block data...");
                                            Block.blocks.clear();
                                            fm.loadBlockData();
                                            Bukkit.broadcastMessage(ChatColor.GREEN + "[TMMI] Reloaded!");
                                        }
                                        case "config" -> {
                                            Bukkit.broadcastMessage(ChatColor.YELLOW + "[TMMI] Reloading config file...");
                                            fm.loadConfig();
                                            Bukkit.broadcastMessage(ChatColor.GREEN + "[TMMI] Reloaded!");

                                        }
                                        default -> {
                                            sender.sendMessage(ChatColor.RED + "Unknown argument " + ChatColor.ITALIC + args[0]);
                                        }
                                    }
                                } else {
                                    Bukkit.broadcastMessage(ChatColor.YELLOW + "[TMMI] Reloading...");
                                    reload();
                                    Bukkit.broadcastMessage(ChatColor.GREEN + "[TMMI] Reloaded!");
                                }
                            }
                            default -> {
                                if (DEBUG.v()) {
                                    switch (args[0].toLowerCase()) {
                                        case "apa" -> {
                                            newThread(new Thread() {
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
                                        }
                                        case "jes" -> {
                                            new BukkitRunnable() {
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
                                        }
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
                                                    case "a" ->
                                                            s = new ATK(null, player.getUniqueId(), "ATK", Weight.CANTRIP, 1, 0, 10, Element.FIRE, null, AreaEffect.DIRECT, 1, 10, 2);
                                                    case "u" ->
                                                            s = new UTL(UTL.Util.MINE, player);
                                                    case "d" ->
                                                            s = new DEF(null, player.getUniqueId(), "DEF", Weight.CANTRIP, 1, 0, 10, Element.WATER, AreaEffect.DIRECT, 200, 1000);
                                                    case "s" ->
                                                            s = new STI(STI.Stat.DMG, player.getUniqueId());
                                                }
                                                if (s != null) {
                                                    w.setMain(s);
                                                }
                                            } else {
                                                log(String.join("\n" , w.getSpells().stream().map(Spell::toString).toList()));
                                            }
                                        }
                                        case "dmg" -> {
                                            EntityMultiplier.getOrNew(player).setDmg(2);
                                        }
                                        case "fm" -> {
                                            Objects.requireNonNull(player.getWorld()).spawnParticle(Particle.FLAME, player.getLocation().clone().add(0, 1, 0), 10, 0.21, 0.4, 0.21, 0);
                                        }
                                        case "getwand" -> {
                                            WeavePlayer w = getWeaver(player);
                                            if (w == null) {
                                                w = new WeavePlayer(player);
                                                //                                        Spell s = new Spell(player.getUniqueId(),"Yoink", Element.AIR, AreaEffect.DIRECT, 10);
                                                //                                        w.getSpellInventory().setActiveSpells(SpellInventory.SpellUsage.MAIN, s);
                                            }
                                            player.getInventory().addItem(new FocusWand(player.getUniqueId()));
                                        }
                                        case "xp" -> {
                                            new BukkitRunnable() {
                                                final Random r = new Random();
                                                World world = player.getWorld();
                                                org.bukkit.block.Block block = player.getTargetBlockExact(5);
                                                final Location l = block.getLocation().add(.5, .5, .5);
                                                int i = 0;
                                                @Override
                                                public void run() {
                                                    i++;
                                                    if (i > 100) {
                                                        Spell s = new ATK(Bukkit.getPlayer("Hexanilix").getUniqueId(), "atk", Weight.CANTRIP, Element.FIRE, null, AreaEffect.DIRECT);
                                                        new BukkitRunnable() {
                                                            int i = 0;
                                                            Item it = null;
                                                            @Override
                                                            public void run() {
                                                                if (it == null) {
                                                                    it = world.dropItem(block.getLocation().add(0.5, 1.5, 0.5), s.toItem());
                                                                    it.setPickupDelay(101);
                                                                    it.setGravity(false);
                                                                    it.setVelocity(new Vector(0, 0.05, 0));
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
                                                                        (ManaCauldron) org.tmmi.block.Block.get(block.getLocation().clone().add(1.5, -3, 1.5)),
                                                                        (ManaCauldron) org.tmmi.block.Block.get(block.getLocation().clone().add(1.5, -3, -0.5)),
                                                                        (ManaCauldron) org.tmmi.block.Block.get(block.getLocation().clone().add(-0.5, -3, 1.5)),
                                                                        (ManaCauldron) org.tmmi.block.Block.get(block.getLocation().clone().add(-0.5, -3, -0.5)))
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
                                        }
                                        case "set_mana" -> {
                                            MagicChunk mc = MagicChunk.getOrNew(player.getLocation()).setMana(Integer.parseInt(args[1]));
                                            log(mc.getX() + ", " + mc.getZ());
                                        }
                                        case "amor" -> {
                                            double x = player.getLocation().getX();
                                            double y = player.getLocation().getY();
                                            double z = player.getLocation().getZ();
                                            for (int i = 0; i < 360; i+=5) {
                                                ArmorStand ar = (ArmorStand) player.getWorld().spawnEntity(new Location(player.getWorld(), x + i*0.5, y, z), EntityType.ARMOR_STAND);
                                                ar.setInvulnerable(true);
                                                ar.setGravity(false);
                                                ar.setSmall(true);
                                                ar.setCustomName(String.valueOf(i));
                                                ar.setCustomNameVisible(true);
                                                ar.setItemInHand(new ItemStack(Material.ENCHANTED_BOOK));
                                                ar.setRightArmPose(new EulerAngle(i*10, 0, 0));
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
                                        case "struct" -> {
                                            Structure.grandWeaver.build(player.getLocation());
//                                            Structure.setData(player.getTargetBlockExact(5), false, false, BlockFace.SOUTH);
                                        }
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
                                        case "query" -> {
                                            getSphere(player.getLocation(), Integer.parseInt(args[1])).stream().filter(s -> !s.getType().name().toLowerCase().contains("ore")).forEach(b -> b.setType(Material.AIR));
                                        }
                                        case "mt" -> {
                                            List<Entity> nearbyEntities = (List<Entity>) Objects.requireNonNull(player.getWorld()).getNearbyEntities(player.getEyeLocation(), 5, 5, 5);
                                            for (Entity e : nearbyEntities) {
                                                if (e instanceof Item i) {
                                                    ItemCommand.getOrNew(i).moveTo(new Location(player.getWorld(), Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3])), 0.5);
                                                }
                                            }
                                        }
                                        case "update" -> player.getTargetBlockExact(5).getBlockData().createBlockState().update(true);
                                        case "suck" -> {
                                            Location l = player.getLocation().getBlock().getLocation();
                                            final Structure s = Structure.grandWeaver;
                                            final int zt = s.depth;
                                            final int xt = s.width;
                                            final int ar = zt*xt;
                                            final int h = s.height*ar-1;
                                            log(h);
                                            log(s.height);
                                            List<org.bukkit.block.Block> bl = new ArrayList<>();
                                            new BukkitRunnable() {
                                                int t = ar/2;
                                                @Override
                                                public void run() {
                                                    Structure.SBD sb = s.getAt(t);
                                                    while (sb != null && sb.m.isAir()) {
                                                        if (t % ar <= 0) t += ar + (ar / 2);
                                                        t--;
                                                        sb = s.getAt(t);
                                                    }
                                                    if (sb == null) cancel();
                                                    else {
                                                        int y = t / ar;
                                                        int z = (t % ar) / zt;
                                                        int x = (t % ar) % xt;
                                                        org.bukkit.block.Block b = l.clone().add(x, y, z).getBlock();
                                                        b.setType(sb.m);
                                                        setData(b, true, true, sb.meta);
                                                        bl.add(b);
                                                    }
                                                    t--;
                                                }
                                            }.runTaskTimer(plugin, 0, 0);
                                            new BukkitRunnable() {
                                                int t = ar/2;
                                                @Override
                                                public void run() {
                                                    Structure.SBD sb = s.getAt(t);
                                                    while (sb != null && sb.m.isAir()) {
                                                        if (t%ar>=ar-1) t += ar-(ar/2);
                                                        t++;
                                                        sb = s.getAt(t);
                                                    }
                                                    if (sb == null) {
                                                        cancel();
                                                        l.clone().add(5, 1, 5).getBlock().setType(s.getAt(181).m);
                                                        l.clone().add(5, 2, 5).getBlock().setType(s.getAt(302).m);
                                                        l.clone().add(5, 3, 5).getBlock().setType(Material.ENCHANTING_TABLE);
                                                        new WeavingTable(l.clone().add(5, 3, 5).getBlock());
                                                        Location lc = l.clone().add((double) xt /2, (double) s.height /2, (double) zt /2);
                                                        for (org.bukkit.block.Block b : bl) {
                                                            l.getWorld().spawnParticle(Particle.FIREWORK, lc, 1, 1, 1, 1, 0.2);
                                                        }
                                                        l.getWorld().playSound(lc, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                                    }
                                                    else {
                                                        int y = t / ar;
                                                        int z = (t % ar) / zt;
                                                        int x = (t % ar) % xt;
                                                        org.bukkit.block.Block b = l.clone().add(x, y, z).getBlock();
                                                        b.setType(sb.m);
                                                        setData(b, true, true, sb.meta);
                                                        bl.add(b);
                                                    }
                                                    t++;
                                                }
                                            }.runTaskTimer(plugin, 0, 0);
                                        }
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
                            }
                            //DEBUG COMMANDS
                        }
                    } else {
                        if (args[0].equalsIgnoreCase("about")) {
                            sender.spigot().sendMessage(about());
                        }
                    }
                } else {
                    sender.spigot().sendMessage(about());
                }
            }
            return true;
        }
        public void spawnFireworkParticle(Location location, Color... colors) {
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

    public static void setCauldronFillLevel(org.bukkit.block.@NotNull Block b, int i) {
        if (b.getType() == Material.CAULDRON || b.getType() == Material.WATER_CAULDRON)
            if (i > -1 && i < 4) {
                if (i == 0) b.setType(Material.CAULDRON);
                else {
                    if (b.getType() != Material.WATER_CAULDRON) b.setType(Material.WATER_CAULDRON);
                    Levelled l = (Levelled) b.getBlockData();
                    l.setLevel(i);
                    b.setBlockData(l);
                }
            }
    }

    public boolean loadClasses() {
        try {
            classes.add(Class.forName("org.tmmi.Main"));
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setItems() {
        ItemStack fusionCrys = newItemStack(Material.END_CRYSTAL, ChatColor.DARK_AQUA + "Fusion Crystal", 365450, List.of());
        allItemInv.getFirst().addItem(SpellWeaver.item, new SpellBook(), SpellAbsorbingBlock.item, WeavingTable.item);
        for (Element e : Element.values()) allItemInv.getFirst().addItem(getItem(e));
        for (AreaEffect e : AreaEffect.values()) allItemInv.getFirst().addItem(AreaEffect.getItem(e));
        {
            NamespacedKey key = new NamespacedKey(this, "fusion_crystal");
            ShapedRecipe r = new ShapedRecipe(key, fusionCrys);
            r.shape(
                    "ATA",
                    "GCN",
                    "AEA");
            r.setIngredient('A', Material.AMETHYST_SHARD);
            r.setIngredient('C', Material.END_CRYSTAL);
            r.setIngredient('N', Material.NETHERITE_SCRAP);
            r.setIngredient('E', Material.ENDER_EYE);
            r.setIngredient('G', Material.GOLDEN_APPLE);
            r.setIngredient('T', Material.TOTEM_OF_UNDYING);
            Bukkit.getServer().addRecipe(r);
            Permission permission = new Permission("tmmi.craft." + r.getKey().getKey(), "fusion_crystal");
            Bukkit.getServer().getPluginManager().addPermission(permission);
            allItemInv.getFirst().addItem(fusionCrys);
        }
//        {
//            NamespacedKey key = new NamespacedKey(this, "crafting_cauldron");
//            ShapedRecipe crfCReci = new ShapedRecipe(key, CrafttingCauldron.item);
//            crfCReci.shape(
//                    "ADA",
//                    "ECE",
//                    "AUA");
//            crfCReci.setIngredient('A', Material.AIR);
//            crfCReci.setIngredient('E', Material.ECHO_SHARD);
//            crfCReci.setIngredient('U', Material.NETHERITE_SCRAP);
//            crfCReci.setIngredient('C', new RecipeChoice.ExactChoice(fusionCrys));
//            crfCReci.setIngredient('D', Material.DIAMOND);
//            Bukkit.getServer().addRecipe(crfCReci);
//            Permission permission = new Permission("tmmi.craft." + crfCReci.getKey().getKey(), "Crafting cauldron perm");
//            Bukkit.getServer().getPluginManager().addPermission(permission);
//        }
    }
    private static final List<Item> selItms = new ArrayList<>();

    public class MainListener implements Listener {
        @EventHandler
        public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
            Player p = event.getPlayer();
            spellPerms.putIfAbsent(p.getUniqueId(), false);
            fm.loadPlayerSaveData(p);
            WeavePlayer w = getOrNew(p);
            new BukkitRunnable() {

                @Override
                public void run() {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(String.valueOf(w.getMana())));
                }
            }.runTaskTimer(Main.this, 0, 0);
        }

        @EventHandler
        public void onPlayerLeave(@NotNull PlayerQuitEvent event) {
            fm.savePlayerData(event.getPlayer());
        }
        @EventHandler
        public void onBow(@NotNull EntityShootBowEvent event) {
            if (event.getEntity() instanceof Player player) {
                new BukkitRunnable() {
                    LivingEntity liver = null;
                    final Arrow ar = (Arrow) event.getProjectile();

                    @Override
                    public void run() {
                        Location cl = ar.getLocation().clone();
                        cl.setYaw(-cl.getYaw());
                        cl.setPitch(-cl.getPitch());
                        double m = Math.pow(vecAvg(ar.getVelocity())*4, 2) / 4;
                        cl.add(cl.getDirection().multiply(m));
                        ar.getLocation().getWorld().spawnParticle(Particle.COMPOSTER, cl, 1, 0, 0, 0,0);
                        liver = (nearestEntity(cl, 10, List.of(ar, event.getEntity())) instanceof LivingEntity e ? e : null);
                        liver = (nearestEntity(cl.add(cl.getDirection().multiply(m)), 5, List.of(ar, event.getEntity())) instanceof LivingEntity e ? e : liver);
                        ar.getLocation().getWorld().spawnParticle(Particle.FLAME, cl, 1, 0, 0, 0,0);
                        if (ar.isOnGround()) cancel();
                        if (liver != null && !liver.isDead()) {
                            if (ar.getLocation().distance(liver.getLocation()) < 1) cancel();
                            Vector direction = liver.getLocation().toVector().subtract(ar.getLocation().toVector()).normalize();
                            ar.setVelocity(ar.getVelocity().add(direction.multiply(Math.cbrt(m*((Math.pow(ar.getLocation().distance(liver.getLocation())/4, 2)/10)+0.2)))));
                        }
                    }
                }.runTaskTimer(plugin, 0, 1);
            }
        }

        @EventHandler
        public void onInteract(@NotNull PlayerInteractEvent event) {
            Location loc = event.getPlayer().getEyeLocation().clone();
            for (int it = 0; it < 6; it++) {
                loc.add(loc.getDirection().multiply(0.5));
                Entity e = nearestEntity(loc, 0.15);
                if (e instanceof Item i) {
                    if (selItms.contains(i)) {
                        e.setGlowing(false);
                        selItms.remove(i);
                    } else {
                        e.setGlowing(true);
                        selItms.add(i);
                    }
                    event.setCancelled(true);
                    break;
                }
            }
            Player p = event.getPlayer();
            if (event.getAction() == Action.LEFT_CLICK_AIR  && p.getInventory().getItemInMainHand().getType() == Material.AIR) {
//                Location l = p.getEyeLocation().clone().subtract(0, 0.5, 0);
//                for (int i = 0; i < 12; i++) {
//                    Entity e = nearestEntity(l.add(l.getDirection().multiply(1)), 0.5);
////                    if (e != p && e instanceof LivingEntity liv) {
////                        BoundingBox b = liv.getBoundingBox();
////                        log((b.getMinX() - b.getMaxX()));
////                        log(new Location(liv.getWorld(), b.getMinX() +  ((b.getMinX() - b.getMaxX())/10) , b.getMinY() + ((b.getMinY() - b.getMaxY())/10) , b.getMinZ() + ((b.getMinZ() - b.getMaxZ())/10)));
////                        for (int j = -2; j < 12; j++) {
////                            Location locer = new Location(liv.getWorld(),
////                                    b.getMinX() - (((b.getMinX() - b.getMaxX())/10) * j),
////                                    b.getMinY() - (((b.getMinY() - b.getMaxY())/10) * j),
////                                    b.getMinZ() - (((b.getMinZ() - b.getMaxZ())/10) * j));
////                            locer.getWorld().spawnParticle(Particle.FLAME, locer, 1, 0, 0, 0, 0);
////                        }
////                        liv.damage(2);
////                        liv.setFireTicks(20);
////                        break;
////                    }
//                    if (e != p && e instanceof LivingEntity liv) {
//                        liv.getWorld().spawnParticle(Particle.CLOUD, liv.getLocation(), 5, 0.2, 0.2, 0.2, 0.1);
//                        liv.damage(0.2);
//                        liv.setVelocity(liv.getVelocity().subtract(genVec(liv.getLocation(), p.getLocation())).normalize().multiply(0.7));
//                        break;
//                    }
//                }
                WeavePlayer w = getOrNew(p);
                log(w.getMain());
                if (w.getMain() != null) w.cast();
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && p.getInventory().getItemInMainHand().getType() == Material.AIR) {
//                org.bukkit.block.Block b = p.getTargetBlockExact(5);
//                if (b != null) b.getWorld().spawnParticle(Particle.CLOUD, b.getLocation().add(0.5, 1, 0.5), 3, 0.1, 0, 0.1, 0.04);
//                p.setVelocity(p.getVelocity().add(p.getEyeLocation().getDirection().multiply(-0.3d)));
            }
        }

        @EventHandler
        public void onEntityDmg(@NotNull EntityDamageEvent event) {
            Entity de = event.getEntity();
            if (de instanceof Player p) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    WeavePlayer w = WeavePlayer.getWeaver(p);
                    if (w != null && w.getElement() == Element.AIR) {
                        event.setCancelled(true);
                        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 0.2, 0), 3, 0.1, 0, 0.1, Math.sqrt(event.getDamage())/25);
                    }
                }
            }
            Entity ce = event.getDamageSource().getCausingEntity();
            if (ce != null) {
                EntityMultiplier em = EntityMultiplier.getOrNew(ce);
                event.setDamage(event.getDamage()*em.getDmg());
            }
        }
        @EventHandler
        public void onEntityDeath(@NotNull EntityDeathEvent event) {
            EntityMultiplier.instances.remove(EntityMultiplier.get(event.getEntity()));
        }

        @EventHandler
        public void placeBlock(@NotNull BlockPlaceEvent event) {
            if (Objects.requireNonNull(event.getItemInHand().getItemMeta()).hasCustomModelData()) {
                ItemStack i = event.getItemInHand();
                Location loc = event.getBlock().getLocation();
                if (isSim(CraftingCauldron.item, i)) {
                    new CraftingCauldron(loc);
                } else if (isSim(ForceField.item, i)) {
                    new ForceField(loc);
                } else if (isSim(SpellWeaver.item, i)) {
                    new SpellWeaver(loc);
                } else if (isSim(SpellAbsorbingBlock.item, i)) {
                    new SpellAbsorbingBlock(loc);
                } else if (isSim(WeavingTable.item, i)) {
                    new WeavingTable(loc);
                }
            }
        }
        @EventHandler
        public void breakBlock(@NotNull BlockBreakEvent event) {
            for (Block l : Block.blocks)
                if (isSimBlk(l.getBlock().getLocation(), event.getBlock().getLocation())) {
                    l.remove(event.getPlayer().getGameMode() != GameMode.CREATIVE);
                    return;
                }
        }
//        @EventHandler
//        public void onPlayerItemHeld(@NotNull PlayerItemHeldEvent event) {
//            Player player = event.getPlayer();
//            WeavePlayer weaver = getWeaver(player);
//            if (weaver != null) {
//                if (weaver.isWeaving()) {
//                    int nxt = event.getNewSlot();
//                    int move = (weaver.getWandSlot() - nxt > 0 ? 1 : -1);
//                }
//            }
//        }
        @EventHandler
        public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
            if (!event.hasItem()) return;
            ItemStack item = event.getItem();
            if (item == null) return;
            if (!item.hasItemMeta()) return;
            if (!Objects.requireNonNull(item.getItemMeta()).hasCustomModelData()) return;
            for (org.tmmi.items.Item i : org.tmmi.items.Item.items) {
                if (isSim(item, i)) {
                    i.onUse(event);
                }
            }
        }
        @EventHandler
        public void onItemPickup(@NotNull PlayerPickupItemEvent event) {
            ItemStack item = event.getItem().getItemStack();
            if (!item.hasItemMeta()) return;
            if (!Objects.requireNonNull(item.getItemMeta()).hasCustomModelData()) return;
            for (org.tmmi.items.Item i : org.tmmi.items.Item.items)
                if (isSim(item, i))
                    i.onPickup(event);
        }
        @EventHandler
        public void onItemPickup(@NotNull PlayerDropItemEvent event) {
            ItemStack item = event.getItemDrop().getItemStack();
            if (!item.hasItemMeta()) return;
            if (!Objects.requireNonNull(item.getItemMeta()).hasCustomModelData()) return;
            for (org.tmmi.items.Item i : org.tmmi.items.Item.items)
                if (isSim(item, i))
                    i.onDrop(event);
        }
        @EventHandler
        public void onBlockClick(@NotNull PlayerInteractEvent event) {
            if (event.getClickedBlock() != null) {
                for (InteractiveBlock b : InteractiveBlock.instances)
                    if (b.getLoc().getBlock().equals(event.getClickedBlock().getLocation().getBlock()))
                        b.onClick(event.getAction(), event.getPlayer(), event);
            }
        }
//        @EventHandler
//        public void onPickup(@NotNull PlayerPickupItemEvent event) {
//            this.activeUser = event.getPlayer();
//            this.handler = activeUser.getUniqueId();
//            this.setSlot(Main.getItemSlot(this.getItem(), event.getPlayer().getInventory()));
//        }
        @EventHandler
        public void invClick(@NotNull InventoryClickEvent event) {
            if (event.getClickedInventory() != null && (event.getClickedInventory().getType() == InventoryType.PLAYER || event.getClickedInventory().getType() == InventoryType.CHEST)) {
                ItemStack i = event.getClickedInventory().getItem(0);
                ItemStack ci = event.getCurrentItem();
                if (ci != null && ci.hasItemMeta() && ci.getItemMeta().hasCustomModelData()
                        && ci.getItemMeta().getCustomModelData() == unclickable) event.setCancelled(true);
                if (allItemInv.contains(event.getClickedInventory())) {
                    if (ci != null) event.getWhoClicked().getInventory().addItem(ci);
                    event.setCancelled(true);
                } else {
                    WeavePlayer weaver = getWeaver(event.getWhoClicked());
                    if (weaver != null && isSim(event.getClickedInventory(), weaver.inventory())) {
                        event.setCancelled(true);
                        boolean us = event.getClick() == ClickType.LEFT;
                        ItemStack mi = us ? (weaver.getMain() == null ? null : weaver.getMain().toItem()) :
                                (weaver.getSecondary() == null ? null : weaver.getSecondary().toItem());
                        for (Spell s : weaver.getSpells()) {
                            if (isSim(ci, mi)) {
                                if (us) weaver.setMain(null);
                                else weaver.setSecondary(null);
                            } else if (isSim(ci, s.toItem())) {
                                if (mi != null)
                                    for (ItemStack itemStack : event.getClickedInventory().getContents())
                                        if (isSim(itemStack, mi)) {
                                            Objects.requireNonNull(ci.getItemMeta()).setEnchantmentGlintOverride(false);
                                            break;
                                        }
                                if (us) weaver.setMain(s);
                                else weaver.setSecondary(s);
                                Objects.requireNonNull(ci.getItemMeta()).setEnchantmentGlintOverride(true);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void reload() {
        onDisable();
        Block.blocks.clear();
        InteractiveBlock.instances.clear();
        Spell.spells.clear();
        CastSpell.instances.clear();
        WeavePlayer.weavers.clear();
        org.tmmi.items.Item.items.clear();
        DISABLED = false;
        onEnable();
    }
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        if (ENABLED.v() && !DISABLED) {
            DISABLED = true;
            for (Thread t : threads) t.interrupt();
            fm.saveData();
            for (SpellAbsorbingBlock s : SpellAbsorbingBlock.instances)
                if (s.getMainThread() != null) s.getMainThread().interrupt();
            for (WeavingTable w : WeavingTable.instances)
                w.onBreak();
        }
    }
}



