package org.tmmi.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tmmi.InteractiveBlock;
import org.tmmi.events.PlayerBlockInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class SpellCrafter extends InteractiveBlock {
    public static Inventory gui;
    public static ItemStack item;
    public static List<CrafttingCauldron> cauldron = new ArrayList<>();

    public SpellCrafter(Location loc) {
        super(Material.ENCHANTING_TABLE, loc, gui);
    }

    @Override
    public void onClick(Action action, @NotNull Player player, PlayerBlockInteractEvent event) {
        player.openInventory(this.getGui());
    }

    @Override
    public void onGUIClick(InventoryAction action, ItemStack item, Player player) {
    }

    @Override
    public void onPlace(Location location) {

    }

    @Override
    public void onBreak(Location location) {

    }
}
