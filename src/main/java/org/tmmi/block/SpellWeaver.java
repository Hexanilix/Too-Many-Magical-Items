package org.tmmi.block;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tmmi.InteractiveBlock;
import org.tmmi.Spell;
import org.tmmi.WeavePlayer;

import java.util.*;

import static org.tmmi.Main.*;
import static org.tmmi.Spell.Element.getElement;

public class SpellWeaver extends InteractiveBlock {
    public static Map<UUID, Inventory> invs = new HashMap<>();
    public static @NotNull Inventory gui() {
        Inventory inv = Bukkit.createInventory(null, 54, "Spell Weaver");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, background);
        inv.setItem(25, newItemStack(Material.ENDER_EYE, ChatColor.GOLD + "Spell", 245782, List.of("Lala", "leh")));
        inv.setItem(34, newItemStack(Material.RED_WOOL, ChatColor.RED + "Weave", 2367845));
        inv.setItem(10, null);
        inv.setItem(11, null);
        inv.setItem(12, null);
        inv.setItem(13, null);
        return inv;
    };
    public static List<CrafttingCauldron> cauldron = new ArrayList<>();
    public static ItemStack item;

    private int magicules;
    public SpellWeaver(Location loc) {
        super(Material.GOLD_BLOCK, loc, gui());
    }

    @Override
    public void onClick(Action action, @NotNull Player player, PlayerInteractEvent event) {
        if (action == Action.RIGHT_CLICK_BLOCK) {
            WeavePlayer w = WeavePlayer.getWeaver(player);
            log(w);
            if (w != null) {
                invs.putIfAbsent(player.getUniqueId(), gui());
                player.openInventory(invs.get(player.getUniqueId()));
            } else {
                this.getLoc().getWorld().spawnParticle(Particle.VILLAGER_ANGRY, this.getLoc().clone().add(0.5, 0.5, 0.5), 0, 0, 0, 0);
            }
            event.setCancelled(true);
        } else {
            if (action != Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onGUIClick(InventoryAction action, @NotNull ItemStack item, Player player, @NotNull InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (getElement(inv.getItem(10)) != null && Spell.CastAreaEffect.getAreaEffect(inv.getItem(12)) != null)
                inv.setItem(34, newItemStack(Material.LIME_WOOL, ChatColor.GREEN + "Weave", 2367845));
            else
                inv.setItem(34, newItemStack(Material.RED_WOOL, ChatColor.RED + "Weave", 2367845));
        }).start();
        if (isSim(event.getCurrentItem(), background)) {event.setCancelled(true); return;}
        if (item == null) return;
        if (!item.hasItemMeta()) return;
        if (!item.getItemMeta().hasCustomModelData()) return;
        int data = item.getItemMeta().getCustomModelData();
        if (data == 2367845) {
            log("int");
            if (item.getType() == Material.LIME_WOOL) {
                log("yuh");
                WeavePlayer w = WeavePlayer.getWeaver(player);
                if (w != null) {
                    if (w.addSpell(new Spell(player.getUniqueId(), "bohemious",
                            getElement(inv.getItem(10)),
                            getElement(inv.getItem(11)),
                            Spell.CastAreaEffect.getAreaEffect(inv.getItem(12)),
                            1000))) {
                        invs.replace(player.getUniqueId(), gui());
                        player.closeInventory();
                    } else {
                        player.sendMessage(ChatColor.RED + "Your spell inventory for this spell type if full!");
                        player.playSound(player.getEyeLocation(), Sound.ENTITY_VILLAGER_NO, 1, 0);
                    }
                }
            }
            event.setCancelled(true);
        }
    }
    public int getMagicules() {
        return 20;
    }
}
