package org.tmmi.events.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Main;

public class PlayerListener implements Listener {
    private Main main;

    public PlayerListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player p = event.getPlayer();
        main.spellPerms.putIfAbsent(p.getUniqueId(), false);
        main.fm.loadPlayerSaveData(p);
    }

    @EventHandler
    public void onPlayerLeave(@NotNull PlayerQuitEvent event) {
        main.fm.savePlayerData(event.getPlayer());
    }
}
