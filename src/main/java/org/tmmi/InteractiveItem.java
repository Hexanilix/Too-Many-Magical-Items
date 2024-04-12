package org.tmmi;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class InteractiveItem implements Listener {
    private final int id;
    private final ItemStack item;

    public InteractiveItem(@NotNull ItemStack item) {
        this.item = item;
        this.id = Objects.requireNonNull(item.getItemMeta()).getCustomModelData();
    }

    public int getId() {
        return id;
    }

    public ItemStack getItem() {
        return item;
    }

    public abstract void onUse(Action action);
    public abstract void onDrop();
    public abstract void onPickup(Player p);
}
