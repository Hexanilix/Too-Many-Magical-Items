package org.tmmi;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tmmi.block.CrafttingCauldron;
import org.tmmi.block.ForceField;
import org.tmmi.block.SpellAbsorbingBlock;
import org.tmmi.block.SpellWeaver;
import org.tmmi.items.FocusWand;
import org.tmmi.items.Wand;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

import static org.tmmi.Spell.Element.getElement;
import static org.tmmi.Spell.spells;
import static org.tmmi.WeavePlayer.getWeaver;
import static org.tmmi.block.CrafttingCauldron.craftingCauldronLocations;
import static org.tmmi.block.Presence.*;
import static org.tmmi.block.Presence.detectorLocations;
import static org.tmmi.Properties.*;

public class Main extends JavaPlugin {
    static List<UUID> uuids = new ArrayList<>();
    public static Plugin plugin;
    public Set<Class<?>> classes = new HashSet<>();
    public static boolean DISABLED;

    public static String DTFL;
    public static String PROP_FILE;
    public static String PROP_VERSION = "1.0.0";
    public static String BLOCK_DATAFILE;
    public static String PLAYER_DATA;
    public static Map<String, Boolean> BoolProperties = new HashMap<>();
    public static Map<String, String> TextProperties = new HashMap<>();
    public static Map<String, Integer> NumProperties = new HashMap<>();

    public static Thread autosave;

    public static Map<UUID, SpellInventory> weavers = new HashMap<>();
    public static List<ItemStack> items;
    public static List<Inventory> allItemInv = new ArrayList<>();

    public static ItemStack background;
    private static final List<Integer> customItemSelectorDataList = new ArrayList<>();
    private static final Map<Player, Object> invToAdd = new HashMap<>();
    public static String permission;
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
            DTFL = this.getDataFolder().getAbsolutePath() + "\\";
            Path path = Path.of(DTFL);
            if (!Files.exists(path)) {
                if (new File(DTFL).mkdir()) {
                    log(Level.INFO, "Created data folder");
                } else {
                    super.onDisable();
                }
            }
            PROP_FILE = DTFL + "tmmi.properties";
            if (!Files.exists(Path.of(PROP_FILE))) {
                try {
                    Files.createFile(Path.of(PROP_FILE));
                    log(Level.WARNING, "Created new properties file since it was absent");
                } catch (IOException e) {
                    super.onDisable();
                    log(Level.SEVERE, "Could not create properties file at '" + PROP_FILE + "'\nLog:\n" + String.join(Arrays.asList(e.getStackTrace()).toString()) + "\n");
                    return;
                }
                List<Pair<Properties, String>> plist = List.of(
                        new Pair<>(COMMENT, "Last automatic modification: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())),
                        new Pair<>(FILEVERSION, PROP_VERSION),
                        new Pair<>(COMMENT, "Do not change the above values and names, this can cause issues and improper loading in the plugin"),
                        new Pair<>(COMMENT, "The following values are to be customised"),
                        new Pair<>(ENABLED, "true"),
                        new Pair<>(AUTOSAVE, "true"),
                        new Pair<>(AUTOSAVE_MSG, "true"),
                        new Pair<>(AUTOSAVE_FREQUENCY, "600"));
                try {
                    FileWriter writer = new FileWriter(PROP_FILE);
                    for (Pair<Properties, String> e : plist) writer.append(e.key().key()).append((e.key().equals(COMMENT) ? "" : "=")).append(e.value()).append("\n");
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    super.onDisable();
                }
            }
            readPropFile();
            if (boolProp(ENABLED)) {
                permission = "tmmi.craft." + new NamespacedKey(plugin, "weaver");
                checkFilesAndCreate();
                if (loadClasses()) {
                    startAutosave();
                    background = newItemStack(Material.BLACK_STAINED_GLASS_PANE, " ", 1010101102);
                    setItems();
                    Bukkit.getPluginManager().registerEvents(new MainListener(), this);

                    Objects.requireNonNull(Bukkit.getPluginCommand("tmmi")).setExecutor(new cmd());
                    Objects.requireNonNull(Bukkit.getPluginCommand("tmmi")).setTabCompleter(new cmd.cmdTabCom());

                    log("Plugin loaded successfully");
                }
            } else {
                log(Level.WARNING, "Plugin is disabled in properties, make sure this is a change you wanted");
            }
        }
    }
    private static void loadPlayerSaveData(@NotNull Player p) {
        loadPlayerSaveData(p.getUniqueId());
    }
    private static void loadPlayerSaveData(UUID id) {
        File folder = new File(PLAYER_DATA);
        if (folder.exists()) {
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().split("\\.")[0].equals(id.toString())) {
                        try {
                            JSONObject json = new JSONObject(new String(Files.readAllBytes(file.toPath())));
                            try {
                                JSONArray ar = json.getJSONArray("spells");
                                UUID pid = UUID.fromString(file.getName().split("\\.")[0]);
                                new WeavePlayer(Bukkit.getPlayer(pid));
                                for (int i = 0; i < ar.length(); i++) {
                                    JSONObject j = ar.getJSONObject(i);
                                    try {
                                        Spell s = new Spell(UUID.fromString(j.getString("id")),
                                                pid,
                                                j.getString("name"),
                                                j.getInt("level"),
                                                j.getInt("cast_cost"),
                                                getElement(j.getString("main_element")),
                                                getElement(j.getString("secondary_element")),
                                                Spell.CastAreaEffect.getAreaEffect(j.getString("cast_area_effect")),
                                                Spell.SpellType.getSpellType(j.getString("spell_type")),
                                                j.getDouble("base_damage"),
                                                j.getDouble("speed"), j.getDouble("travel"));
                                        log(s.toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException ignore) {}
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            }
        } else {
            log(Level.SEVERE, "Player data folder doesn't exist and couldn't be created at " + folder.getAbsolutePath() + "\n"+
                    "Player data won't be saved, it is advised to restart the plugin and check logs for any reading errors or" +
                    "insufficient permission levels for this plugin");
        }
    }
    private static void savePlayerData(@NotNull Player p) {
        savePlayerData(p.getUniqueId());
    }
    private static void savePlayerData(@NotNull UUID id) {
        File file = new File(DTFL + "playerdata/" + id + ".json");
        try {
            if (!file.exists()) file.createNewFile();
            if (file.exists()) {
                FileWriter writer = new FileWriter(file);
                WeavePlayer w = getWeaver(id);
                String json = "{\n";
                if (w != null) {
                    Spell main = w.getSpellInventory().getMainSpell();
                    Spell sec = w.getSpellInventory().getSecondarySpell();
                    json += "\t\"spells\":[\n" +
                                String.join("\n\t\t",String.join(",\n", w.getSpells().stream().map(Spell::toJson).toList()).split("\n")) +
                                "\n\t],\n" +
                                "\t\"main\":\"" + (main != null ? main.getId() : "null") + "\",\n" +
                                "\t\"second\":\"" + (sec != null ? sec.getId() : "null") + "\"";
                }
                json += "\n}";
                writer.write(json); writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startAutosave() {
        if (boolProp(AUTOSAVE)) {
            autosave = new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep((long) (intProp(AUTOSAVE_FREQUENCY) * 1000));
                        for (Player p : Bukkit.getOnlinePlayers())
                            savePlayerData(p);
                        if (boolProp(AUTOSAVE_MSG)) log("Autosaving...");
                    }
                } catch (InterruptedException e) {
                    if (DISABLED) log(Level.WARNING, "Autosave interrupted. Plugin data will not be saved until plugin disable and any new data acquired this point will be lost in case of a unprecedented stop");
                }
            });
            autosave.start();
        }
    }

    private float intProp(@NotNull Properties key) {
        return NumProperties.getOrDefault(key.key(), -1);
    }

    private boolean boolProp(@NotNull Properties key) {
        return BoolProperties.getOrDefault(key.key(), false);
    }

    private class cmd implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (command.getName().equalsIgnoreCase("tmmi")) {
                if (!(sender instanceof ConsoleCommandSender)) {
                    Player player = (Player) sender;
                    if (args.length != 0) {
                        if (player.isOp()) {
                            if (args[0].equalsIgnoreCase("setPerm")) {
                                if (args.length >= 2 && args.length < 4) {
                                    Player desPlayer = Bukkit.getPlayer(args[1]);
                                    if (desPlayer == null) {
                                        player.sendMessage(ChatColor.RED + "Unknown player " + ChatColor.ITALIC + args[1]);
                                    } else {
                                        PermissionAttachment attachment = desPlayer.addAttachment(Main.this);
                                        attachment.setPermission(permission, !desPlayer.hasPermission(permission));
                                        player.sendMessage((desPlayer.hasPermission(permission) ? ChatColor.GREEN : ChatColor.YELLOW) + "Set " + desPlayer.getDisplayName() + "'s permission to use magic to " + desPlayer.hasPermission(permission));
                                        if (!(args.length == 3 && args[2].equalsIgnoreCase("hide"))) {
                                            desPlayer.sendMessage((desPlayer.hasPermission(permission) ? ChatColor.GREEN : ChatColor.YELLOW) + "You now " + (desPlayer.hasPermission(permission) ? "have" : "don't have") + " permission to use magic");
                                        }
                                        desPlayer.recalculatePermissions();
                                    }
                                } else if (args.length < 2) {
                                    PermissionAttachment attachment = player.addAttachment(Main.this);
                                    attachment.setPermission(permission, !player.hasPermission(permission));
                                    player.sendMessage((player.hasPermission(permission) ? ChatColor.GREEN : ChatColor.YELLOW) + "You now " + (player.hasPermission(permission) ? "have" : "don't have") + " permission to use magic");
                                    player.recalculatePermissions();
                                } else {
                                    player.sendMessage(ChatColor.RED + "Too many arguments");
                                }
                            } else if (args[0].equalsIgnoreCase("items")) {
                                if (args.length >= 2) {
                                    if (args[1].equalsIgnoreCase("removeAllCraftingCauldrons")) {
                                        String confirm = ChatColor.RED + "You must confirm this action. To do so, add the 'confirm' argument at the end of the command.";
                                        if (args.length == 2) {
                                            player.sendMessage(confirm);
                                        } else if (args.length == 3) {
                                            if (args[2].equals("confirm")) {
                                                craftingCauldronLocations.clear();
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
                                                for (Location loc : detectorLocations) {
                                                    detectorName.remove(loc);
                                                    detectorSize.remove(loc);
                                                }
                                                detectorLocations.clear();
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
                            } else if (args[0].equalsIgnoreCase("getPlayerHead")) {
//                                if (args.length == 2) {
////                                    if (playerHeads.containsKey(Bukkit.getOfflinePlayer(args[1]).getUniqueId())) {
////                                        player.getInventory().addItem(playerHeads.get(Bukkit.getOfflinePlayer(args[1]).getUniqueId()));
////                                    } else {
//                                    ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
//                                    SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
//                                    assert skullMeta != null;
//                                    skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(args[1]));
//                                    skullMeta.setDisplayName(Objects.requireNonNull(Bukkit.getOfflinePlayer(args[1])).getName());
//                                    playerHead.setItemMeta(skullMeta);
//                                    player.getInventory().addItem(playerHead);
//                                    playerHeads.put(Objects.requireNonNull(Bukkit.getPlayer(args[1])).getUniqueId(), playerHead);
//                                    try {
//                                        Main.playerHead.save();
//                                    } catch (IOException e) {
//                                        throw new RuntimeException(e);
//                                    }
////                                    }
//                                } else {
//                                    player.sendMessage(ChatColor.RED + "Please select a player");
//                                }
                            } else if (args[0].equalsIgnoreCase("about")) {
                                player.spigot().sendMessage(about());
                            } else if (args[0].equalsIgnoreCase("guide")) {
                                Inventory inv = player.getInventory();
                                if (!inv.contains(guideBook())) {
                                    inv.addItem(guideBook());
                                } else {
                                    player.sendMessage(ChatColor.GREEN + "You already have a guide in your inventory at slot " + (Arrays.asList(inv.getStorageContents()).indexOf(guideBook()) + 1) + "!");
                                }
                            } else if (args[0].equalsIgnoreCase("find_block")) {
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
                                getBlocksInSphere(player.getLocation(), 5).forEach(block -> block.setType(Material.STONE));
                            } else if (args[0].equalsIgnoreCase("getWand")) {
                                WeavePlayer w = new WeavePlayer(player, new SpellInventory());
                                w.setWand(new FocusWand(player.getUniqueId()));
                                player.getInventory().addItem(w.getWand());
                                Spell s = new Spell(player.getUniqueId(),"Yoink", Spell.Element.AIR, Spell.CastAreaEffect.DIRECT, 10);
                                w.addSpell(s);
                                w.getSpellInventory().setActiveSpells(SpellInventory.SpellType.MAIN, s);
                            } else if (args[0].equalsIgnoreCase("spell")) {
                                WeavePlayer w = getWeaver(player);
                                assert w != null;
                                Spell s = null;
                                switch (args[1].toLowerCase()) {
                                    case "a" -> s = new Spell(player.getUniqueId(),"Yoink", Spell.Element.AIR, Spell.CastAreaEffect.DIRECT, 10);
                                    case "b" -> s = new Spell(player.getUniqueId(),"Yoink1", Spell.Element.FIRE, Spell.CastAreaEffect.DIRECT, 10);
                                    case "c" -> s = new Spell(player.getUniqueId(),"Yoink2", Spell.Element.WATER, Spell.CastAreaEffect.DIRECT, 10);
                                    case "d" -> s = new Spell(player.getUniqueId(),"Yoink3", Spell.Element.EARTH, Spell.CastAreaEffect.DIRECT, 10);
                                    case "e" -> s = new Spell(player.getUniqueId(),"whak", Spell.Element.FIRE, Spell.CastAreaEffect.WIDE, 10);
                                }
                                if (s != null) {
                                    w.addSpell(s);
                                    w.getSpellInventory().setActiveSpells(SpellInventory.SpellType.MAIN, s);
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Unknown argument " + ChatColor.ITALIC + args[0]);
                            }
                        } else {
                            if (args[0].equalsIgnoreCase("about")) {
                                player.spigot().sendMessage(about());
                            } else {
                                player.sendMessage(ChatColor.RED + "You don't have the required permissions to use this command!");
                            }
                        }
                    } else {
                        player.spigot().sendMessage(about());
                    }
                }
            } else {
               log(Level.WARNING, "This command cannot be executed within the console!");
            }
            return true;
        }

        @NotNull
        private static ItemStack getItemStack() {
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta bookMeta = (BookMeta) book.getItemMeta();
            assert bookMeta != null;
            bookMeta.setTitle("Ma diary");
            bookMeta.setAuthor("God");
            List<String> content = List.of("Hex", "is", "cool");
            bookMeta.setPages(content);
            book.setItemMeta(bookMeta);
            return book;
        }

        public static class cmdTabCom implements TabCompleter {
            @Nullable
            @Override
            public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
                List<String> tab = new ArrayList<>();
                if (command.getName().equalsIgnoreCase("tmmi")) {
                    if (!(sender instanceof ConsoleCommandSender)) {
                        Player player = (Player) sender;
                        if (player.isOp()) {
                            if (args.length != 0) {
                                switch (args[0]) {
                                    case "setPerm" -> {
                                        if (args.length == 3) {
                                            tab.add("hide");
                                        } else if (args.length == 2) {
                                            for (Player onPlay : Bukkit.getOnlinePlayers()) {
                                                tab.add(onPlay.getName());
                                            }
                                        }
                                    }
                                    case "recipes" -> tab.add("book");
                                    case "getPlayerHead" -> {
                                        for (Player onPlay : Bukkit.getOnlinePlayers()) {
                                            tab.add(onPlay.getName());
                                        }
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
                                    default -> {
                                        tab.add("recipes");
                                        tab.add("setPerm");
                                        tab.add("items");
                                        tab.add("getPlayerHead");
                                        tab.add("about");
                                        tab.add("find_block");
                                    }
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

    private void readPropFile() {
        try (InputStream input = new FileInputStream(PROP_FILE)) {
            java.util.Properties prop = new java.util.Properties();
            prop.load(input);
            for (String key : prop.stringPropertyNames()) {
                String val = prop.getProperty(key);
                if (Objects.equals(val, "true") || Objects.equals(val, "false")) {
                    BoolProperties.put(key, Boolean.parseBoolean(val));
                } else {
                    try {
                        NumProperties.put(key, Integer.parseInt(val));
                    } catch (NumberFormatException e) {
                        TextProperties.put(key, val);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            super.onDisable();
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

    private void checkFilesAndCreate() {
        PLAYER_DATA = DTFL + "playerdata/";
        File pdf = new File(PLAYER_DATA);
        if (!pdf.exists()) pdf.mkdir();
        BLOCK_DATAFILE = DTFL + "block.json";
        List<String> files = new ArrayList<>(Arrays.asList(BLOCK_DATAFILE));
        for (String file : files) {
            Path path = Path.of(file);
            if (!Files.exists(path)) {
                try {
                    Files.createFile(path);
                    log(Level.WARNING, "Created new file at '" + path +"' since it was absent");
                } catch (IOException e) {
                    log(Level.SEVERE, "Could not create file at '" + path + "'\nLog:\n" + String.join(Arrays.asList(e.getStackTrace()).toString()) + "\n");
                }
            }
        }
    }

    private void setItems() {
        allItemInv.add(Bukkit.createInventory(null, 54, "pg1"));

        CrafttingCauldron.item = newItem(Material.CAULDRON, ChatColor.LIGHT_PURPLE + "Crafting Cauldron", 200000);
        SpellAbsorbingBlock.item = newItem(Material.LODESTONE, ChatColor.GOLD + "Spell Condenser", 200001);
        SpellWeaver.item = newItem(Material.GOLD_BLOCK, ChatColor.DARK_AQUA + "MAGIC CARFTIN", 23461, List.of("A Crystal with the power", "of 1m brewing stands"));
        Spell.Element.FIRE_ITEM = newItem(Material.FIRE_CHARGE, ChatColor.DARK_AQUA + "Fire", 245723);
        Spell.Element.EARTH_ITEM = newItem(Material.GRASS_BLOCK, ChatColor.DARK_GREEN + "Earth", 245724);
        Spell.Element.WATER_ITEM = newItem(Material.WATER_BUCKET, ChatColor.DARK_GREEN + "Water", 245725);
        Spell.Element.AIR_ITEM = newItem(Material.FEATHER, ChatColor.WHITE + "Air", 245726);
        Spell.CastAreaEffect.DIRECT_ITEM = newItem(Material.WRITABLE_BOOK, ChatColor.DARK_AQUA + "Direct", 824574);
        Spell.CastAreaEffect.WIDE_ITEM = newItem(Material.WRITABLE_BOOK, ChatColor.DARK_AQUA + "Wide", 824575);
        Spell.CastAreaEffect.AREA_ITEM = newItem(Material.WRITABLE_BOOK, ChatColor.DARK_AQUA + "Area", 824576);
        Item fusionCrys = newItem(Material.END_CRYSTAL, ChatColor.DARK_AQUA + "Fusion Crystal", 365450, List.of());
        {
            NamespacedKey key = new NamespacedKey(this, "fusion_crystal");
            ShapedRecipe crfCReci = new ShapedRecipe(key, fusionCrys);
            crfCReci.shape(
                    "SAS",
                    "UAU",
                    "SES");
            crfCReci.setIngredient('A', Material.AIR);
            crfCReci.setIngredient('E', Material.END_CRYSTAL);
            crfCReci.setIngredient('U', Material.NETHERITE_SCRAP);
            crfCReci.setIngredient('S', Material.IRON_INGOT);
            Bukkit.getServer().addRecipe(crfCReci);
            Permission permission = new Permission("tmmi.craft." + crfCReci.getKey().getKey(), "Fusion crystal perm");
            Bukkit.getServer().getPluginManager().addPermission(permission);
        }
        {
            NamespacedKey key = new NamespacedKey(this, "crafting_cauldron");
            ShapedRecipe crfCReci = new ShapedRecipe(key, CrafttingCauldron.item);
            crfCReci.shape(
                    "ADA",
                    "ECE",
                    "AUA");
            crfCReci.setIngredient('A', Material.AIR);
            crfCReci.setIngredient('E', Material.ECHO_SHARD);
            crfCReci.setIngredient('U', Material.NETHERITE_SCRAP);
            crfCReci.setIngredient('C', new RecipeChoice.ExactChoice(fusionCrys));
            crfCReci.setIngredient('D', Material.DIAMOND);
            Bukkit.getServer().addRecipe(crfCReci);
            Permission permission = new Permission("tmmi.craft." + crfCReci.getKey().getKey(), "Crafting cauldron perm");
            Bukkit.getServer().getPluginManager().addPermission(permission);
        }
    }
    public static @NotNull Item newItem(Material mat, String name, int data) {
        return newItem(mat, name, data, new ArrayList<>());
    }
    public static @NotNull Item newItem(Material mat, String name, int data, List<String> lore) {
        Item i = new Item(mat);
        ItemMeta m = i.getItemMeta();
        assert m != null;
        m.setDisplayName(name);
        m.setCustomModelData(data);
        m.setLore(lore);
        i.setItemMeta(m);
        allItemInv.get(0).addItem(i);
        return i;
    }
    public static @NotNull ItemStack newItemStack(Material mat, String name) {
        return newItemStack(mat, name, 0, null);
    }
    public static @NotNull ItemStack newItemStack(Material mat, String name, int data) {
        return newItemStack(mat, name, data, null);
    }
    public static @NotNull ItemStack newItemStack(Material mat, String name, int data, List<String> lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        assert m != null;
        m.setDisplayName(name);
        if (data != 0) m.setCustomModelData(data);
        if (lore != null) m.setLore(lore);
        i.setItemMeta(m);
        return i;
    }

    public static class MainListener implements Listener {
        @EventHandler
        public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
            Player p = event.getPlayer();
            loadPlayerSaveData(p);
        }

        @EventHandler
        public void onPlayerLeave(@NotNull PlayerQuitEvent event) {
            savePlayerData(event.getPlayer());
        }

        @EventHandler
        public void placeBlock(@NotNull BlockPlaceEvent event) {
            if (Objects.requireNonNull(event.getItemInHand().getItemMeta()).hasCustomModelData()) {
                ItemStack i = event.getItemInHand();
                Location loc = event.getBlock().getLocation();
                if (isSim(CrafttingCauldron.item, i)) {
                    new CrafttingCauldron(loc);
                } else if (isSim(ForceField.item, i)) {
                    new ForceField(loc);
                } else if (isSim(SpellWeaver.item, i)) {
                    new SpellWeaver(loc);
                    log("placesc rafter");
                } else if (isSim(SpellAbsorbingBlock.item, i)) {
                    new SpellAbsorbingBlock(loc);
                }
            }
        }
        @EventHandler
        public void breakBlock(@NotNull BlockBreakEvent event) {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                return;
            }
            for (Block l : blocks) {
                if (l.getLoc().equals(event.getBlock().getLocation())) {
                    l.onBreak(event.getBlock().getLocation());
                }
            }
        }
        @EventHandler
        public void onPlayerItemHeld(@NotNull PlayerItemHeldEvent event) {
            Player player = event.getPlayer();
            WeavePlayer weaver = getWeaver(player);
            if (weaver != null) {
                if (weaver.isWeaving()) {
                    int nxt = event.getNewSlot();
                    int move = (weaver.getWandSlot() - nxt > 0 ? 1 : -1);
                }
            }
        }
        @EventHandler
        public static void onPlayerInteract(@NotNull PlayerInteractEvent event) {
            if (!event.hasItem()) return;
            ItemStack item = event.getItem();
            if (item == null) return;
            if (!item.hasItemMeta()) return;
            if (!Objects.requireNonNull(item.getItemMeta()).hasCustomModelData()) return;
            for (Item i : Item.items)
                if (isSim(item, i))
                    i.onUse(event);
            WeavePlayer weaver = getWeaver(event.getPlayer());
            if (weaver != null)
                if (item.equals(weaver.getWand()))
                    weaver.getWand().onUse(event);
        }
        @EventHandler
        public static void onItemPickup(@NotNull PlayerPickupItemEvent event) {
            ItemStack item = event.getItem().getItemStack();
            if (!item.hasItemMeta()) return;
            if (!Objects.requireNonNull(item.getItemMeta()).hasCustomModelData()) return;
            for (Item i : Item.items)
                if (isSim(item, i))
                    i.onPickup(event);
        }
        @EventHandler
        public static void onItemPickup(@NotNull PlayerDropItemEvent event) {
            ItemStack item = event.getItemDrop().getItemStack();
            if (!item.hasItemMeta()) return;
            if (!Objects.requireNonNull(item.getItemMeta()).hasCustomModelData()) return;
            for (Item i : Item.items)
                if (isSim(item, i))
                    i.onDrop(event);
        }
        @EventHandler
        public static void onBlockClick(@NotNull PlayerInteractEvent event) {
            if (event.getClickedBlock() != null) {
                for (InteractiveBlock b : interactiveBlock) {
                    if (b.getLoc().getBlock().equals(event.getClickedBlock().getLocation().getBlock())) {
                        b.onClick(event.getAction(), event.getPlayer(), event);
                    }
                }
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
            ItemStack i = event.getInventory().getItem(0);
            ItemStack ci = event.getCurrentItem();
            if (allItemInv.contains(event.getInventory())) {
                event.getWhoClicked().getInventory().addItem(ci);
                event.setCancelled(true);
            } else {
                for (InteractiveBlock inter : interactiveBlock) {
                    if (isSim(inter.getGui().getItem(0), i)) {
                        inter.onGUIClick(event.getAction(), ci, (Player) event.getWhoClicked(), event);
                    }
                }
            }
        }
    }
    public static @NotNull List<org.bukkit.block.Block> getBlocksInSphere(@NotNull Location center, int radius) {
        List<org.bukkit.block.Block> blocks = new ArrayList<>();
        int bX = center.getBlockX();
        int bY = center.getBlockY();
        int bZ = center.getBlockZ();
        for (int x = bX - radius; x <= bX + radius; x++) {
            for (int y = bY - radius; y <= bY + radius; y++) {
                for (int z = bZ - radius; z <= bZ + radius; z++) {
                    double distance = ((bX - x) * (bX - x) + ((bZ - z) * (bZ - z)) + ((bY - y) * (bY - y)));
                    if (distance < radius * radius) {
                        blocks.add(Objects.requireNonNull(center.getWorld()).getBlockAt(x, y, z));
                    }
                }
            }
        }
        return blocks;
    }
    public static boolean isSim(ItemStack i1, ItemStack i2) {
        return (i1 != null && i2 != null) && (i1 == i2 || ((i1.hasItemMeta() && i2.hasItemMeta()) &&
                (i1.getItemMeta().hasCustomModelData() && i2.getItemMeta().hasCustomModelData())
                && i1.getItemMeta().getCustomModelData() == i2.getItemMeta().getCustomModelData()
                && i1.getType() == i2.getType()));
    }
    static int getItemSlot(ItemStack item, @NotNull Inventory inv) {
        for (int i = 0; i < inv.getContents().length; i++) {
            if (inv.getItem(i) == item) {
                return i;
            }
        }
        return -1;
    }
    public static boolean inSphere(@NotNull Location center, int radius, @NotNull Location location) {
        int dx = center.getBlockX() - location.getBlockX();
        int dy = center.getBlockY() - location.getBlockY();
        int dz = center.getBlockZ() - location.getBlockZ();
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    public enum TMMIobject {
        SPELL,
        WAND,
        ITEM,
        BLOCK
    }
    static final String digits = "0123456789abcdef";
    public static @NotNull UUID newUUID(@NotNull TMMIobject type) {
        StringBuilder u = new StringBuilder("19a4bc21-000a-");
        switch (type) {
            case SPELL -> u.append("d049-").append(IntToHex(spells.size()));
            case WAND -> u.append("c213-").append(IntToHex(Wand.wands.size()));
            case ITEM -> u.append("e082-").append(IntToHex(Item.items.size()));
            case BLOCK -> u.append("8b3f-").append(IntToHex(Block.blocks.size()));
        }
        u.append('-');
        for (int i = 0; i < 8; i ++)
            u.append(digits.charAt(new Random().nextInt(16)));
        uuids.add(UUID.fromString(u.toString()));
        return UUID.fromString(u.toString());
    }
    public static @NotNull String IntToHex(int i) {
        StringBuilder hex = new StringBuilder();
        while (i > 0) {
            int digit = i % 16;
            hex.insert(0, digits.charAt(digit));
            i = i / 16;
        }
        return "0".repeat(Math.max(0, 4 - hex.length())) + hex;
    }
    @Override
    public void onDisable() {
        autosave.interrupt();
        for (Player p : Bukkit.getOnlinePlayers()) {
            savePlayerData(p);
        }
    }
}



