package org.tmmi;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.tmmi.Main.digits;

public abstract class Item extends ItemStack {
    public static List<Item> items = new ArrayList<>();
    public Item(Material mat) {
        super(mat);
        items.add(this);
    }
    public abstract void onUse(PlayerInteractEvent event);

    public abstract void onDrop(PlayerDropItemEvent event);

    public abstract void onPickup(PlayerPickupItemEvent event);

    public String toJSON() {
        ItemMeta m = this.getItemMeta();
        assert m != null;
        StringBuilder lor = new StringBuilder("\n");
        int i = 0;
        if (m.getLore() != null)
            for (String s : m.getLore()) {
                lor.append("\t\t\t").append(i).append(":\"").append(s).append("\"\n"); i++;
            }
        return "\t{\n" +
                (m.hasCustomModelData() ? "\t\t\"cus_md\":" + m.getCustomModelData() + ",\n" : "") +
                "\t\t\"material\":\"" + this.getType() + "\",\n" +
                "\t\t\"Lore\": [" + lor + "\n" +
                "\t\t],\n" +
                "\t}";
    }
}
