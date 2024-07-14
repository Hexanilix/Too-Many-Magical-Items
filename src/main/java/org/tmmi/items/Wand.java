package org.tmmi.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Main;
import org.tmmi.spell.Spell;
import org.tmmi.WeavePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.tmmi.Main.log;

public abstract class Wand extends Item {
    public static List<Wand> wands = new ArrayList<>();
    private int slot;
    private float power;
    private int level;
    private boolean isWeaving;
    private int select_cooldown;
    private Spell selSpell;

    public Wand(UUID id, int level, int power) {
        super(Material.STICK);
        ItemMeta fcM = this.getItemMeta();
        assert fcM != null;
        fcM.setDisplayName(ChatColor.GOLD + "Focus Wand");
        fcM.setCustomModelData(2140000+wands.size());
        this.setItemMeta(fcM);
        this.level = 1;
        this.isWeaving = false;
        this.select_cooldown = 0;
        this.power = 1;
    }
    public Wand() {
        this(null, 1, 1);
    }
    public Wand(UUID id) {
        this(id, 1, 1);
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public boolean isWeaving() {
        return this.isWeaving;
    }

    public float getPower() {
        return this.power;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void addLevel(int lvl) {
        this.level += lvl;
    }

    @Override
    public void onUse(@NotNull PlayerInteractEvent event) {
        Action action = event.getAction();
        if (Main.spellPerms.getOrDefault(event.getPlayer().getUniqueId(), false)) {
            event.getPlayer().sendMessage("Cant use this bruv");
            return;
        }
        WeavePlayer weaver = WeavePlayer.getWeaver(event.getPlayer());
        // selector of spell
        if (weaver != null) {
            if (weaver.hasGrandBook()) {
                if (this.select_cooldown == 0) {
                    if (selSpell != null) {
                        weaver.cast();
                        selSpell = null;
                    }
                    if (action == Action.LEFT_CLICK_AIR) {

                    } else if (action == Action.RIGHT_CLICK_AIR) {

                    } else if (action == Action.LEFT_CLICK_BLOCK) {

                    } else if (action == Action.RIGHT_CLICK_BLOCK) {

                    }
                }
            }
            weaver.cast();
        }
    }

    @Override
    public void onDrop(PlayerDropItemEvent event) {

    }

    @Override
    public void onPickup(@NotNull PlayerPickupItemEvent event) {
    }
}
