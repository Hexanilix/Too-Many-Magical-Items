package org.tmmi.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Item;
import org.tmmi.Main;
import org.tmmi.Spell;
import org.tmmi.WeavePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Wand extends Item {
    public static List<Wand> wands = new ArrayList<>();
    private int slot;
    private float power;
    private int level;
    private boolean isWeaving;
    private UUID handler;
    private int select_cooldown;
    private Spell selSpell;
    private Player activeUser = null;

    public Wand(UUID handler, int level, int power) {
        super(Material.STICK);
        ItemMeta fcM = this.getItemMeta();
        assert fcM != null;
        fcM.setDisplayName(ChatColor.GOLD + "Focus Wand");
        fcM.setCustomModelData(2140000+wands.size());
        this.setItemMeta(fcM);
        this.handler = handler;
        this.level = 1;
        this.isWeaving = false;
        this.select_cooldown = 0;
        this.power = 1;
    }
    public Wand(UUID handler) {
        this(handler, 1, 1);
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
        if (activeUser == null) {
            activeUser = Bukkit.getPlayer(handler);
        }
        if (activeUser != null) {
            if (activeUser.hasPermission(Main.permission)) {
                activeUser.sendMessage("Cant use this bruv");
                return;
            }
            WeavePlayer weaver = WeavePlayer.getWeaver(activeUser.getUniqueId());
            // selector of spell
            if (weaver != null) {
                if (weaver.hasGrandBook()) {
                    if (this.select_cooldown == 0) {
                        if (selSpell != null) {
                            weaver.cast(action);
                            selSpell = null;
                        }
                        if (action == Action.LEFT_CLICK_AIR) {

                        } else if (action == Action.RIGHT_CLICK_AIR) {

                        } else if (action == Action.LEFT_CLICK_BLOCK) {

                        } else if (action == Action.RIGHT_CLICK_BLOCK) {

                        }
                    }
                }
                weaver.cast(action);
            }
        }
    }

    @Override
    public void onDrop(PlayerDropItemEvent event) {
        this.activeUser = null;
    }

    @Override
    public void onPickup(@NotNull PlayerPickupItemEvent event) {
        this.activeUser = event.getPlayer();
        this.handler = activeUser.getUniqueId();
    }
}
