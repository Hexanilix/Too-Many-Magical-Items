package org.tmmi.block;

import org.bukkit.Location;
import org.bukkit.Material;
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
import org.tmmi.events.PlayerBlockInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.tmmi.Main.log;

public class SpellCrafter extends InteractiveBlock {
    public static Inventory gui;
    public static List<CrafttingCauldron> cauldron = new ArrayList<>();
    public static ItemStack item;

    private int magicules;
    public SpellCrafter(Location loc) {
        super(Material.GOLD_BLOCK, loc, gui);
    }

    @Override
    public void onClick(Action action, @NotNull Player player, PlayerInteractEvent event) {
        player.openInventory(this.getGui());
    }

    @Override
    public void onGUIClick(InventoryAction action, @NotNull ItemStack item, Player player, @NotNull InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (Objects.requireNonNull(item.getItemMeta()).getCustomModelData() == 365450) {
            assert inv != null;
            log( new Spell(player.getUniqueId(), "bohemious",
                    Objects.requireNonNull(Spell.Element.getElement(inv.getItem(10))),
                    Spell.Element.getElement(inv.getItem(11)),
                    Spell.CastAreaEffect.getAreaEffect(inv.getItem(12)),
                    1000));
            player.closeInventory();
        }
        event.setCancelled(true);
    }
    public int getMagicules() {
        return 20;
    }
}
