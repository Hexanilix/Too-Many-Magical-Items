package org.tmmi;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.hetils.Util.isSim;
import static org.hetils.Util.newItemStack;

public enum Element {
    FIRE,
    EARTH,
    WATER,
    AIR;
    public static final ItemStack FIRE_ITEM = newItemStack(Material.FIRE_CHARGE, ChatColor.DARK_AQUA + "Fire", 245723);
    public static final ItemStack EARTH_ITEM = newItemStack(Material.GRASS_BLOCK, ChatColor.DARK_GREEN + "Earth", 245724);
    public static final ItemStack WATER_ITEM = newItemStack(Material.WATER_BUCKET, ChatColor.DARK_GREEN + "Water", 245725);
    public static final ItemStack AIR_ITEM = newItemStack(Material.FEATHER, ChatColor.WHITE + "Air", 245726);
    public static final List<Biome> FIRE_BIOMES = new ArrayList<>();
    public static final List<Biome> EARTH_BIOMES = new ArrayList<>();
    public static final List<Biome> WATER_BIOMES = new ArrayList<>();
    public static final List<Biome> AIR_BIOMES = new ArrayList<>();
    @Contract(pure = true)
    public static Element getElement(@NotNull String element) {
        Element e = null;
        switch (element) {
            case "FIRE" -> e = FIRE;
            case "EARTH" -> e = EARTH;
            case "WATER" -> e = WATER;
            case "AIR" -> e = AIR;
        }
        return e;
    }
    @Contract(pure = true)
    public static @Nullable Element getElement(ItemStack item) {
        if (isSim(item, FIRE_ITEM)) return FIRE;
        else if (isSim(item, EARTH_ITEM)) return EARTH;
        else if (isSim(item, WATER_ITEM)) return WATER;
        else if (isSim(item, AIR_ITEM)) return AIR;
        return null;
    }
    @Contract(pure = true)
    public static ItemStack getItem(Element element) {
        ItemStack i = null;
        if (element != null)
            switch (element) {
                case AIR -> i = AIR_ITEM;
                case EARTH -> i = EARTH_ITEM;
                case WATER -> i = WATER_ITEM;
                default -> i = FIRE_ITEM;
            }
        return i;
    }

    @Contract(pure = true)
    public static List<Biome> getOptimalBiomes(Element element) {
        List<Biome> b = new ArrayList<>();
        if (element != null)
            switch (element) {
                case AIR -> b = AIR_BIOMES;
                case EARTH -> b = EARTH_BIOMES;
                case WATER -> b = WATER_BIOMES;
                default -> b = FIRE_BIOMES;
            }
        return b;
    }
}

