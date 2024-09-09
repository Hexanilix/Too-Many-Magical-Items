package org.tmmi.spell;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.Element;
import org.tmmi.spell.atributes.Weight;
import org.tmmi.events.SpellCollideEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.hetils.Item.newItemStack;
import static org.hetils.Util.*;
import static org.tmmi.Main.*;

public abstract class Spell {
    public static Collection<Spell> spells = new HashSet<>();
    public static ArrayList<UUID> disabled = new ArrayList<>();
    public static double mxT = 20;
    public static double mxS = 20;
    public static double mxD = 20;

    public static @Nullable Spell getSpell(UUID id) {
        for (Spell s : spells)
            if (id.equals(s.getId()))
                return s;
        return null;
    }

    public static Map<LivingEntity, Double> entDmgMap = new HashMap<>();
    public static void damageEnt(@NotNull LivingEntity e, double dmg) {
        log("added " + dmg + " to " + e.getName());
        entDmgMap.putIfAbsent(e, 0d);
        entDmgMap.replace(e, entDmgMap.get(e)+dmg);
    }
    public static BukkitRunnable damageRunnable = new BukkitRunnable() {
        @Override
        public void run() {
            for (Map.Entry<LivingEntity, Double> e : entDmgMap.entrySet())
                e.getKey().damage(e.getValue());
            entDmgMap.clear();
        }
    };

    public enum SpellAtribute {
        CHARGE
    }

    private final UUID id;
    private final String name;
    private int level;
    private Element element;
    private int XP;
    private int cc;
    private final Weight weight;

    private @NotNull UUID uuid() {
        String u = UUID_SEQUENCE + IntToHex(spells.size(), 4) +
                '-' +
                toHex("3hure9tgi", 4) +
                '-' +
                toHex(weight.name(), 4) +
                '-' +
                toHex(name, 4) +
                String.valueOf(digits.charAt(new Random().nextInt(16))).repeat(8);
        return UUID.fromString(u);
    }
    public Spell(UUID id, String name, Weight weight, Element element, int level, int XP, int cc) {
        this.name = name;
        this.cc = cc;
        assert level > 0;
        this.level = level;
        this.XP = XP;
        this.weight = weight;
        this.element = element;
        this.id = (id == null ? uuid() : id);
        if (!DISABLED_SPELLS.v().contains(this.id)) spells.add(this);
    }
    public String getName() {
        return name;
    }
    public UUID getId() {
        return id;
    }
    public int getLevel() {
        return level;
    }
    public int getCc() {
        return cc;
    }

    public void setCc(int cc) {
        this.cc = cc;
    }

    public int getXP() {
        return XP;
    }
    public void addXP(int XP) {
        this.XP += XP;
        this.attemptLvlUP();
    }
    public Weight getWeight() {
        return weight;
    }
    public void attemptLvlUP() {
        while (XP - xpsum(this.level-1) >= lvlXPcalc(this.level-1)) {
            this.level++;
            this.onLevelUP();
        }
    }

    public Element getElement() {
        return this.element;
    }

    public abstract CastSpell cast(Location castLocation, float multiplier, Entity e);

    public abstract void onLevelUP();

    public void onCollide(SpellCollideEvent event) {}

    @Contract(value = " -> new", pure = true)
    private @NotNull List<String> extraItemLore() {
        return new ArrayList<>();
    }
    public final @NotNull ItemStack toItem() {
        final ChatColor c = switch (this.element) {
            case AIR -> ChatColor.WHITE;
            case FIRE -> ChatColor.RED;
            case EARTH -> ChatColor.GREEN;
            default -> ChatColor.AQUA;
        };
        final ChatColor sc = switch (this.element) {
            case AIR -> ChatColor.GRAY;
            case FIRE -> ChatColor.DARK_RED;
            case EARTH -> ChatColor.DARK_GREEN;
            default -> ChatColor.DARK_AQUA;
        };
        final int nxtlxp = this.getXP() - (this.xpsum(this.getLevel()-1));
        final int nxtLvlXP = lvlXPcalc(this.getLevel()-1);
        final int perc = (int) (((float) nxtlxp / nxtLvlXP) * 100);
        List<String> lore = new ArrayList<>(List.of(c + "Cast Cost: " + cc));
        lore.addAll(extraItemLore());
        lore.addAll(List.of(c+"Level "+c+this.getLevel(), c + "[" +
                        "-".repeat(Math.max(0, perc/10)) +
                        sc + "-".repeat(Math.max(0, 10 - perc/10)) +
                        c + "]" + (nxtlxp >= 1000 ? BigDecimal.valueOf((float) nxtlxp / 1000).setScale(2, RoundingMode.HALF_EVEN) + "k" : nxtlxp) +
                        sc + "/" + c + (nxtLvlXP >= 1000 ? BigDecimal.valueOf((float) nxtLvlXP / 1000).setScale(2, RoundingMode.HALF_EVEN) + "k" : nxtLvlXP),
                ChatColor.GRAY + "Total XP: " + this.getXP()));
        final ItemStack item = switch (weight) {
            case CANTRIP -> newItemStack(Material.PAPER, ChatColor.AQUA + name, 34900, lore);
            case INCANTATION -> newItemStack(Material.WRITTEN_BOOK, ChatColor.LIGHT_PURPLE + name, 34900, lore);
            case SORCERY -> newItemStack(Material.ENCHANTED_BOOK, ChatColor.GOLD + "" + ChatColor.BOLD + name, 34900, lore);
        };
        ItemMeta m = item.getItemMeta();
        m.setLore(lore);
        item.setItemMeta(m);
        return item;
    }

    public int xpsum(int lvl) {
        int s = 0;
        for (int i = 0; i < lvl; i++) s += lvlXPcalc(i);
        return s;
    }
    public static int lvlXPcalc(int lvl) {
        return (int) (lvl < 0 ? 0 : Math.round(((Math.pow(14, 1+lvl))/Math.pow(10, lvl)-4)));
    }

    @Override
    public String toString() {
        return "Spell{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", XP=" + XP +
                ", castCost=" + cc +
                ", spellType=" + weight +
                '}';
    }

    @Contract(pure = true)
    public @NotNull String json() {
        return "";
    }

    public String toJson() {
        return "\t\t{\n" +
                "\t\"type\":\"ATK\",\n" +
                "\t\"id\":\"" + this.getId() + "\",\n" +
                "\t\"name\":\"" + this.getName() + "\",\n" +
                "\t\"level\":" + this.getLevel() + ",\n" +
                "\t\"experience\":" + this.getXP() + ",\n" +
                "\t\"cast_cost\":" + this.getCc() + ",\n" +
                "\t\"weight\":\"" + this.getWeight() + "\",\n" +
                json() +
                "}";
    }
}
