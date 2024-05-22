package org.tmmi.events;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.tmmi.block.Block;
import org.tmmi.PlayerEvent;

public class PlayerBlockInteractEvent extends PlayerEvent {
    private final Block block;
    private final PlayerInteractEvent event;
    public PlayerBlockInteractEvent(Player player, Block block, @NotNull PlayerInteractEvent event) {
        super(player);
        this.block = block;
        this.event = event;
    }

    public PlayerInteractEvent getEvent() {
        return event;
    }

    public Block getBlock() {
        return block;
    }
}
