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

import static org.hetils.Item.newItemStack;
import static org.hetils.Util.isSim;

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
    public static @Nullable Element getElement(@NotNull String element) {
        return switch (element) {
            case "FIRE" -> FIRE;
            case "EARTH" -> EARTH;
            case "WATER" -> WATER;
            case "AIR" -> AIR;
            default -> null;
        };
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
    public static @Nullable ItemStack getItem(Element element) {
        return switch (element) {
            case AIR -> AIR_ITEM;
            case EARTH -> EARTH_ITEM;
            case WATER -> WATER_ITEM;
            case FIRE -> FIRE_ITEM;
            case null -> null;
        };
    }

    @Contract(pure = true)
    public static @Nullable List<Biome> getOptimalBiomes(Element element) {
        return switch (element) {
            case AIR -> AIR_BIOMES;
            case EARTH -> EARTH_BIOMES;
            case WATER -> WATER_BIOMES;
            case FIRE -> FIRE_BIOMES;
            case null -> null;
        };
    }
}

