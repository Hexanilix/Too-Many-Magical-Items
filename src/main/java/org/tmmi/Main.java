package org.tmmi;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.block.data.Levelled;
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
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.hetils.FileVersion;
import org.hetils.Property;
import org.jetbrains.annotations.NotNull;
import org.tmmi.spells.*;
import org.tmmi.spells.atributes.AreaEffect;
import org.tmmi.block.*;
import org.tmmi.items.SpellBook;

import java.util.*;
import java.util.logging.Level;

import static org.hetils.Util.*;
import static org.tmmi.Element.getItem;
import static org.tmmi.WeavePlayer.getOrNew;
import static org.tmmi.WeavePlayer.getWeaver;

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

    public static Thread autosave;

    public static Map<UUID, Boolean> spellPerms = new HashMap<>();
    public static List<Inventory> allItemInv = List.of(Bukkit.createInventory(null, 54, "pg1"));

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
     * A shortcut to the Bukkit console logger
     *
     * @param msg The message
     */
    public static void log(Object msg) {
        log(Level.INFO, msg);
    }
    /**
     * A shortcut to the Bukkit console logger
     *
     * @param msg The message
     * @param lv The level of the message
     */
    public static void log(Level lv, Object msg) {
        Bukkit.getLogger().log(lv, "[TMMI] " + msg);
    }

    public static Collection<Thread> threads = new ArrayList<>();

    /**
     * Adds a thread to a list within the TMMI plugin
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
            plugin = this;
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

    /**
     * A shortcut to the Bukkit console logger
     *
     * @param caul The cauldron
     * @param level The water level ranging from an int 0 to 4, skips on any other value
     */
    public static void setCauldronFillLevel(org.bukkit.block.@NotNull Block caul, int level) {
        if (caul.getType() == Material.CAULDRON || caul.getType() == Material.WATER_CAULDRON)
            if (level > -1 && level < 4) {
                if (level == 0) caul.setType(Material.CAULDRON);
                else {
                    if (caul.getType() != Material.WATER_CAULDRON) caul.setType(Material.WATER_CAULDRON);
                    Levelled l = (Levelled) caul.getBlockData();
                    l.setLevel(level);
                    caul.setBlockData(l);
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

    public void reload() {
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



