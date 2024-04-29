package org.tmmi;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Item extends ItemStack {
    public static List<Item> items = new ArrayList<>();
    public static @Nullable Item getItem(UUID id) {
        for (Item i : items) {
            if (i.getId() == id) return i;
        }
        return null;
    }
    private @NotNull UUID uuid() {
        return UUID.fromString();
    }

    private final UUID id;
    public Item(Material mat, UUID id) {
        super(mat);
        this.id = (id == null ? uuid() : id);
        items.add(this);
    }
    public Item(Material mat) {
        this(mat, Main.newUUID(Main.TMMIobject.ITEM));
    }

    public UUID getId() {
        return id;
    }

    public void onUse(PlayerInteractEvent event) {

    }

    public void onDrop(PlayerDropItemEvent event) {

    }

    public void onPickup(PlayerPickupItemEvent event) {

    }
}
