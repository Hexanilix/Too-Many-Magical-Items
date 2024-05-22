package org.tmmi.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class InteractiveBlock extends Block {
    public static List<InteractiveBlock> instances = new ArrayList<>();
    private final Inventory gui;

    public InteractiveBlock(Material material, Location loc, Inventory gui) {
        super(material, loc);
        this.gui = gui;
        instances.add(this);
    }

    public Inventory getGui() {
        return gui;
    }

    public abstract void onClick(Action action, Player player, PlayerInteractEvent event);

    public void onGUIClick(InventoryAction action, ItemStack item, Player player, InventoryClickEvent event) {

    }
}
