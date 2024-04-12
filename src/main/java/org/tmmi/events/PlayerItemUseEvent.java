package org.tmmi.events;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.tmmi.PlayerEvent;

public class PlayerItemUseEvent extends PlayerEvent {
    private final ItemStack itemStack;
    private final PlayerInteractEvent event;

    public PlayerItemUseEvent(Player player, ItemStack itemStack, PlayerInteractEvent event) {
        super(player);
        this.itemStack = itemStack;
        this.event = event;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public PlayerInteractEvent getEvent() {
        return event;
    }
}
