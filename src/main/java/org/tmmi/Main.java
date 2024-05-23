package org.tmmi;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
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
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.*;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tmmi.spells.*;
import org.tmmi.spells.atributes.AreaEffect;
import org.tmmi.spells.atributes.Type;
import org.tmmi.block.*;
import org.tmmi.items.FocusWand;
import org.tmmi.items.ItemCommand;
import org.tmmi.items.SpellBook;
import org.tmmi.spells.atributes.Weight;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

import static org.tmmi.Element.getElement;
import static org.tmmi.Element.getItem;
import static org.tmmi.WeavePlayer.getOrNew;
import static org.tmmi.WeavePlayer.getWeaver;
import static org.tmmi.block.Presence.*;
import static org.tmmi.Property.*;

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

    public static String DTFL;
    public static String CONF_FILE;
    public static String BLOCK_DATAFILE;
    public static String PLAYER_DATA;
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

    @Override
    public void onEnable() {
        Block.blocks = new HashSet<>();
        InteractiveBlock.instances = new HashSet<>();
        Spell.spells = new HashSet<>();
        CastSpell.instances = new HashSet<>();
        WeavePlayer.weavers = new HashSet<>();
        org.tmmi.items.Item.items = new HashSet<>();
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
            CONF_FILE = DTFL + "config.yml";
            if (!Files.exists(Path.of(CONF_FILE))) {
                try {
                    Files.createFile(Path.of(CONF_FILE));
                    log(Level.WARNING, "Created new config file since it was absent");
                } catch (IOException e) {
                    super.onDisable();
                    log(Level.SEVERE, "Could not create config file at '" + CONF_FILE + "'\nLog:\n" + String.join(Arrays.asList(e.getStackTrace()).toString()) + "\n");
                    return;
                }
                try {
                    FileWriter writer = new FileWriter(CONF_FILE);
                    for (Property p : Property.properties) writer.append(p.p()).append(": ").append(p.toString()).append("\n");
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    super.onDisable();
                }
            }
            //Reading yml
                int conf = loadConfig();
                int v = FileVersion.versionDiff(Property.FILE_VERSION.v(), FILES_VERSION);
                if (v != 0) {
                    if (v < 0) {
                        log(Level.WARNING, "config.yml is obsolete, updating contents of config.yml");
                        updateConfig();
                    } else log(Level.SEVERE, "Plugin cannot read properly files higher than " + FILES_VERSION + ": config.yml file version is " + FILE_VERSION.v().toString() +". Plugin will use default values in case of a different naming scheme. Use [link] to convert to older file versions");
                }
                if (conf < Property.properties.size()) {
                    log(Level.WARNING, "Some config properties not found," +
                            "not found properties remain at default values. Updating config.yml");
                    updateConfig();
                }
            if (Property.ENABLED.v()) {
                Spell.mxS = SPELL_SPEED_CAP.v();
                Spell.mxT = SPELL_TRAVEL_CAP.v();
                Spell.mxD = SPELL_DAMAGE_CAP.v();
                if (checkFilesAndCreate()) {
                    if (loadClasses()) {
                        if (AUTOSAVE_FREQUENCY.v() < 10) {
                            AUTOSAVE_FREQUENCY.setV(10);
                            updateConfig();
                        }
                        startAutosave();
                        loadBlockData();
                        background = newItemStack(Material.BLACK_STAINED_GLASS_PANE, " ", unclickable);
                        setItems();
                        Bukkit.getPluginManager().registerEvents(new MainListener(), this);

                        Objects.requireNonNull(Bukkit.getPluginCommand("tmmi")).setExecutor(new cmd());
                        Objects.requireNonNull(Bukkit.getPluginCommand("tmmi")).setTabCompleter(new cmd.cmdTabCom());
                        log("Plugin loaded successfully");
                    }
                }
            } else log(Level.WARNING, "Plugin is soft disabled in config, make sure this is a change you wanted");
        }
    }

    private int loadConfig() {
        try (InputStream input = new FileInputStream(CONF_FILE)) {
            HashMap<String, Object> l = new Yaml().load(input);
            if (l == null) return -1;
            if (l.isEmpty()) return 0;
            for (Map.Entry<String, Object> s : l.entrySet())
                for (Property pr : Property.properties)
                    if (Objects.equals(pr.p(), s.getKey()))
                        switch (s.getKey()) {
                            case "FILE_VERSION" -> pr.setV(new FileVersion(String.valueOf(s.getValue())));
                            default -> pr.setV(s.getValue());
                        }
            return l.size();
        }  catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void updateConfig() {
        FileVersion old = Property.FILE_VERSION.v();
        try (InputStream input = new FileInputStream(CONF_FILE)) {
            Property.FILE_VERSION.setV(FILES_VERSION);
            FileWriter writer = new FileWriter(CONF_FILE);
            writer.append("# Last automatic modification: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())).append("\n");
            HashMap<String, Object> l = new Yaml().load(input);
            if (l == null) l = new HashMap<>();
            for (Property pr : Property.properties)
                if (!l.containsKey(pr.p())) writer.append(pr.p()).append(": ").append(pr.toString()).append("\n");
            writer.close();
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
            Property.FILE_VERSION.setV(old);
            super.onDisable();
        }
    }

    private boolean savePlayerData(@NotNull Player p) {
        return savePlayerData(p.getUniqueId());
    }

    private boolean savePlayerData(@NotNull UUID id) {
        File file = new File(DTFL + "playerdata/" + id + ".json");
        try {
            if (!file.exists()) file.createNewFile();
            if (file.exists()) {
                FileWriter writer = new FileWriter(file);
                WeavePlayer w = getWeaver(id);
                String json = "{\n";
                if (w != null) {
                    List<String> spells = w.getSpells().stream().map(s -> s.toJson()).toList();
                    json += "\t\"element\": \"" + w.getElement() + "\",\n" +
                            "\t\"can_size\": " + w.getCanSize() + ",\n" +
                            "\t\"sor_size\": " + w.getSorSize() + ",\n" +
                            "\t\"spells\": [\n" +
                            String.join(",\n", spells.stream().map(s -> String.join("\n\t\t", s.split("\n"))).toList()) +
                            "\n\t],\n" +
                            "\t\"main\":\"" + (w.getMain() != null ? w.getMain().getId() : "null") + "\",\n" +
                            "\t\"second\":\"" + ( w.getSecondary() != null ?  w.getSecondary().getId() : "null") + "\"";
                }
                json += "\n}";
                writer.write(json); writer.close();
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private boolean autoSave() {
        boolean complete = true;
        for (Player p : Bukkit.getOnlinePlayers())
            if (!savePlayerData(p)) complete = false;
        complete = saveBlockData() && complete;
        return complete;
    }

    private void startAutosave() {
        if (AUTOSAVE.v()) {
            autosave = new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(AUTOSAVE_FREQUENCY.v() * 1000L);
                        autoSave();
                        if (GARBAGE_COLLECTION.v()) System.gc();
                        if (AUTOSAVE_MSG.v()) log(AUTOSAVE_MSG_VALUE.v());
                    }
                } catch (InterruptedException e) {
                    if (!DISABLED) log(Level.WARNING, "Autosave interrupted. Plugin data will not be saved until plugin disable and any new data acquired this point will be lost in case of a unprecedented stop");
                }
            });
            autosave.start();
        }
    }
    private void loadBlockData() {
        File file = new File(BLOCK_DATAFILE);
        if (file.exists()) {
            try {
                JSONObject json = new JSONObject(new String(Files.readAllBytes(file.toPath())));
                try {
                    if (!json.has("blocks")) return;
                    JSONArray ar = json.getJSONArray("blocks");
                    for (int i = 0; i < ar.length(); i++) {
                        JSONObject j = ar.getJSONObject(i);
                        try {
                            Block b;
                            org.tmmi.block.Type st = org.tmmi.block.Type.getType(j.getString("type"));
                            World w = Bukkit.getWorld(j.getString("world"));
                            if (w == null) {
                                log("Unknown world \"" + j.getString("world") + "\". Omitting");
                                continue;
                            }
                            Location loc = new Location(w,
                                    j.getDouble("x"),
                                    j.getDouble("y"),
                                    j.getDouble("z"));
                            if (st == null) {
                                log("Unknown block type \"" + Type.getType(j.getString("type")) + "\". Omitting");
                                continue;
                            }
                            switch (st) {
                                case CRAFTING_CAULDRON -> b = new CraftingCauldron(loc);
                                case FORCE_FIELD -> b = new ForceField(loc);
                                case SPELL_SUCKER -> new SpellAbsorbingBlock(loc);
                                case SPELL_WEAVER -> new SpellWeaver(loc);
//                                case PRESENCE_DETECTOR -> new Presence(loc);
                            }
                        } catch (JSONException ignore) {}
                    }
                } catch (JSONException ignore) {}
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log(Level.SEVERE, "Block data file doesn't exist and couldn't be created at " + file.getAbsolutePath() + '\n' + e);
            }
        }
    }
    private boolean saveBlockData() {
        File file = new File(BLOCK_DATAFILE);
        try {
            if (!file.exists()) file.createNewFile();
            if (file.exists()) {
                FileWriter writer = new FileWriter(file);
                String json = "{\n";
                List<String> blocks = new ArrayList<>();
                for (Block b : Block.blocks)
                    blocks.add(String.join("\n\t\t", b.toJSON().split("\n")));
                json += "\t\"blocks\": [\n" + String.join(",\n", blocks) + "\n\t]";
                json += "\n}";
                writer.write(json);
                writer.close();
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }
    private void loadPlayerSaveData(@NotNull Player p) {
        loadPlayerSaveData(p.getUniqueId());
    }
    private void loadPlayerSaveData(UUID id) {
        File folder = new File(PLAYER_DATA);
        File file = new File(PLAYER_DATA + "\\" + id);
        try {
            if (!folder.exists() || !file.exists()) {
                if (!folder.exists())
                    if (!folder.mkdir()) {
                        log(Level.SEVERE, "Player data folder doesn't exist and couldn't be created at " + file.getAbsolutePath() + "\n" +
                                "Player data won't be saved, it is advised to restart the plugin and check logs for any reading errors or" +
                                "insufficient permission levels for this plugin");
                        file.createNewFile();
                    }
            } else {
                JSONObject json = new JSONObject(new String(Files.readAllBytes(file.toPath())));
                try {
                    if (json.has("spells")) {
                        JSONArray ar = json.getJSONArray("spells");
                        int cs = json.getInt("can_size");
                        int ss = json.getInt("sor_size");
                        Element el = Element.getElement(json.getString("element"));
                        WeavePlayer w = new WeavePlayer(Bukkit.getPlayer(id), el, cs, ss);
                        for (int i = 0; i < ar.length(); i++) {
                            JSONObject j = ar.getJSONObject(i);
                            try {
                                Spell s;
                                Type st = Type.getType(j.getString("type"));
                                if (st == null) {
                                    log("Unknown spell type \"" + Type.getType(j.getString("type") + "\". Omitting"));
                                    continue;
                                }
                                switch (st) {
                                    case ATK -> s = new ATK(
                                            UUID.fromString(j.getString("id")),
                                            id,
                                            j.getString("name"),
                                            Weight.getSpellType(j.getString("weight")),
                                            j.getInt("level"),
                                            j.getInt("experience"),
                                            j.getInt("cast_cost"),
                                            getElement(j.getString("main_element")),
                                            getElement(j.getString("secondary_element")),
                                            AreaEffect.getAreaEffect(j.getString("area_effect")),
                                            j.getDouble("speed"),
                                            j.getDouble("travel"),
                                            j.getDouble("base_damage"));
                                    case DEF -> s = new DEF(
                                            UUID.fromString(j.getString("id")),
                                            id,
                                            j.getString("name"),
                                            Weight.getSpellType(j.getString("weight")),
                                            j.getInt("level"),
                                            j.getInt("experience"),
                                            j.getInt("cast_cost"),
                                            getElement(j.getString("element")),
                                            AreaEffect.getAreaEffect(j.getString("area_effect")),
                                            j.getInt("hold_time"),
                                            j.getInt("durability"));
                                    case STI -> s = new STI(
                                            STI.Stat.get(j.getString("stat")),
                                            UUID.fromString(j.getString("id")),
                                            id,
                                            j.getInt("level"),
                                            j.getInt("experience"),
                                            j.getInt("cast_cost"),
                                            j.getInt("effect_time"),
                                            j.getInt("multiplier"));
                                    default -> s = new UTL(
                                            UTL.Util.get(j.getString("util")),
                                            id,
                                            j.getInt("level"),
                                            j.getInt("experience"),
                                            j.getInt("cast_cost"));
                                }
                                w.addSpell(s);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Spell main = json.getString("main").equals("null") ? null : Spell.getSpell(UUID.fromString(json.getString("main")));
                        log(main);
                        Spell sec = json.getString("second").equals("null") ? null : Spell.getSpell(UUID.fromString(json.getString("second")));
                        if (main != null) w.setMain(main);
                        if (sec != null) w.setSecondary(sec);
                        Particle particle = null;
                        switch (el) {
                            case FIRE -> particle = Particle.FLAME;
                            case AIR -> particle = Particle.CLOUD;
                            case WATER -> particle = Particle.DRIPPING_WATER;
                            case EARTH -> particle = Particle.ANGRY_VILLAGER;
                        }
                        if (particle != null) {
                            Particle finalParticle = particle;
                            new BukkitRunnable() {
                                private final Particle part = finalParticle;

                                @Override
                                public void run() {
                                    Player p = Bukkit.getPlayer(id);
                                    if (p != null)
                                        p.getWorld().spawnParticle(part, p.getLocation().clone().add(0, 1, 0), 1, 0.2, 0.4, 0.2, 0.01);
                                }
                            }.runTaskTimer(plugin, 80, 80);
                        }
                    }
                } catch (JSONException ignore) {
                    log(Level.WARNING, "An error occurred while loading " + Bukkit.getPlayer(id).getName() + "'s player file located at " + file.toPath() + ". Omitting.");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    private class cmd implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (command.getName().equalsIgnoreCase("tmmi")) {
                if (!(sender instanceof ConsoleCommandSender)) {
                    Player player = (Player) sender;
                    if (args.length != 0) {
                        if (player.isOp()) {
                            switch (args[0].toLowerCase()) {
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
                                            Vector dir = p.getEyeLocation().getDirection().normalize();
                                            Location loc = p.getEyeLocation().clone().add(dir.multiply(2));
                                            loc.add(dir.multiply(Math.pow(loc.distance(bl), 2)/15));
                                            if (loc.distance(lc) > 0.1) {
                                                lc = loc;
                                                locs.add(loc);
                                                p.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
                                                count++;
                                            }
                                            if (count >= probe) {
                                                double[] vals = new double[probe];
                                                for (int i = 0; i < probe; i++) {
                                                    double t = (double) i / probe;
                                                    Location ploc = bl.clone().add(lc.clone().subtract(bl).multiply(t));
                                                    vals[i] = ploc.distance(locs.get(i));
                                                    p.getWorld().spawnParticle(Particle.COMPOSTER, ploc, 1, 0, 0, 0 ,0);
                                                }
                                                double avg = 0;
                                                for (double val : vals) avg += val;
                                                avg = avg/vals.length;
                                                p.sendMessage(String.valueOf(avg));
                                                cancel();
                                            }
                                        }
                                    }.runTaskTimer(plugin, 0, 0);
                                }
                                case "setperm" -> {
                                    if (args.length >= 2 && args.length < 4) {
                                        Player desPlayer = Bukkit.getPlayer(args[1]);
                                        if (desPlayer == null) {
                                            player.sendMessage(ChatColor.RED + "Unknown player " + ChatColor.ITALIC + args[1]);
                                        } else {
                                            permission.replace(desPlayer.getUniqueId(), !permission.get(desPlayer.getUniqueId()));
                                            player.sendMessage( ChatColor.LIGHT_PURPLE + "Set " + desPlayer.getName() + "'s permission to " + (permission.get(desPlayer.getUniqueId()) ? ChatColor.GREEN + "allow magic" : ChatColor.YELLOW + "forbid magic"));
                                            if (!(args.length == 3 && args[2].equalsIgnoreCase("hide"))) {
                                                desPlayer.sendMessage("You now" + (permission.get(desPlayer.getUniqueId()) ? ChatColor.GREEN + " have permission to use magic" : ChatColor.YELLOW + "You now don't have permission to use magic"));
                                            }
                                            desPlayer.recalculatePermissions();
                                        }
                                    } else if (args.length < 2) {
                                        permission.replace(player.getUniqueId(), !permission.get(player.getUniqueId()));
                                        player.sendMessage("You now" + (permission.get(player.getUniqueId()) ? ChatColor.GREEN + " have permission to use magic" : ChatColor.YELLOW + "You now don't have permission to use magic"));
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Too many arguments");
                                    }
                                }
                                case "items" -> {
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
                                    Inventory inv = player.getInventory();
                                    if (!inv.contains(guideBook())) {
                                        inv.addItem(guideBook());
                                    } else {
                                        player.sendMessage(ChatColor.GREEN + "You already have a guide in your inventory at slot " + (Arrays.asList(inv.getStorageContents()).indexOf(guideBook()) + 1) + "!");
                                    }
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
                                    getBlocksInSphere(player.getLocation(), 5).forEach(block -> block.setType(Material.STONE));

                                }
                                case "save" -> {
                                    player.sendMessage((autoSave() ? ChatColor.GREEN + "Saved plugin data" : ChatColor.RED + "An error occurred while saving data"));
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
                                case "reload" -> {
                                    player.sendMessage(ChatColor.YELLOW + "[TMMI] Reloading...");
                                    onDisable();
                                    onEnable();
                                    player.sendMessage(ChatColor.GREEN + "[TMMI] Reloaded!");
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
                                case "fm" -> {
                                    Objects.requireNonNull(player.getWorld()).spawnParticle(Particle.FLAME, player.getLocation().clone().add(0, 1, 0), 10, 0.21, 0.4, 0.21, 0);
                                }
                                case "box" -> {
                                    player.getBoundingBox().expand(1, 1, 1);
                                }
                                case "getwand" -> {
                                    WeavePlayer w = WeavePlayer.getWeaver(player);
                                    if (w == null) {
                                        w = new WeavePlayer(player);
//                                        Spell s = new Spell(player.getUniqueId(),"Yoink", Element.AIR, AreaEffect.DIRECT, 10);
//                                        w.getSpellInventory().setActiveSpells(SpellInventory.SpellUsage.MAIN, s);
                                    }
                                    player.getInventory().addItem(new FocusWand(player.getUniqueId()));
                                }
                                case "xp" -> {
                                    World w = player.getWorld();
                                    int x = player.getLocation().getBlockX();
                                    int y = player.getLocation().getBlockY();
                                    int z = player.getLocation().getBlockZ();
                                    int m = Integer.parseInt(args[1]);
                                    for (int i = x-m; i < x+ m; i++)
                                        for (int j = y-m; j < y + m; j++)
                                            for (int k = z-m; k < z + m; k++)
                                                w.spawnEntity(new Location(w, i, j, k), EntityType.EXPERIENCE_BOTTLE);
                                }
                                case "tnt" -> {
                                    World w = player.getWorld();
                                    int x = player.getLocation().getBlockX();
                                    int y = player.getLocation().getBlockY();
                                    int z = player.getLocation().getBlockZ();
                                    int m = Integer.parseInt(args[1]);
                                    for (int i = x-m; i < x+ m; i++)
                                        for (int j = y-m; j < y + m; j++)
                                            for (int k = z-m; k < z + m; k++)
                                                w.spawnEntity(player.getLocation(), EntityType.TNT_MINECART);
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
                                        if (e instanceof org.bukkit.entity.Item i) {
                                            ItemCommand.getOrNew(i).moveTo(new Location(player.getWorld(), Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3])), 0.5);
                                        }
                                    }
                                }
                                case "suck" -> {
                                    int s = Integer.parseInt(args[1]);
                                    Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(player.getEyeLocation(), s, s, s);
                                    for (Entity e : nearbyEntities) {
                                        if (e instanceof org.bukkit.entity.Item i) {
                                            ItemCommand.getOrNew(i).moveTo(player, 0.5);
                                        }
                                    }
                                }
                                case "drop" -> {
                                    getSphere(player.getLocation(), Integer.parseInt(args[1])).forEach(b -> {
                                        b.getWorld().dropItem(b.getLocation(), new ItemStack(b.getType()));
                                        b.setType(Material.AIR);
                                    });
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
                                                if (dest == null && e instanceof org.bukkit.entity.Item i && i.getItemStack().getType() == Material.PAPER) {
                                                    String[] s = Objects.requireNonNull(i.getItemStack().getItemMeta()).getDisplayName().split(" ");
                                                    try {
                                                        dest = new Location(pl.getWorld(), Double.parseDouble(s[1]), Double.parseDouble(s[2]), Double.parseDouble(s[3]));
                                                        i.remove();
                                                    } catch (NumberFormatException ignore) {}
                                                } else if (dest != null) {
                                                    if (e instanceof Player p) {
//                                                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 70, 10));
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
                                default -> player.sendMessage(ChatColor.RED + "Unknown argument " + ChatColor.ITALIC + args[0]);
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

        public static class cmdTabCom implements TabCompleter {
            @Nullable
            @Override
            public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
                List<String> tab = new ArrayList<>();
                if (command.getName().equalsIgnoreCase("tmmi")) {
                    if (!(sender instanceof ConsoleCommandSender)) {
                        if (sender.isOp()) {
                            if (args.length != 0)
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
                                        tab.add("setPerm");
                                        tab.add("items");
                                        tab.add("about");
                                        tab.add("find_block");
                                        tab.add("save");
                                        tab.add("reload");
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

    public static org.bukkit.util.@NotNull Vector genVec(@NotNull Location a, @NotNull Location b) {
        return genVec(a.toVector(), b.toVector());
    }
    public static org.bukkit.util.@NotNull Vector genVec(@NotNull Vector a, @NotNull Vector b) {
        double dX = a.getX() - b.getX();
        double dZ = a.getZ() - b.getZ();
        double yaw = Math.atan2(dZ, dX);
        double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), a.getY() - b.getY()) + Math.PI;
        double x = Math.sin(pitch) * Math.cos(yaw);
        double y = Math.sin(pitch) * Math.sin(yaw);
        double z = Math.cos(pitch);
        return new org.bukkit.util.Vector(x, z, y).normalize();
    }

    public static @NotNull List<org.bukkit.block.Block> getSphere(@NotNull Location center, int radius) {
        List<org.bukkit.block.Block> l = new ArrayList<>();
        for (double x = center.getX() - radius; x <= center.getX() + radius; x++)
            for (double y = center.getY() - radius; y <= center.getY() + radius; y++)
                for (double z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                    Location currentLocation = new Location(center.getWorld(), x, y, z);
                    if (center.distance(currentLocation) <= radius)
                        l.add(Objects.requireNonNull(center.getWorld()).getBlockAt(currentLocation));

                }
        return l;
    }
    public static Entity nearestEntity(@NotNull Location loc, double rad) {
        return nearestEntity(loc, rad, new ArrayList<>());
    }
    public static Entity nearestEntity(@NotNull Location loc, double rad, List<Entity> exlusions) {
        Collection<Entity> nearbyEntities = Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, rad, rad, rad);
        Entity n = null;
        if (!nearbyEntities.isEmpty()) {
            for (Entity e : nearbyEntities)
                if ((n == null || e.getLocation().distance(loc) < n.getLocation().distance(loc)) && !exlusions.contains(e))
                    n = e;
        }
        return n;
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

    private boolean checkFilesAndCreate() {
        PLAYER_DATA = DTFL + "playerdata/";
        File pdf = new File(PLAYER_DATA);
        if (!pdf.exists())
            if (!pdf.mkdir()) {
            log(Level.SEVERE, "Couldn't create player data folder at: "  + PLAYER_DATA);
            return false;
        }
        BLOCK_DATAFILE = DTFL + "blocks.json";
        List<String> files = new ArrayList<>(Arrays.asList(BLOCK_DATAFILE));
        for (String file : files) {
            Path path = Path.of(file);
            if (!Files.exists(path)) {
                try {
                    Files.createFile(path);
                    log(Level.WARNING, "Created new file at '" + path +"' since it was absent");
                } catch (IOException e) {
                    log(Level.SEVERE, "Could not create file at '" + path + "'\nLog:\n" + String.join(Arrays.asList(e.getStackTrace()).toString()) + "\n");
                    return false;
                }
            }
        }
        return true;
    }
    public static void setCusData(@NotNull ItemStack i, int data) {
        ItemMeta m = i.getItemMeta();
        assert m != null;
        m.setCustomModelData(data);
        i.setItemMeta(m);
    }

    private void setItems() {
        ItemStack fusionCrys = newItemStack(Material.END_CRYSTAL, ChatColor.DARK_AQUA + "Fusion Crystal", 365450, List.of());
        allItemInv.get(0).addItem(SpellWeaver.item, new SpellBook());
        for (Element e : Element.values()) allItemInv.get(0).addItem(getItem(e));
        for (AreaEffect e : AreaEffect.values()) allItemInv.get(0).addItem(AreaEffect.getItem(e));
//        {
//            NamespacedKey key = new NamespacedKey(this, "fusion_crystal");
//            ShapedRecipe crfCReci = new ShapedRecipe(key, fusionCrys);
//            crfCReci.shape(
//                    "SAS",
//                    "UAU",
//                    "SES");
//            crfCReci.setIngredient('A', Material.AIR);
//            crfCReci.setIngredient('E', Material.END_CRYSTAL);
//            crfCReci.setIngredient('U', Material.NETHERITE_SCRAP);
//            crfCReci.setIngredient('S', Material.IRON_INGOT);
//            Bukkit.getServer().addRecipe(crfCReci);
//            Permission permission = new Permission("tmmi.craft." + crfCReci.getKey().getKey(), "Fusion crystal perm");
//            Bukkit.getServer().getPluginManager().addPermission(permission);
//        }
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
    private static final List<org.bukkit.entity.Item> selItms = new ArrayList<>();

    private static double vecToNum(@NotNull Vector v) {
        return (((v.getX()+v.getY()+v.getZ())/3));
    }
    public class MainListener implements Listener {
        @EventHandler
        public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
            Player p = event.getPlayer();
            spellPerms.putIfAbsent(p.getUniqueId(), false);
            Main.this.loadPlayerSaveData(p);
        }

        @EventHandler
        public void onPlayerLeave(@NotNull PlayerQuitEvent event) {
            Main.this.savePlayerData(event.getPlayer());
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
                        double m = Math.pow(vecToNum(ar.getVelocity())*4, 2) / 4;
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
                Entity e = Main.nearestEntity(loc, 0.15);
                if (e instanceof org.bukkit.entity.Item i) {
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
                WeavePlayer w = WeavePlayer.getOrNew(p);
                if (w.getMain() != null) w.getMain().cast(event, p.getEyeLocation(), 1);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && p.getInventory().getItemInMainHand().getType() == Material.AIR) {
                org.bukkit.block.Block b = p.getTargetBlockExact(5);
                if (b != null) b.getWorld().spawnParticle(Particle.CLOUD, b.getLocation().add(0.5, 1, 0.5), 3, 0.1, 0, 0.1, 0.04);
                p.setVelocity(p.getVelocity().add(p.getEyeLocation().getDirection().multiply(-0.3d)));
            }
        }

        @EventHandler
        public void onEntityDmg(@NotNull org.bukkit.event.entity.EntityDamageEvent event) {
            Entity de = event.getEntity();
            if (de instanceof Player p) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setCancelled(true);
                    p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 0.2, 0), 3, 0.1, 0, 0.1, Math.sqrt(event.getDamage()));
                }
            }
            Entity ce = event.getDamageSource().getCausingEntity();
            if (ce != null) {
                if (de instanceof LivingEntity l) {
                    l.damage(event.getDamage()*EntityMultiplier.getOrNew(ce).getDmg());
                }
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
                }
            }
        }
        @EventHandler
        public void breakBlock(@NotNull BlockBreakEvent event) {
            for (Block l : Block.blocks) {
                if (isSimBlk(l.getBlock().getLocation(), event.getBlock().getLocation())) {
                    l.remove(event.getPlayer().getGameMode() != GameMode.CREATIVE);
                    break;
                }
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
//        @EventHandler
//        public void onPlayerRightClick(@NotNull PlayerMoveEvent event) {
//            log("From to:\n" + event.getFrom() + "\n" + event.getTo());
//        }
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
                    for (InteractiveBlock inter : InteractiveBlock.instances)
                        if (isSim(inter.getGui().getItem(0), i)) {
                            inter.onGUIClick(event.getAction(), ci, (Player) event.getWhoClicked(), event);
                            return;
                        }
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
    public static boolean isSim(Inventory i1, Inventory i2) {
        if (i1 == null || i2 == null) return false;
        if (i1 == i2) return true;
        if (i1.getSize() != i2.getSize()) return false;
        for (int i = 0; i < i1.getSize(); i++)
            if (!isSim(i1.getItem(i), i2.getItem(i))) return false;
        return true;
    }
    public static boolean isSim(ItemStack i1, ItemStack i2) {
        if (i1 == null || i2 == null) return false;
        if (i1 == i2) return true;
        return i1.hasItemMeta() &&
                i2.hasItemMeta() &&
                ((i1.getItemMeta().hasCustomModelData() && i2.getItemMeta().hasCustomModelData()) ?
                    i1.getItemMeta().getCustomModelData() == i2.getItemMeta().getCustomModelData() :
                        (i1.getItemMeta().hasCustomModelData() == i2.getItemMeta().hasCustomModelData())) &&
                i1.getType() == i2.getType();
    }
    static int getItemSlot(ItemStack item, @NotNull Inventory inv) {
        for (int i = 0; i < inv.getContents().length; i++) {
            if (inv.getItem(i) == item) {
                return i;
            }
        }
        return -1;
    }
    public static boolean isSimBlk(Location a, Location b) {
        return a == b || ((a != null && b != null) && (a.getWorld() == b.getWorld()
                && (int) a.getX() == (int) b.getX()
                && (int) a.getY() == (int) b.getY()
                && (int) a.getZ() == (int) b.getZ()));
    }
    public static boolean isSimBlk(org.bukkit.block.Block a, org.bukkit.block.Block b) {
        return a == b || ((a != null && b != null) && (a.getWorld() == b.getWorld()
                && a.getX() == b.getX()
                && a.getY() == b.getY()
                && a.getZ() == b.getZ()));
    }
    public static boolean inSphere(@NotNull Location center, int radius, @NotNull Location location) {
        int dx = center.getBlockX() - location.getBlockX();
        int dy = center.getBlockY() - location.getBlockY();
        int dz = center.getBlockZ() - location.getBlockZ();
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }
    public static final String digits = "0123456789abcdef";
    public static @NotNull String toHex(Object o, int size) {
        o = o != null ? o.toString() : null;
        if (o instanceof String s) {
            int m = 0;
            for (int i = 0; i < s.length(); i++) m += s.charAt(i);
            return IntToHex(m, size);
        } else {
            return "0".repeat(size);
        }
    }
    public static @NotNull String IntToHex(int i, int size) {
        StringBuilder hex = new StringBuilder();
        while (i > 0) {
            int digit = i % 16;
            hex.insert(0, digits.charAt(digit));
            i = i / 16;
        }
        return "0".repeat(Math.max(0, size - hex.length())) + hex;
    }
    @Override
    public void onDisable() {
        DISABLED = true;
        if (ENABLED.v()) {
            if (autosave != null) autosave.interrupt();
            autoSave();
            for (SpellAbsorbingBlock s : SpellAbsorbingBlock.SAblocks)
                if (s.getSpellGrabThread() != null) s.getSpellGrabThread().interrupt();
        }
        Block.blocks = null;
        InteractiveBlock.instances = null;
        Spell.spells = null;
        CastSpell.instances = null;
        WeavePlayer.weavers = null;
        org.tmmi.items.Item.items = null;
    }
}



