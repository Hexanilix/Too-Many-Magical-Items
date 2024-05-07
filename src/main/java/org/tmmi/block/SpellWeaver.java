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
import org.tmmi.Main;
import org.tmmi.Spell;
import org.tmmi.WeavePlayer;

import java.util.*;

import static org.tmmi.Main.*;
import static org.tmmi.Spell.CastAreaEffect.getAreaEffect;
import static org.tmmi.Spell.Element.getElement;

public class SpellWeaver extends InteractiveBlock {
    private static final ItemStack NOS = newItemStack(Material.BARRIER, "No spell", unclickable);
    public static @NotNull Inventory gui() {
        Inventory inv = Bukkit.createInventory(null, 54, "Spell Weaver");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, background);
        inv.setItem(25, NOS);
        inv.setItem(34, newItemStack(Material.RED_WOOL, ChatColor.RED + "Weave", 2367845));
        inv.setItem(10, null);
        inv.setItem(11, null);
        inv.setItem(12, null);
        inv.setItem(13, null);
        return inv;
    };
    public static List<CrafttingCauldron> cauldron = new ArrayList<>();
    public static ItemStack item = newItem(Material.GOLD_BLOCK, ChatColor.DARK_AQUA + "MAGIC CARFTIN", 23461, List.of("A Crystal with the power", "of 1m brewing stands"));

    private int magicules;
    public SpellWeaver(Location loc) {
        super(Material.GOLD_BLOCK, loc, gui());
    }
    private final Map<UUID, Inventory> invs = new HashMap<>();

    @Override
    public void onClick(Action action, @NotNull Player player, PlayerInteractEvent event) {
        if (action == Action.RIGHT_CLICK_BLOCK) {
            WeavePlayer w = WeavePlayer.getWeaver(player);
            log(w);
            if (w != null) {
                invs.putIfAbsent(player.getUniqueId(), gui());
                player.openInventory(invs.get(player.getUniqueId()));
            } else {
                this.getLoc().getWorld().spawnParticle(Particle.ANGRY_VILLAGER, this.getLoc().clone().add(0.5, 0.5, 0.5), 0, 0, 0, 0);
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
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Spell.Element me = getElement(inv.getItem(10));
            Spell.CastAreaEffect ef = getAreaEffect(inv.getItem(12));
            if (me != null && ef != null) {
                inv.setItem(34, newItemStack(Material.LIME_WOOL, ChatColor.GREEN + "Weave", 2367845));
                ChatColor c;
                switch (me) {
                    case AIR -> c = ChatColor.WHITE;
                    case FIRE -> c = ChatColor.RED;
                    case EARTH -> c = ChatColor.GREEN;
                    default -> c = ChatColor.AQUA;
                }
                Spell.Element se = getElement(inv.getItem(11));
                ChatColor sc;
                switch (se == null ? me : se) {
                    case AIR -> sc = ChatColor.GRAY;
                    case FIRE -> sc = ChatColor.DARK_RED;
                    case EARTH -> sc = ChatColor.DARK_GREEN;
                    default -> sc = ChatColor.DARK_AQUA;
                }
                inv.setItem(25, newItemStack(inv.getItem(10).getType(), "Crafted Spell:", 142068, List.of(ChatColor.WHITE + "Main element: " + c + me.name(),
                        ChatColor.WHITE + "Sec Elem: " + sc + (se == null ? "null" : se.name()),
                        ChatColor.WHITE + "Area Effect: " + c + ef.name(),
                        ChatColor.WHITE + "Spell Type: Cantrip")));
            } else {
                inv.setItem(25, NOS);
                inv.setItem(34, newItemStack(Material.RED_WOOL, ChatColor.RED + "Weave", 2367845));
            }
        }).start();
        if (isSim(event.getCurrentItem(), background)) {event.setCancelled(true); return;}
        if (item == null) return;
        if (!item.hasItemMeta()) return;
        if (!item.getItemMeta().hasCustomModelData()) return;
        int data = item.getItemMeta().getCustomModelData();
        if (data == 2367845) {
            if (item.getType() == Material.LIME_WOOL) {
                WeavePlayer w = WeavePlayer.getWeaver(player);
                if (w != null) {
                    if (w.addSpell(new Spell(player.getUniqueId(), "bohemious",
                            getElement(inv.getItem(10)),
                            getElement(inv.getItem(11)),
                            getAreaEffect(inv.getItem(12)),
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
