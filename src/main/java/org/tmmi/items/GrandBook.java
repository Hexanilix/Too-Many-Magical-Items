package org.tmmi.items;

import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GrandBook extends Item {
    public GrandBook(@NotNull ItemStack item) {
        super(item.getType());
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
