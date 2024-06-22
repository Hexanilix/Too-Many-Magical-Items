package org.tmmi;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.items.GrandBook;
import org.tmmi.spells.Spell;

import java.util.*;

import static org.hetils.Util.*;
import static org.tmmi.Main.*;
import static org.tmmi.spells.atributes.Weight.CANTRIP;
import static org.tmmi.spells.atributes.Weight.SORCERY;

public class WeavePlayer {
    public static Collection<WeavePlayer> weavers = new HashSet<>();
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
    private float mana;
    private float maxMana;
    private boolean isWeaving;
    private final List<Spell> spells = new ArrayList<>();
    private GrandBook grandBook;
    private Element element;
    private Spell main;
    private Spell sec;
    private int canSize;
    private int sorSize;
    Thread manaThread;
    private int manaCool = 0;
    public WeavePlayer(Player handler, Element element, int canSize, int sorSize, float maxMana) {
        this.player = handler;
        this.isWeaving = false;
        this.element = element;
        this.canSize = canSize;
        this.sorSize = sorSize;
        this.maxMana = maxMana;
        this.mana = 0;
//        this.manaThread = newThread(() -> {
//            try {
//                while (true) {
//                    if (handler != null) {
//                        if (manaCool == 0 && mana < maxMana) {
//                            mana += (int) Math.min((float) MagicChunk.getOrNew(handler).mean() / 250, maxMana - mana);
//                        } else manaCool--;
//                    }
//                    Thread.sleep(1000);
//                }
//            } catch (InterruptedException ignore) {}
//        });
//        this.manaThread.start();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (handler != null) {
                    if (manaCool == 0 && mana < maxMana) {
                        MagicChunk mc = MagicChunk.getOrNew(handler);
                        int m = (int) Math.min((float) mc.mean() / 250, maxMana - mana);
                        mana += m;
                        mc.subMana(m);
                    } else manaCool--;
                }
            }
        }.runTaskTimer(plugin, 0, 20);
        weavers.add(this);
    }
    public WeavePlayer(Player handler) {
        this(handler, null, 5, 2, 25);
    }

    public WeavePlayer subMana(int sub) {
        this.mana-=sub;
        return this;
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
                    m.addEnchant(Enchantment.UNBREAKING, 0, true);
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



    public void cast() {
        Spell s = main;
        if (s != null) {
            if (player.getGameMode() == GameMode.CREATIVE || s.getCastCost() <= mana) {
                s.cast(this.player.getEyeLocation(), 1);
                if (player.getGameMode() != GameMode.CREATIVE) mana -= s.getCastCost();
                manaCool = 5;
            }
        }
    }
    public void expandCan() {
        if (this.canSize + 1 <= 18)
            this.canSize ++;
    }
    public void expandCan(int amnt) {
        if (this.canSize + amnt <= 18)
            this.canSize += amnt;
    }
    public void expandSor() {
        if (this.sorSize + 1 <= 18)
            this.sorSize ++;
    }
    public void expandSor(int amnt) {
        if (this.sorSize + amnt <= 18)
            this.sorSize += amnt;
    }

    public void addMaxMana(float maxMana) {
        this.maxMana += maxMana;
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
        return Arrays.stream(player.getInventory().getContents()).anyMatch(i -> isSim(i, grandBook));
    }

    private void bkgSetMain(Spell s) {
        if (s == null || getCanSpells().contains(s) || getSorSpells().contains(s)) {
            main = s;
        } else {
            if (s.getHandler().compareTo(player.getUniqueId()) != 0) log("Soft warning: Foreign spell used in inventory");
        }
    }
    public void setMain(Spell s) {
        if (s == null || getCanSpells().contains(s) || getSorSpells().contains(s)) {
            main = s;
        } else {
            if (s.getHandler().compareTo(player.getUniqueId()) == 0) {
                addSpell(s);
                bkgSetMain(s);
                main = s;
            } else log("Soft warning: Foreign spell used in inventory");
        }
    }
    public Spell getMain() {return main;}

    public void setSecondary(Spell s) {
        if (getCanSpells().contains(s) || getSorSpells().contains(s)) {
            sec = s;
        } else {
            if (s.getHandler().compareTo(player.getUniqueId()) == 0) {
                addSpell(s);
                sec = s;
            } else log("Soft warning: Foreign spell used in inventory");
        }
    }
    public Spell getSecondary() {return sec;}

    public String toJSON() {
        List<String> spells = this.spells.stream().map(Spell::toJson).toList();
        return "\t\"element\": \"" + this.element + "\",\n" +
                "\t\"max_mana\": " + this.maxMana + ",\n" +
                "\t\"can_size\": " + this.canSize + ",\n" +
                "\t\"sor_size\": " + this.sorSize + ",\n" +
                "\t\"spells\": [\n" +
                String.join(",\n", spells.stream().map(s -> String.join("\n\t\t", s.split("\n"))).toList()) +
                "\n\t],\n" +
                "\t\"main\":\"" + (this.main != null ? this.main.getId() : "null") + "\",\n" +
                "\t\"second\":\"" + (this.sec != null ?  this.sec.getId() : "null") + "\"";

    }

    public float getMana() {
        return this.mana;
    }
}
