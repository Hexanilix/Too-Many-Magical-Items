package org.tmmi;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public abstract class InteractiveItem extends Item {
    public InteractiveItem(@NotNull Material mat) {
        super(mat);
    }
    public InteractiveItem(@NotNull Material mat, UUID id) {
        super(mat, id);
    }

    public abstract void onUse(Action action);
    public abstract void onDrop();
    public abstract void onPickup(Player p);
}
