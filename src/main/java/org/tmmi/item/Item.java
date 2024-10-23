package org.tmmi.item;

import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.Main;
import org.tmmi.UIID;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public abstract class Item extends ItemStack {
    public static final NamespacedKey ITEM_UIID = new NamespacedKey(Main.name, "uiid");
    public static final NamespacedKey ITEM_TYPE = new NamespacedKey(Main.name, "item_type");
    public static final Collection<Item> items = new HashSet<>();

    private final UIID uiid;

    public Item(Type type) {
        super();
        if (type == null)
            throw new RuntimeException("Item type cannot be null.");
        this.uiid = new UIID(type);
        setItem(this);
    }

    public Item(UUID uuid) {
        super();
        if (uuid == null)
            throw new RuntimeException("Item UUID cannot be null.");
        this.uiid = new UIID(uuid);
        setItem(this);
    }

    public Item(UIID uiid) {
        super();
        if (uiid == null)
            throw new RuntimeException("Item UUID cannot be null.");
        this.uiid = uiid;
        setItem(this);
    }

    public static @Nullable Item get(String uuid) {
        for (Item i : items)
            if (i.getUIID().sameAs(uuid))
                return i;
        return null;
    }
    public static @Nullable Item get(UUID uuid) {
        for (Item i : items)
            if (i.getUIID().equals(uuid))
                return i;
        return null;
    }

    public final Type getItemType() {
        return this.uiid.getItemType();
    }

    public UIID getUIID() {
        return uiid;
    }

    public abstract void onUse(PlayerInteractEvent event);

    public abstract void onDrop(PlayerDropItemEvent event);

    public abstract void onPickup(PlayerPickupItemEvent event);

    public String toJSON() {
        return "{UIID:"+this.uiid+",type:"+this.getItemType()+"}";
    }

    private static void setItem(@NotNull Item item) {
        UIID uiid = item.getUIID();
        ItemMeta m = item.getItemMeta();
        m.getPersistentDataContainer().set(
                ITEM_UIID,
                PersistentDataType.STRING,
                uiid.toUUID()
        );
        m.getPersistentDataContainer().set(
                ITEM_TYPE,
                PersistentDataType.STRING,
                uiid.getItemType().name()
        );
        items.add(item);
    }

    public static UIID getUIID(ItemStack item) {
        UIID t = null;
        if (item == null || !item.hasItemMeta()) return t;
        ItemMeta m = item.getItemMeta();
        PersistentDataContainer c = m.getPersistentDataContainer();
        if (!c.has(ITEM_UIID)) return t;
        t = new UIID(c.get(ITEM_UIID, PersistentDataType.STRING));
        return t;
    }
    public static String getUUID(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta m = item.getItemMeta();
        PersistentDataContainer c = m.getPersistentDataContainer();
        if (!c.has(ITEM_UIID)) return null;
        return c.get(ITEM_UIID, PersistentDataType.STRING);
    }
}
