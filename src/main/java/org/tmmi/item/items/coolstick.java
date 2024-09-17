package org.tmmi.item.items;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.tmmi.WeavePlayer;
import org.tmmi.item.Item;
import org.tmmi.spell.Spell;
import org.tmmi.spell.spells.FlameReel;
import org.tmmi.spell.spells.MagicMissile;

import java.util.List;

import static org.hetils.minecraft.General.isSim;
import static org.tmmi.Main.plugin;

public class coolstick extends Item {
    public static ItemStack COOLSTICK = new coolstick();
    public coolstick() {
        super(Material.STICK);
        ItemMeta m = this.getItemMeta();
        assert m != null;
        m.setDisplayName(ChatColor.GOLD + "Magic Wand");
        Item.setCusData(m, Items.COOLSTICK);
        m.setLore(List.of());
        this.setItemMeta(m);
        Bukkit.getPluginManager().registerEvents(new Listener(), plugin);
    }
    @Override
    public void onUse(@NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR)
            ss[sesl % ss.length].cast(event.getPlayer().getEyeLocation(), 1, event.getPlayer());
    }
    int sesl = 0;
    Spell[] ss = new Spell[]{new MagicMissile(), new FlameReel()};
    public class Listener implements org.bukkit.event.Listener {
        @EventHandler
        public void onPlayerItemHeld(@NotNull PlayerItemHeldEvent event) {
            Player p = event.getPlayer();
            if (p.isSneaking() && isSim(p.getInventory().getItemInMainHand(), coolstick.this)) {
                sesl += event.getNewSlot() - event.getPreviousSlot();
                if (sesl < 0) sesl += 16784;
                else if (sesl > 16784) sesl -= 16784;
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ss[sesl % ss.length].getName()));
                p.getInventory().setHeldItemSlot(event.getPreviousSlot());
            }
        }
        @EventHandler
        public void onInvOpen(PlayerDropItemEvent event) {
            Player p = event.getPlayer();
            if (p.isSneaking() && isSim(event.getItemDrop().getItemStack(), coolstick.this)) {
                event.setCancelled(true);
                p.openInventory(WeavePlayer.getWeaver(p).inventory());
            }
        }
    }

    @Override
    public void onDrop(PlayerDropItemEvent event) {

    }

    @Override
    public void onPickup(PlayerPickupItemEvent event) {

    }
}
