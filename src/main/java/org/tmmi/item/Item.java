package org.tmmi.item;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.HashSet;

public abstract class Item extends ItemStack {
    public static void setCusData(ItemMeta m, Items i) {
        if (i != null) m.setCustomModelData(CUSTOM_EXCLUSIVE+switch (i) {
            case BLACK_BACKGROUND -> 1;
            case COOLSTICK -> 100;
            case FOCUS_WAND -> 101;
            case CREDIT_COOKIE -> 102;
        });
    }
    public static final int CUSTOM_EXCLUSIVE = 38750;
    public enum Items {
        BLACK_BACKGROUND,
        COOLSTICK,
        FOCUS_WAND,
        CREDIT_COOKIE
    }
    public static Collection<Item> items = new HashSet<>();
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
