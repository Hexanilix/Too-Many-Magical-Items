package org.tmmi.spell.atributes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.hetils.minecraft.General.isSim;
import static org.hetils.minecraft.Item.newItemStack;

public enum AreaEffect {
    DIRECT,
    WIDE,
    AREA;
    public static final ItemStack DIRECT_ITEM = newItemStack(Material.WRITABLE_BOOK, ChatColor.DARK_AQUA + "Direct", 824574);
    public static final ItemStack WIDE_ITEM = newItemStack(Material.WRITABLE_BOOK, ChatColor.DARK_AQUA + "Wide", 824575);
    public static final ItemStack AREA_ITEM = newItemStack(Material.WRITABLE_BOOK, ChatColor.DARK_AQUA + "Area", 824576);
    @Contract(pure = true)
    public static @Nullable AreaEffect getAreaEffect(@NotNull String type) {
        return switch (type) {
            case "DIRECT" -> DIRECT;
            case "WIDE" -> WIDE;
            case "AREA" -> AREA;
            default -> null;
        };
    }
    @Contract(pure = true)
    public static @Nullable AreaEffect getAreaEffect(ItemStack item) {
        if (isSim(item, DIRECT_ITEM)) return DIRECT;
        else if (isSim(item, WIDE_ITEM)) return WIDE;
        else if (isSim(item, AREA_ITEM)) return AREA;
        return null;
    }
    public static @Nullable ItemStack getItem(AreaEffect effect) {
        return switch (effect) {
            case DIRECT -> DIRECT_ITEM;
            case AREA -> AREA_ITEM;
            case WIDE -> WIDE_ITEM;
            case null -> null;
        };
    }
}