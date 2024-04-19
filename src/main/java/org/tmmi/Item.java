package org.tmmi;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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


    private final UUID id;
    public Item(Material mat, UUID id) {
        super(mat);
        this.id = id;
    }
    public Item(Material mat) {
        this(mat, Main.newUUID(Main.TMMIobject.ITEM));
    }

    public UUID getId() {
        return id;
    }
}
