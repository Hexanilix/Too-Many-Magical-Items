package org.tmmi;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.hetils.FileVersion;
import org.hetils.Property;
import org.jetbrains.annotations.NotNull;
import org.tmmi.block.*;
import org.tmmi.events.Listeners;
import org.tmmi.events.listeners.BlockListener;
import org.tmmi.events.listeners.ItemListener;
import org.tmmi.events.listeners.PlayerListener;
import org.tmmi.item.items.SpellBook;
import org.tmmi.item.items.wands.coolstick;
import org.tmmi.item.items.potions.PortableGlacier;
import org.tmmi.item.items.potions.PotionListener;
import org.tmmi.spell.CastSpell;
import org.tmmi.spell.Spell;
import org.tmmi.spell.atributes.AreaEffect;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

import static org.hetils.General.toHex;
import static org.hetils.minecraft.Item.newItemStack;
import static org.tmmi.Element.getItem;

public class Main extends JavaPlugin {
    public static final String name = "tmmi";
    public Map<UUID, Boolean> permission = new HashMap<>();
    public static final FileVersion FILES_VERSION = new FileVersion(1,0,0);
    public static final FileVersion PLUGIN_VERSION = new FileVersion(1,0,0);
    public static String UUID_SEQUENCE = toHex(Main.class.getPackage().getSpecificationVersion(), 4)
            +toHex(PLUGIN_VERSION, 4)+'-';
    public static int unclickable = 2147837;
    public static Plugin plugin;
    public Collection<Class<?>> classes = new HashSet<>();
    public boolean DISABLED;

    public final Property<FileVersion> FILE_VERSION = new Property<>("FILE_VERSION", Main.FILES_VERSION);
    public final Property<Boolean> ENABLED = new Property<>("ENABLED", true);
    public final Property<Boolean> GARBAGE_COLLECTION = new Property<>("GARBAGE_COLLECTION", true);
    public final Property<Boolean> AUTOSAVE = new Property<>("AUTOSAVE", true);
    public final Property<Boolean> AUTOSAVE_MSG = new Property<>("AUTOSAVE_MSG", true);
    public final Property<String> AUTOSAVE_MSG_VALUE = new Property<>("AUTOSAVE_MSG_VALUE", "Autosaving...");
    public final Property<Integer> AUTOSAVE_FREQUENCY = new Property<>("AUTOSAVE_FREQUENCY", 1800);
    public final Property<Boolean> CUSTOM_SPELLS = new Property<>("CUSTOM_SPELLS", false);
    public final Property<List<UUID>> DISABLED_SPELLS = new Property<>("DISABLED_SPELLS", new ArrayList<>());
    public final Property<Double> SPELL_SPEED_CAP = new Property<>("SPELL_SPEED_CAP", 20d);
    public final Property<Double> SPELL_TRAVEL_CAP = new Property<>("SPELL_TRAVEL_CAP", 20d);
    public final Property<Double> SPELL_DAMAGE_CAP = new Property<>("SPELL_DAMAGE_CAP", 20d);
    public final Property<Integer> CHUNK_TREE_DEPTH = new Property<>("CHUNK_TREE_DEPTH", 3);
    public final Property<Boolean> LEGACY_STI_SPELL = new Property<>("LEGACY_STI_SPELL", false);
    public final Property<Boolean> CHECK_FOR_UPDATES = new Property<>("CHECK_FOR_UPDATES", true);
    public final Property<Boolean> AUTOMATIC_UPDATE = new Property<>("AUTOMATIC_UPDATE", false);
    public final Property<Integer> CHUNK_MANA_CAP = new Property<>("CHUNK_MANA_CAP", 2000);

    public final Property<Boolean> DEBUG = new Property<>("DEBUG_n_TEST", false);

    public Thread autosave;

    public Map<UUID, Boolean> spellPerms = new HashMap<>();
    public List<Inventory> allItemInv = List.of(Bukkit.createInventory(null, 54, "pg1"));

    public static ItemStack background;
    @NotNull ItemStack guideBook() {
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
    BaseComponent[] about() {
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

    /**
     * The TMMI plugin console logger. This method sends the string value of {@code msg} through the
     * Bukkit logger with the disclosure that the message was sent by this plugin, in the form of a
     * string in front of the message: <i>{@code [TMMI]}</i>
     *
     * @param msg The object converted to a string to send
     */
    public static void log(Object msg) {
        log(Level.INFO, msg);
    }
    /**
     * The TMMI plugin console logger. This method sends the string value of {@code msg} through the
     * Bukkit plugin with the disclosure that the message was sent by this plugin, in the form of a string
     * in front of the message: <i>{@code [TMMI]}</i>
     *
     * @param msg The object converted to a string to send
     * @param lv The level of the message
     */
    public static void log(Level lv, Object msg) {
        Bukkit.getLogger().log(lv, "[TMMI] " + msg);
    }

    public static Collection<Thread> threads = new ArrayList<>();

    /**
     * Creates a thread that's added to a {@link Collection} of threads that
     * is interrupted when disabling or reloading the plugin
     *
     * @param run The Runnable
     * @return returns a Thread, after adding it to the list
     */
    public static @NotNull Thread newThread(Runnable run) {
        Thread t = new Thread(run);
        threads.add(t);
        return t;
    }
    /**
     * Adds a thread to a list within the TMMI plugin
     *
     * @param t The Thread
     * @return returns the Thread, after adding it to the list
     */
    public static @NotNull Thread newThread(Thread t) {
        threads.add(t);
        return t;
    }

    /**
     * Adds a thread to a list in the TMMI plugin
     *
     * @param delay the amount
     * @param run The task to run after the specified delay
     */
    public static void runLater(int delay, Runnable run) {
        newThread(() -> {
            try {
                Thread.sleep(delay);
                run.run();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }).start();
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
            fm.createFiles();
            fm.loadConfig();
            Objects.requireNonNull(Bukkit.getPluginCommand("tmmi")).setExecutor(new cmd(this));
            Objects.requireNonNull(Bukkit.getPluginCommand("tmmi")).setTabCompleter(new cmd.cmdTabCom());
            if (ENABLED.v()) {
                MagicChunk.treeDepth = CHUNK_TREE_DEPTH.v();
                Spell.mxS = SPELL_SPEED_CAP.v();
                Spell.mxT = SPELL_TRAVEL_CAP.v();
                Spell.mxD = SPELL_DAMAGE_CAP.v();
                if (fm.checkFilesAndCreate()) {
                    if (loadClasses()) {
                        MagicChunk.chmc = CHUNK_MANA_CAP.v();
                        if (AUTOSAVE_FREQUENCY.v() < 10) {
                            AUTOSAVE_FREQUENCY.setV(10);
                            fm.updateConfig();
                        }
                        if (AUTOSAVE.v()) {
                            autosave = newThread(() -> {
                                try {
                                    while (!this.DISABLED) {
                                        Thread.sleep(AUTOSAVE_FREQUENCY.v() * 1000L);
                                        fm.saveData();
                                        if (GARBAGE_COLLECTION.v()) System.gc();
                                        if (AUTOSAVE_MSG.v()) log(AUTOSAVE_MSG_VALUE.v());
                                    }
                                } catch (InterruptedException e) {
                                    if (!DISABLED)
                                        log(
                                                Level.WARNING,
                                                "Autosave interrupted. Plugin data will not be saved until server/plugin stop/disable and any new data acquired up to that point will be lost in case of an unprecedented stop"
                                        );
                                }
                            });
                            autosave.start();
                        }
                        fm.loadBlockData();
                        background = newItemStack(Material.BLACK_STAINED_GLASS_PANE, " ", unclickable);
                        setItems();
                        listeners();
                        Spell.damageRunnable.runTaskTimer(plugin, 0, 10);
                        for (Player p : Bukkit.getOnlinePlayers()) fm.loadPlayerSaveData(p);
                        log("Plugin loaded successfully");
                        if (AUTOMATIC_UPDATE.v()) {
                            if (!CHECK_FOR_UPDATES.v()) CHECK_FOR_UPDATES.setV(true);
                            Updator.update();
                        } else if (CHECK_FOR_UPDATES.v()) {
                            if (Updator.checkForUpdates() == -1)
                                log("YOURE BEHIND");
                        }
                        if (Updator.lookForDuplicates() != -2) log(Level.WARNING,
                                "There is another version of TMMI located in your plugins folder!");
                    }
                }
            } else log(Level.WARNING, "Plugin is soft disabled in config, make sure this is a change you wanted");
        }
    }

    private void listeners() {
        Bukkit.getPluginManager().registerEvents(new BlockListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PotionListener(this), this);
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
        if (block.getType() == Material.CHISELED_BOOKSHELF &&
                switch (book.getType()) {
                    case ENCHANTED_BOOK, BOOK, WRITTEN_BOOK, WRITABLE_BOOK -> true;
                    default -> false;
        }) {
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
        allItemInv.getFirst().addItem(SpellWeaver.item, new SpellBook(), SpellAbsorbingBlock.item, WeavingTable.item, new coolstick(), new PortableGlacier(1));
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
    public void remove() {
        remove(true);
    }
    public void remove(boolean shutdown) {
        new Thread(() -> {
            try {
                // Search only in the specified directory (no subdirectories)
                for (Path path : Files.newDirectoryStream(Path.of("./"))) {
                    File f = path.toFile();
                    if (f.exists() && f.isFile()) {
                        String[] n = f.getName().split("\\.");
                        if (Objects.equals(n[n.length - 1], "jar")) {
                            for (String s : f.getName().split("-")) {
                                try {
                                    if (new org.hetils.FileVersion(false, s.replace("v", "").replace("P", "")).versionDiff(new FileVersion(1, 0, 0)) == 0) {
                                        f.delete();
                                        return;
                                    }
                                } catch (FileVersion.FileVersionFormatException ignore) {
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        if (shutdown) Bukkit.getServer().shutdown();
    }

    public void reload() {
        onDisable();
        Block.instances.clear();
        InteractiveBlock.instances.clear();
        Spell.spells.clear();
        CastSpell.instances.clear();
        WeavePlayer.weavers.clear();
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



