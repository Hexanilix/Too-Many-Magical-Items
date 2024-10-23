package org.tmmi.item.items.potions;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.hetils.Pair;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Main;
import org.tmmi.item.Item;

import java.util.*;

import static org.tmmi.Main.plugin;


public class PortableGlacier extends Item {
    public PortableGlacier(int level) {
        super(Material.SPLASH_POTION);
        PotionMeta m = (PotionMeta) this.getItemMeta();
        m.setColor(Color.fromRGB(127, 127, 255));
        m.setDisplayName(ChatColor.AQUA + "Portable Glacier");
        m.setLore(List.of( ChatColor.DARK_AQUA + "Level " + level));
        m.setCustomModelData(576883);
        m.getPersistentDataContainer().set(
                PotionListener.PORTABLE_GLACIER,
                PersistentDataType.INTEGER,
                m.getCustomModelData()
        );
        m.getPersistentDataContainer().set(
                PotionListener.POTION_LEVEL,
                PersistentDataType.INTEGER,
                level
        );
        this.setItemMeta(m);
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
