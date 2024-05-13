package org.tmmi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.Spells.Spell;
import org.tmmi.items.GrandBook;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.tmmi.Main.*;
import static org.tmmi.Spells.Weight.CANTRIP;
import static org.tmmi.Spells.Weight.SORCERY;

public class WeavePlayer {
    public static List<WeavePlayer> weavers = new ArrayList<>();
    public static @Nullable WeavePlayer getWeaver(@NotNull Player player) {
        return getWeaver(player.getUniqueId());
    }
    public static @Nullable WeavePlayer getWeaver(@NotNull HumanEntity human) {
        return getWeaver(human.getUniqueId());
    }
    public static @Nullable WeavePlayer getWeaver(UUID id) {
        for (WeavePlayer w : weavers)
            if (w.getPlayer().getUniqueId() == id) return w;
        return null;
    }

    private final Player player;
    private boolean isWeaving;
    private final List<Spell> spells = new ArrayList<>();
    private GrandBook grandBook;
    private Element element;
    private Spell main;
    private Spell sec;
    private int canSize;
    private int sorSize;
    public WeavePlayer(Player handler, Element element, int canSize, int sorSize) {
        this.player = handler;
        this.isWeaving = false;
        this.element = element;
        this.canSize = canSize;
        this.sorSize = sorSize;
        weavers.add(this);
    }
    public WeavePlayer(Player handler) {
        this(handler, null, 5, 2);
    }

    public static @NotNull WeavePlayer getOrNew(@NotNull Player p) {
        WeavePlayer w = getWeaver(p.getUniqueId());
        if (w == null) return new WeavePlayer(p);
        return w;
    }

    public Inventory inventory() {
        Inventory inv = Bukkit.createInventory(null, 54, "Spell Weaver");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, background);
        setCusData(inv.getItem(0), 2003245);
        List<Spell> ss = getSorSpells();
        for (int i = 10; i < 17; i++) {
            if (i < ss.size()) {
                Spell s = ss.get(i);
                ItemStack item = s.toItem();
                if (s == this.getMain()) {
                    ItemMeta m = item.getItemMeta();
                    m.addEnchant(Enchantment.SHARPNESS, 0, true);
                    item.setItemMeta(m);
                }
                inv.setItem(i, item);
            } else inv.setItem(i, (i-10 < this.sorSize ? newItemStack(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Empty Slot", unclickable) : newItemStack(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Locked Slot", unclickable)));
        }
        List<Spell> cs= getCanSpells();
        for (int i = 0; i < 14; i++) {
            int j = (i > 6 ? i + 30 : i + 28);
            if (j < cs.size()) {
                Spell s = cs.get(j);
                ItemStack item = s.toItem();
                if (s == this.getMain()) {
                    ItemMeta m = item.getItemMeta();
                    m.addEnchant(Enchantment.SHARPNESS, 0, true);
                    item.setItemMeta(m);
                }
                inv.setItem(j, item);
            } else inv.setItem(j, (i < this.canSize ? newItemStack(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Empty Slot", unclickable) : newItemStack(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Locked Slot", unclickable)));
        }
        for (int i = 28; i < this.canSize; i++) inv.setItem((i > 35 ? i+3 : i), null);
        int j = 28;
        for (Spell s : cs) {
            log(s.getName());
            ItemStack item = s.toItem();
            inv.setItem(j, item);
            j++;
        }
        return inv;
    }

    private List<Spell> getCanSpells() {
        return spells.stream().filter(s -> s.getWeight() == CANTRIP).toList();
    }
    private List<Spell> getSorSpells() {
        return spells.stream().filter(s -> s.getWeight() == SORCERY).toList();
    }

    public Player getPlayer() {
        return player;
    }

    public void cast(@NotNull PlayerInteractEvent event) {
        Spell s = (event.getAction().name().contains("LEFT") ? this.getMain() : this.getSecondary());
        if (s != null) {
            s.cast(event, this.player.getEyeLocation(), 1);
        }
    }

    public int getCanSize() {
        return canSize;
    }

    public int getSorSize() {
        return sorSize;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public boolean addSpell(@NotNull Spell s) {
        if (s.getWeight() == CANTRIP) {
            if (this.canSize <= getCanSpells().size()) return false;
            else spells.add(s);
        } else {
            if (this.sorSize <= getSorSpells().size()) return false;
            else spells.add(s);
        }
        return true;
    }
    public boolean removeSpell(@NotNull Spell s) {
        return spells.remove(s);
    }

    public List<Spell> getSpells() {
        return spells;
    }

    public boolean isWeaving() {
        return isWeaving;
    }

    public void setWeaving(boolean b) {
        isWeaving = b;
    }

    public boolean hasGrandBook() {
        return grandBook != null;
    }

    public void setMain(Spell s) {
        if (s == null || getCanSpells().contains(s) || getSorSpells().contains(s)) {
            main = s;
        } else {
            if (s.getHandler() == player.getUniqueId()) {
                addSpell(s);
                main = s;
            } else log("Soft warning: Foreign spell used in inventory");
        }
    }
    public Spell getMain() {return main;}

    public void setSecondary(Spell s) {
        if (getCanSpells().contains(s) || getSorSpells().contains(s)) {
            sec = s;
        } else {
            if (s.getHandler() == player.getUniqueId()) {
                addSpell(s);
                sec = s;
            } else log("Soft warning: Foreign spell used in inventory");
        }
    }
    public Spell getSecondary() {return sec;}
}
