package org.tmmi.item;

import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public abstract class BlockItem extends Item {
    public BlockItem(Type type) {
        super(type);
    }

    @Override
    public void onUse(PlayerInteractEvent event) {

    }

    @Override
    public void onDrop(PlayerDropItemEvent event) {

    }

    @Override
    public void onPickup(PlayerPickupItemEvent event) {

    }
}
