package org.tmmi;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.tmmi.Main.getRanUUIDstring;

public class Item extends ItemStack {
    public static List<Item> items = new ArrayList<>();


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
