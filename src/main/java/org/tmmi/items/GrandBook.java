package org.tmmi.items;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tmmi.InteractiveItem;

public class GrandBook extends InteractiveItem {
    public GrandBook(@NotNull ItemStack item) {
        super(item.getType());
    }

    @Override
    public void onUse(Action action) {

    }

    @Override
    public void onDrop() {

    }

    @Override
    public void onPickup(Player p) {

    }
}
