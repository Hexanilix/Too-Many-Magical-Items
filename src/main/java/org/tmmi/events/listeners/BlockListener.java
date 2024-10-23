package org.tmmi.events.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.EntityMultiplier;
import org.tmmi.Main;
import org.tmmi.block.*;
import org.tmmi.item.Item;

import java.util.Objects;

import static org.hetils.minecraft.General.isSim;

public class BlockListener implements Listener {
    private final Main main;

    public BlockListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getClickedBlock() != null)
            for (InteractiveBlock b : InteractiveBlock.instances)
                if (b.getLoc().getBlock().equals(event.getClickedBlock().getLocation().getBlock())) {
                    b.onClick(event.getAction(), event.getPlayer(), event);
                    return;
                }
    }

    @EventHandler
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        EntityMultiplier.instances.remove(EntityMultiplier.get(event.getEntity()));
    }

    public enum BlockType {
        Cauldron;
        public static final int l = BlockType.values().length;
        public static @Nullable BlockType getType(String type) {
            for (int i = 0; i < l; i++) {
                BlockType t = BlockType.values()[i];
                if (t.name().equals(type)) return t;
            }
            return null;
        }
    }

    public static final NamespacedKey BLOCK_TYPE = new NamespacedKey(Main.name, "block_type");
    public static BlockType getBlock(ItemStack item) {
        BlockType t = null;
        if (item == null || !item.hasItemMeta()) return t;
        ItemMeta m = item.getItemMeta();
        PersistentDataContainer c = m.getPersistentDataContainer();
        if (!c.has(BLOCK_TYPE)) return t;
        t= BlockType.getType(c.get(BLOCK_TYPE, PersistentDataType.STRING));
        return t;
    }

    @EventHandler
    public void placeBlock(@NotNull BlockPlaceEvent event) {
        ItemStack inmh = event.getItemInHand();
        BlockType t = getBlock(inmh);
        if (t != null) {
            Item i = Item.get(Item.getUUID(inmh));
        }
    }

    @EventHandler
    public void breakBlock(@NotNull BlockBreakEvent event) {
        for (Block l : Block.instances)
            if (isSim(l.getBlock().getLocation(), event.getBlock().getLocation())) {
                l.remove(event.getPlayer().getGameMode() != GameMode.CREATIVE);
                return;
            }
    }
}
