package org.tmmi.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.tmmi.spell.spells.FlameReel;
import org.tmmi.spell.spells.MagicMissile;

import java.util.List;

public class coolstick extends Item {
    public coolstick() {
        super(Material.STICK);
        ItemMeta m = this.getItemMeta();
        assert m != null;
        m.setDisplayName(ChatColor.GOLD + "Magic Miss");
        m.setCustomModelData(3845730);
        m.setLore(List.of());
        this.setItemMeta(m);
    }

    @Override
    public void onUse(@NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR) {
            new MagicMissile().cast(event.getPlayer().getEyeLocation(), 1, event.getPlayer());
        } else {
            new FlameReel().cast(event.getPlayer().getEyeLocation(), 1, event.getPlayer());
        }
    }

    @Override
    public void onDrop(PlayerDropItemEvent event) {

    }

    @Override
    public void onPickup(PlayerPickupItemEvent event) {

    }
}
