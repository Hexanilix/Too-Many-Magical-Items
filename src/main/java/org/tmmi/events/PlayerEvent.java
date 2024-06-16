package org.tmmi.events;

import org.bukkit.entity.Player;

public abstract class PlayerEvent extends Event {
    private final Player player;

    public PlayerEvent(Player player) {
        super();
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
