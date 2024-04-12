package org.tmmi;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.tmmi.events.PlayerBlockInteractEvent;

public abstract class InteractiveBlock extends Block {
    public InteractiveBlock(Material material, Location loc) {
        super(material, loc);
    }

    public abstract void onClick(Action action, Player player, PlayerBlockInteractEvent event);

    public abstract void onGUIClick(ItemStack item, Player player);
}
