package org.tmmi.item.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.tmmi.WeavePlayer;
import org.tmmi.item.Item;

import java.util.ArrayList;
import java.util.List;

import static org.tmmi.Main.log;

public class SpellBook extends Item {
    public static List<SpellBook> spellbooks = new ArrayList<>();

    public SpellBook() {
        super(Material.ENCHANTED_BOOK);
        ItemMeta m = this.getItemMeta();
        assert m != null;
        m.setDisplayName(ChatColor.GOLD + "Spell Book");
        m.setCustomModelData(3845730 + spellbooks.size());
        m.setLore(List.of());
        this.setItemMeta(m);
    }
    public void invClick(InventoryClickEvent event) {

    }

    @Override
    public void onUse(@NotNull PlayerInteractEvent event) {
        log(WeavePlayer.weavers);
        Player p = event.getPlayer();
        WeavePlayer w = WeavePlayer.getWeaver(p);
        log(w);
        if (w == null) return;
        p.openInventory(w.inventory());
    }

    public void onDrop(PlayerDropItemEvent event) {

    }

    public void onPickup(PlayerPickupItemEvent event) {

    }
}