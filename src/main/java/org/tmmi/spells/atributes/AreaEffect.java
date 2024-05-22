package org.tmmi.spells.atributes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.Main;

import static org.tmmi.Main.isSim;

public enum AreaEffect {
    DIRECT,
    WIDE,
    AREA;
    public static final ItemStack DIRECT_ITEM = Main.newItemStack(Material.WRITABLE_BOOK, ChatColor.DARK_AQUA + "Direct", 824574);
    public static final ItemStack WIDE_ITEM = Main.newItemStack(Material.WRITABLE_BOOK, ChatColor.DARK_AQUA + "Wide", 824575);
    public static final ItemStack AREA_ITEM = Main.newItemStack(Material.WRITABLE_BOOK, ChatColor.DARK_AQUA + "Area", 824576);
    @Contract(pure = true)
    public static AreaEffect getAreaEffect(@NotNull String type) {
        AreaEffect c = null;
        switch (type) {
            case "DIRECT" -> c = DIRECT;
            case "WIDE_RANGE" -> c = WIDE;
            case "AREA_EFFECT" -> c = AREA;
        }
        return c;
    }
    @Contract(pure = true)
    public static @Nullable AreaEffect getAreaEffect(ItemStack item) {
        if (isSim(item, DIRECT_ITEM)) return DIRECT;
        else if (isSim(item, WIDE_ITEM)) return WIDE;
        else if (isSim(item, AREA_ITEM)) return AREA;
        return null;
    }
    public static @Nullable ItemStack getItem(AreaEffect effect) {
        if (effect == null) return null;
        switch (effect) {
            case DIRECT -> {
                return DIRECT_ITEM;
            }
            case AREA -> {
                return AREA_ITEM;
            }
            default -> {
                return WIDE_ITEM;
            }
        }
    }
}