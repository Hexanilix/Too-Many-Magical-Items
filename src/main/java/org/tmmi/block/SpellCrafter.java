package org.tmmi.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.tmmi.InteractiveBlock;
import org.tmmi.events.PlayerBlockInteractEvent;

public class SpellCrafter extends InteractiveBlock {
    private GUI;
    SpellCrafter(Material material, Location loc) {
        super(material, loc);
    }

    @Override
    public void onClick(Action action, Player player, PlayerBlockInteractEvent event) {

    }

    @Override
    public void onGUIClick(ItemStack item, Player player) {

    }

    @Override
    public void onPlace(Location location) {

    }

    @Override
    public void onBreak(Location location) {

    }
}
