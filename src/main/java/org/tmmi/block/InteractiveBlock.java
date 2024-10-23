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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class InteractiveBlock extends Block {
    public static Collection<InteractiveBlock> instances = new HashSet<>();

    public InteractiveBlock(Material material, org.bukkit.block.Block loc, ItemStack item) {
        super(material, loc, item);
        instances.add(this);
    }
    public InteractiveBlock(Material material, Location loc, ItemStack item) {
        super(material, loc, item);
        instances.add(this);
    }
    public InteractiveBlock(Material material, org.bukkit.block.Block loc) {
        super(material, loc);
        instances.add(this);
    }

    public InteractiveBlock(Material material, Location loc) {
        super(material, loc);
        instances.add(this);
    }

    public void onClick(Action action, Player player, PlayerInteractEvent event) {

    }

    public void onGUIClick(InventoryAction action, ItemStack item, Player player, InventoryClickEvent event) {

    }
}
