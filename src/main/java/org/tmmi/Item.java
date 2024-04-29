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
import java.util.Random;
import java.util.UUID;

import static org.tmmi.Main.digits;

public class Item extends ItemStack {
    public static List<Item> items = new ArrayList<>();
    public static @Nullable Item getItem(UUID id) {
        for (Item i : items) {
            if (i.getId() == id) return i;
        }
        return null;
    }
    public static @NotNull UUID uuid(@NotNull Material mat) {
        return UUID.fromString(Main.UUID_SEQUENCE + Main.toHex(mat.name(), 4) + '-' +
                Main.IntToHex(items.size(), 4) + '-' +
                Main.toHex("09w48nvy5g", 4) + '-' +
                String.valueOf(digits.charAt(new Random().nextInt(16))).repeat(8));
    }

    private final UUID id;
    public Item(Material mat, UUID id) {
        super(mat);
        this.id = (id == null ? uuid(mat) : id);
        items.add(this);
    }
    public Item(Material mat) {
        this(mat, null);
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
