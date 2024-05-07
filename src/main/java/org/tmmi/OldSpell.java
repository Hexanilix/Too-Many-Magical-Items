package org.tmmi;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.tmmi.Main.plugin;

public class OldSpell {
    private final Player handler;
    private final String name;
    private final int maxLevel;
    private int level;
    private final List<Integer> credCost;
    private final ItemStack displayItem;
    private final List<List<ItemStack>> recipe;
    private int cooldown;
    private final int cooldownTime;
    public OldSpell(String name, int maxLevel, int level, ItemStack displayItem, List<Integer> credCost, List<List<ItemStack>> recipe, int cooldown) {
        this(name, maxLevel, level, displayItem, credCost, recipe, cooldown, null);
    }
    public OldSpell(String name, int maxLevel, int level, ItemStack displayItem, List<Integer> credCost, List<List<ItemStack>> recipe, int cooldown, Player handler) {
        this.name = name;
        this.maxLevel = maxLevel;
        this.level = level;
        this.displayItem = displayItem;
        this.credCost = credCost;
        this.recipe = recipe;
        this.cooldownTime = cooldown;
        this.handler = handler;
    }
    public String getName() {
        return name;
    }
    public int getMaxLevel() {
        return maxLevel;
    }
    public int getLevel() {
        return level;
    }
    public ItemStack getDisplayItem() {
        return displayItem;
    }
    public int getCredCost(int lvl) {
        if (lvl <= 0 || lvl > maxLevel) {
            throw new IndexOutOfBoundsException("Level cannot exceed " + maxLevel + " nor be under 1");
        } else {
            return credCost.get(lvl - 1);
        }
    }
    public List<ItemStack> getRecipe(int lvl) {
        return (lvl <= 0 || lvl > maxLevel ? null : recipe.get(lvl - 1));
    }
    public void startCooldown() {
        this.cooldown = this.cooldownTime;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (cooldown == 0) {
                    cancel();
                }
                cooldown--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }
    public int getCooldown() {
        return cooldown;
    }
    public void setLevel(int lvl) {
        if (lvl > 0 || lvl < maxLevel) {
            level = lvl;
        }
    }
    private final List<Block> GlobalBlocksToRemove = new ArrayList<>();

    public void cast() {

    }
    public void activatdfhgdhdsfe(Player player) {
        switch (name) {
            case "Arrow" -> {
                int point = 0;
                for (int i = 0; i < Math.max(5, level * 2); i++) {
                    int num = 8 - (level - 1);
                    Arrow arrow = player.getWorld().spawnArrow(player.getEyeLocation(), player.getEyeLocation().getDirection(), Math.min(level, 4), Math.min(level, 4));
                    arrow.setColor(Color.AQUA);
                    arrow.setDamage((double) level / 2);
                    arrow.setBounce(false);
                    arrow.setKnockbackStrength(level / 4);
                    arrow.setShooter(null);
                    arrow.setMetadata("spell", new FixedMetadataValue(plugin, name));
                }
            }
            case "Cut" -> new BukkitRunnable() {
                int i = 0;
                final Location location = player.getEyeLocation();
                final Vector direction = player.getEyeLocation().getDirection();

                @Override
                public void run() {
                    i++;
                    location.add(direction);
                    Vector leftDirection = new Vector(-direction.getZ(), direction.getY(), direction.getX());
                    leftDirection.normalize();
                    for (int i = 0; i <= level; i++) {
                        Location loc = location.clone().add(leftDirection.clone().multiply((i > level / 2 ? -(i - level / 2) : i)));
                        if (i == 0) {
                            loc = location;
                        }
                        Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.ELECTRIC_SPARK, loc, 10, 0.2, 0.2, 0.2, 0);
                        Collection<Entity> collection = Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, 1, 1, 1);
                        if (!collection.isEmpty()) {
                            for (Entity e : collection) {
                                if (e.hasMetadata("spell")) {
                                    e.getWorld().spawnParticle(Particle.CLOUD, e.getLocation(), 10, 0.5, 0.5, 0.5);
                                    e.getWorld().playSound(e.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 1);
                                    e.remove();
                                }
                            }
                        }
                    }
                    if (i == level * 3) {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, 2);
            case "Fireball" -> {
                Fireball fireball = (Fireball) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.FIREBALL);
                fireball.setYield(level);
                fireball.setGlowing(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!fireball.isValid()) {
                            fireball.getWorld().spawnParticle(Particle.FLAME, fireball.getLocation(), 10, 1, 1, 1, 0, 0);
                        } else {
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0, 2);
                fireball.setVelocity(player.getEyeLocation().getDirection());
                fireball.setMetadata("spell", new FixedMetadataValue(plugin, name));
            }
            case "Flood" -> {
                new BukkitRunnable() {
                    int i = 0;
                    final int radius = level / 3;
                    final Location loc = player.getEyeLocation();
                    final Vector direction = player.getEyeLocation().getDirection();
                    final World world = player.getWorld();
                    final List<Block> blocksToRemove = new ArrayList<>();
                    final Map<Block, Material> blockMaterial = new HashMap<>();

                    @Override
                    public void run() {
                        i++;
                        loc.add(direction);
                        for (double x = loc.getX() - radius; x <= loc.getX() + radius; x++) {
                            for (double y = loc.getY() - radius; y <= loc.getY() + radius; y++) {
                                for (double z = loc.getZ() - radius; z <= loc.getZ() + radius; z++) {
                                    Location currentLocation = new Location(world, x, y, z);
                                    if (loc.distance(currentLocation) <= radius) {
                                        Block setingBlock = world.getBlockAt(currentLocation);
                                        if (!GlobalBlocksToRemove.contains(setingBlock)) {
                                            GlobalBlocksToRemove.add(setingBlock);
                                            blocksToRemove.add(setingBlock);
                                            blockMaterial.putIfAbsent(setingBlock, setingBlock.getType());
                                            setingBlock.setType(Material.WATER);
                                            List<Block> blockList = new ArrayList<>();
                                            blockList.add(world.getBlockAt(setingBlock.getLocation().clone().add(1, 0, 0)));
                                            blockList.add(world.getBlockAt(setingBlock.getLocation().clone().add(0, 1, 0)));
                                            blockList.add(world.getBlockAt(setingBlock.getLocation().clone().add(0, 0, 1)));
                                            blockList.add(world.getBlockAt(setingBlock.getLocation().clone().subtract(1, 0, 0)));
                                            blockList.add(world.getBlockAt(setingBlock.getLocation().clone().subtract(0, 1, 0)));
                                            blockList.add(world.getBlockAt(setingBlock.getLocation().clone().subtract(0, 0, 1)));
                                            for (Block ex : blockList) {
                                                if (ex.getType().isAir()) {
                                                    if (!GlobalBlocksToRemove.contains(ex)) {
                                                        GlobalBlocksToRemove.add(ex);
                                                        blocksToRemove.add(ex);
                                                        blockMaterial.putIfAbsent(ex, ex.getType());
                                                        ex.setType(Material.LIGHT);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (i == level * 5) {
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
                                }
                            }.runTaskTimer(plugin, 200, 0);
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0, 2);
            }
            case "Frost" -> {
//                public static @NotNull List<ItemStack> subSpells() {
//                    List<ItemStack> list = new ArrayList<>();
//                    {
//                        ItemStack item = new ItemStack(Material.SPRUCE_TRAPDOOR);
//                        ItemMeta itemMeta = item.getItemMeta();
//                        assert itemMeta != null;
//                        itemMeta.setDisplayName(ChatColor.DARK_GRAY + "Bridge");
//                        item.setItemMeta(itemMeta);
//                        list.add(item);
//                    }
//                    {
//                        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
//                        ItemMeta itemMeta = item.getItemMeta();
//                        assert itemMeta != null;
//                        itemMeta.setDisplayName(ChatColor.DARK_GRAY + "Shard");
//                        item.setItemMeta(itemMeta);
//                        list.add(item);
//                    }
//                    {
//                        ItemStack item = new ItemStack(Material.BRICK_WALL);
//                        ItemMeta itemMeta = item.getItemMeta();
//                        assert itemMeta != null;
//                        itemMeta.setDisplayName(ChatColor.DARK_GRAY + "Wall");
//                        item.setItemMeta(itemMeta);
//                        list.add(item);
//                    }
//                    return list;
//                }
            }
            case "Lightning" -> {
                Location eyeLocation = player.getEyeLocation();
                Location currentLocation = eyeLocation.clone();
                World world = player.getWorld();
                float points = 0;
                int maxDistance = 100;
                for (int distance = 0; distance < maxDistance; distance++) {
                    currentLocation.add(eyeLocation.getDirection());
                    Block block = world.getBlockAt(currentLocation);
                    LivingEntity hit = null;
                    Collection<Entity> list = world.getNearbyEntities(currentLocation, 0.2, 0.2, 0.2);
                    for (Entity ent : list) {
                        if (ent instanceof LivingEntity e && ent != player) {
                            hit = e;
                            break;
                        }
                    }
                    if (!block.getType().isAir() || hit != null) {
                        double x0;
                        double z0;
                        double y0;
                        if (hit != null) {
                            x0 = hit.getLocation().getX();
                            z0 = hit.getLocation().getZ();
                            y0 = hit.getLocation().getY();
                        } else {
                            x0 = block.getX();
                            z0 = block.getZ();
                            y0 = block.getY();
                        }
                        int radius = level * 2;
                        for (double x = x0 - radius; x <= x0 + radius; x++) {
                            for (double z = z0 - radius; z <= z0 + radius; z++) {
                                for (double y = y0 - 5; y <= y0 + 5; y++) {
                                    double distanceSquared = Math.pow(x - x0, 2) + Math.pow(z - z0, 2);
                                    if (distanceSquared <= radius * radius) {
                                        Location loc = new Location(block.getWorld(), x, y + 1, z);
                                        Collection<Entity> collection = world.getNearbyEntities(loc, 0.5, 0.5, 0.5);
                                        if (!collection.isEmpty()) {
                                            for (Entity e : collection) {
                                                if (e != player) {
                                                    if (e instanceof LivingEntity l) {
                                                        e.getWorld().spawnEntity(e.getLocation(), EntityType.LIGHTNING_BOLT);
                                                        world.playSound(loc, Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
                                                        l.damage(level);
                                                        points += 0.2F;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
            case "Thunder" -> {
                Location eyeLocation = player.getEyeLocation();
                Location currentLocation = eyeLocation.clone();
                World world = player.getWorld();
                int maxDistance = 100;
                for (int distance = 0; distance < maxDistance; distance++) {
                    currentLocation.add(eyeLocation.getDirection());
                    Block block = world.getBlockAt(currentLocation);
                    LivingEntity hit = null;
                    Collection<Entity> list = world.getNearbyEntities(currentLocation, 0.2, 0.2, 0.2);
                    for (Entity ent : list) {
                        if (ent instanceof LivingEntity e && ent != player) {
                            hit = e;
                            break;
                        }
                    }
                    if (!block.getType().isAir() || hit != null) {
                        if (hit != null) {
                            block = hit.getWorld().getBlockAt(hit.getLocation().clone().subtract(0, 1, 0));
                        }
                        Block finalBlock = block;
                        new BukkitRunnable() {
                            short i = 0;
                            short i2 = 0;

                            @Override
                            public void run() {
                                i++;
                                int Radius = level * 2;
                                for (int radI = 0; radI < Radius * 2; radI++) {
                                    double angle = radI * ((2 * Math.PI) / Radius);
                                    double x = finalBlock.getX() + Radius * Math.cos(angle + i);
                                    double z = finalBlock.getZ() + Radius * Math.sin(angle + i);
                                    Location particleLocation = new Location(world, x, finalBlock.getY() + 1, z);
                                    world.spawnParticle(Particle.ELECTRIC_SPARK, particleLocation, 1, 0, 0, 0, 0);
                                }
                                if (i == 40) {
                                    i = 0;
                                    i2++;
                                    int x0 = finalBlock.getX();
                                    int z0 = finalBlock.getZ();
                                    int y0 = finalBlock.getY();
                                    int radius = level * 2;
                                    for (int x = x0 - radius; x <= x0 + radius; x++) {
                                        for (int z = z0 - radius; z <= z0 + radius; z++) {
                                            for (int y = y0 - 5; y <= y0 + 5; y++) {
                                                double distanceSquared = Math.pow(x - x0, 2) + Math.pow(z - z0, 2);
                                                if (distanceSquared <= radius * radius) {
                                                    Location loc = new Location(world, x, y, z);
                                                    Collection<Entity> collection = world.getNearbyEntities(loc, 0.5, 0.5, 0.5);
                                                    if (!collection.isEmpty()) {
                                                        for (Entity e : collection) {
                                                            if (e instanceof LivingEntity l) {
                                                                l.getWorld().spawnEntity(l.getLocation(), EntityType.LIGHTNING_BOLT);
                                                                l.damage((double) level / 2);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (i2 == Radius) {
                                    cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 0, 0);
                        break;
                    }
                }
            }
            case "Fire" -> {
            }
            case "Air" -> {

            }
        }
    }
    @Contract("_, _ -> param1")
    public static @NotNull List<OldSpell> removeSpellsByName(List<OldSpell> origin, @NotNull List<OldSpell> subtractor) {
        List<String> subNames = new ArrayList<>();
        for (OldSpell spell : subtractor) {
            subNames.add(spell.getName());
        }
        List<OldSpell> removal = new ArrayList<>();
        for (OldSpell spell : origin) {
            if (subNames.contains(spell.getName())) {
                removal.add(spell);
            }
        }
        origin.removeAll(removal);
        return origin;
    }
    public static @NotNull List<ItemStack> getDisplayItems(@NotNull List<OldSpell> spells) {
        List<ItemStack> items = new ArrayList<>();
        for (OldSpell s : spells) {
            items.add(s.getDisplayItem());
        }
        return items;
    }
    public static OldSpell getSpellByName(@NotNull List<OldSpell> list, String name) {
        OldSpell spell = null;
        for (OldSpell s : list) {
            if (s.getName().equals(name)) {
                spell = s;
                break;
            }
        }
        return spell;
    }
    public static OldSpell getSpellByCusModelData(@NotNull List<OldSpell> list, int data) {
        OldSpell spell = null;
        for (OldSpell s : list) {
            if (Objects.requireNonNull(s.getDisplayItem().getItemMeta()).getCustomModelData() == data) {
                spell = s;
                break;
            }
        }
        return spell;
    }
    public static @NotNull List<Integer> getSpellCusModelData(@NotNull List<OldSpell> spellList) {
        List<Integer> list = new ArrayList<>();
        for (OldSpell s : spellList) {
            list.add(Objects.requireNonNull(s.getDisplayItem().getItemMeta()).getCustomModelData());
        }
        return list;
    }
    public static @NotNull List<String> getSpellNames(@NotNull List<OldSpell> spellList) {
        List<String> list = new ArrayList<>();
        for (OldSpell s : spellList) {
            list.add(ChatColor.stripColor(s.getName()).toLowerCase());
        }
        return list;
    }
    public static @NotNull List<OldSpell> allSpells() {
        List<OldSpell> spellList = new ArrayList<>();
        {
            String name = "Arrow";
            ItemStack itemStack = new ItemStack(Material.ARROW);
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(ChatColor.RED + name);
            itemMeta.setCustomModelData(306480);
            itemStack.setItemMeta(itemMeta);
            List<List<ItemStack>> list = new ArrayList<>(Arrays.asList(
                    List.of(new ItemStack(Material.FIRE_CHARGE, 4)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 8), new ItemStack(Material.FIRE_CHARGE, 2))));
            spellList.add(new OldSpell(name, 8, 0, itemStack, List.of(200, 40, 80, 160, 320, 640, 1280, 2560), list, 10));
        }
        {
            String name = "Cut";
            ItemStack itemStack = new ItemStack(Material.SHEARS);
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(ChatColor.DARK_PURPLE + name);
            itemMeta.setCustomModelData(306481);
            itemStack.setItemMeta(itemMeta);
            List<List<ItemStack>> list = new ArrayList<>(Arrays.asList(
                    List.of(new ItemStack(Material.FIRE_CHARGE, 4)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 8), new ItemStack(Material.FIRE_CHARGE, 2))));
            spellList.add(new OldSpell(name, 8, 0, itemStack, List.of(200, 40, 80, 160, 320, 640, 1280, 2560), list, 10));
        }
        {
            String name = "Fireball";
            ItemStack itemStack = new ItemStack(Material.FIRE_CHARGE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(ChatColor.RED + name);
            itemMeta.setCustomModelData(306482);
            itemStack.setItemMeta(itemMeta);
            List<List<ItemStack>> list = new ArrayList<>(Arrays.asList(
                    List.of(new ItemStack(Material.FIRE_CHARGE, 4)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 8), new ItemStack(Material.FIRE_CHARGE, 2))));
            spellList.add(new OldSpell(name, 8, 0, itemStack, List.of(200, 40, 80, 160, 320, 640, 1280, 2560), list, 10));
        }
        {
            String name = "Flood";
            ItemStack itemStack = new ItemStack(Material.WATER_BUCKET);
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(ChatColor.BLUE + name);
            itemMeta.setCustomModelData(306483);
            itemStack.setItemMeta(itemMeta);
            List<List<ItemStack>> list = new ArrayList<>(Arrays.asList(
                    List.of(new ItemStack(Material.FIRE_CHARGE, 4)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 8), new ItemStack(Material.FIRE_CHARGE, 2))));
            spellList.add(new OldSpell(name, 8, 0, itemStack, List.of(200, 40, 80, 160, 320, 640, 1280, 2560), list, 10));
        }
        {
            String name = "Frost";
            ItemStack itemStack = new ItemStack(Material.ICE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(ChatColor.BLUE + name);
            itemMeta.setCustomModelData(306484);
            itemStack.setItemMeta(itemMeta);
            List<List<ItemStack>> list = new ArrayList<>(Arrays.asList(
                    List.of(new ItemStack(Material.FIRE_CHARGE, 4)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 8), new ItemStack(Material.FIRE_CHARGE, 2))));
            spellList.add(new OldSpell(name, 8, 0, itemStack, List.of(200, 40, 80, 160, 320, 640, 1280, 2560), list, 10));
        }
        {
            String name = "Lightning";
            ItemStack itemStack = new ItemStack(Material.BLAZE_ROD);
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(ChatColor.YELLOW + name);
            itemMeta.setCustomModelData(306485);
            itemStack.setItemMeta(itemMeta);
            List<List<ItemStack>> list = new ArrayList<>(Arrays.asList(
                    List.of(new ItemStack(Material.FIRE_CHARGE, 4)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 8), new ItemStack(Material.FIRE_CHARGE, 2))));
            spellList.add(new OldSpell(name, 8, 0, itemStack, List.of(200, 40, 80, 160, 320, 640, 1280, 2560), list, 10));
        }
        {
            String name = "Thunder";
            ItemStack itemStack = new ItemStack(Material.LIGHTNING_ROD);
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(ChatColor.GOLD + name);
            itemMeta.setCustomModelData(306486);
            itemStack.setItemMeta(itemMeta);
            List<List<ItemStack>> list = new ArrayList<>(Arrays.asList(
                    List.of(new ItemStack(Material.FIRE_CHARGE, 4)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 8), new ItemStack(Material.FIRE_CHARGE, 2))));
            spellList.add(new OldSpell(name, 8, 0, itemStack, List.of(200, 40, 80, 160, 320, 640, 1280, 2560), list, 10));
        }
        {
            String name = "Fire";
            ItemStack itemStack = new ItemStack(Material.MAGMA_CREAM);
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(ChatColor.RED + name);
            itemMeta.setCustomModelData(306487);
            itemStack.setItemMeta(itemMeta);
            List<List<ItemStack>> list = new ArrayList<>(Arrays.asList(
                    List.of(new ItemStack(Material.FIRE_CHARGE, 4)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 8), new ItemStack(Material.FIRE_CHARGE, 2))));
            spellList.add(new OldSpell(name, 8, 0, itemStack, List.of(200, 40, 80, 160, 320, 640, 1280, 2560), list, 10));
        }
        {
            String name = "Air";
            ItemStack itemStack = new ItemStack(Material.FEATHER);
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(ChatColor.WHITE + name);
            itemMeta.setCustomModelData(306488);
            itemStack.setItemMeta(itemMeta);
            List<List<ItemStack>> list = new ArrayList<>(Arrays.asList(
                    List.of(new ItemStack(Material.FIRE_CHARGE, 4)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 2), new ItemStack(Material.FIRE_CHARGE, 1)),
                    List.of(new ItemStack(Material.BLAZE_ROD, 8), new ItemStack(Material.FIRE_CHARGE, 2))));
            spellList.add(new OldSpell(name, 8, 0, itemStack, List.of(200, 40, 80, 160, 320, 640, 1280, 2560), list, 10));
        }
        return spellList;
    }
}
