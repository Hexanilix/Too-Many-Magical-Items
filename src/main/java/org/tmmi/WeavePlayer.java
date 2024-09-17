package org.tmmi;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.hetils.minecraft.General;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.item.Item;
import org.tmmi.item.items.GrandBook;
import org.tmmi.item.items.coolstick;
import org.tmmi.spell.Spell;
import org.tmmi.spell.spells.FlameReel;
import org.tmmi.spell.spells.MagicMissile;

import java.util.*;

import static org.hetils.minecraft.General.isSim;
import static org.hetils.minecraft.Item.*;
import static org.tmmi.Main.*;
import static org.tmmi.spell.atributes.Weight.CANTRIP;
import static org.tmmi.spell.atributes.Weight.SORCERY;

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

    int sesl = 0;
    Spell[] ss = new Spell[]{new MagicMissile(), new FlameReel()};
    public class Listener implements org.bukkit.event.Listener {
        @EventHandler
        public void onPlayerItemHeld(@NotNull PlayerItemHeldEvent event) {
            Player p = event.getPlayer();
            if (p.isSneaking() && isSim(p.getInventory().getItemInMainHand(), coolstick.COOLSTICK)) {
                sesl += event.getNewSlot() - event.getPreviousSlot();
                if (sesl < 0) sesl += 16784;
                else if (sesl > 16784) sesl -= 16784;
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ss[sesl % ss.length].getName()));
                p.getInventory().setHeldItemSlot(event.getPreviousSlot());
            }
        }
        @EventHandler
        public void onInvOpen(@NotNull PlayerDropItemEvent event) {
            Player p = event.getPlayer();
            if (p.isSneaking() && isSim(event.getItemDrop().getItemStack(), coolstick.COOLSTICK)) {
                event.setCancelled(true);
                p.openInventory(WeavePlayer.getWeaver(p).inventory());
            }
        }
    }
    private final Player player;
    private ManaBar mana;
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
        this.mana = new ManaBar(maxMana, 0);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (handler != null) {
                    ManaBar ma = WeavePlayer.this.getManaBar();
                    if (manaCool <= 0) {
                        if (ma.amount < ma.limit) {
                            MagicChunk.takeMana(10);
                            ma.add(m);
                            mc.subMana(m);
                        }
                    } else manaCool--;
                    if (handler.getGameMode() != GameMode.CREATIVE) handler.spigot()
                            .sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                    ChatColor.RED + "❤" + Math.round(handler.getHealth()) + "/" + Math.round(handler.getMaxHealth()) + ChatColor.AQUA + "       ֍" + ma.amount + "/" + ma.limit));
                }
            }
        }.runTaskTimer(plugin, 0, 2);
        weavers.add(this);
    }
    public WeavePlayer(Player handler) {
        this(handler, null, 5, 2, 100);
    }

    public WeavePlayer subMana(int sub) {
        this.mana.sub(sub);
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
        setCustomData(inv.getItem(0), 2003245);
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
            if (player.getGameMode() == GameMode.CREATIVE || s.getCc() <= mana.amount) {
                s.cast(this.player.getEyeLocation(), 1, this.player);
                if (player.getGameMode() != GameMode.CREATIVE) mana.sub(s.getCc());
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
        this.mana = new ManaBar(this.mana.limit + maxMana, this.mana.amount);
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
        return Arrays.stream(player.getInventory().getContents()).anyMatch(i -> General.isSim(i, grandBook));
    }

    private void bkgSetMain(Spell s) {
        if (s == null || getCanSpells().contains(s) || getSorSpells().contains(s)) {
            main = s;
        }
    }
    public void setMain(Spell s) {
        if (s == null || getCanSpells().contains(s) || getSorSpells().contains(s)) {
            main = s;
        }
    }
    public Spell getMain() {return main;}

    public void setSecondary(Spell s) {
        if (getCanSpells().contains(s) || getSorSpells().contains(s)) {
            sec = s;
        }
    }
    public Spell getSecondary() {return sec;}

    public String toJSON() {
        List<String> spells = this.spells.stream().map(Spell::json).toList();
        return "\t\"element\": \"" + this.element + "\",\n" +
                "\t\"max_mana\": " + this.mana.limit + ",\n" +
                "\t\"can_size\": " + this.canSize + ",\n" +
                "\t\"sor_size\": " + this.sorSize + ",\n" +
                "\t\"spells\": [\n" +
                String.join(",\n", spells.stream().map(s -> String.join("\n\t\t", s.split("\n"))).toList()) +
                "\n\t],\n" +
                "\t\"main\":\"" + (this.main != null ? this.main.getId() : "null") + "\",\n" +
                "\t\"second\":\"" + (this.sec != null ?  this.sec.getId() : "null") + "\"";

    }

    public float getMana() {
        return this.mana.amount;
    }
    public ManaBar getManaBar() {return this.mana;}

    public void setManaBar(ManaBar manaBar) {
        this.mana = manaBar;
    }
}
