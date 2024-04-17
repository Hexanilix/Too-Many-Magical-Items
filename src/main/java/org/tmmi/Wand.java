package org.tmmi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class Wand extends InteractiveItem {
    private int slot;
    private float power;
    private int level;
    private boolean isWeaving;
    private UUID handler;
    private int select_cooldown;
    private Spell selSpell;
    private Player activeUser = null;

    public Wand(ItemStack item, UUID handler, int level, int power) {
        super(item);
        this.handler = handler;
        this.level = 1;
        this.isWeaving = false;
        this.select_cooldown = 0;
        this.power = 1;
    }
    public Wand(ItemStack item, UUID handler) {
        this(item, handler, 1, 1);
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

    @Override
    public void onUse(Action action) {
        if (activeUser == null) {
            activeUser = Bukkit.getPlayer(handler);
        }
        if (activeUser != null) {
            if (activeUser.hasPermission(Main.permission)) {
                activeUser.sendMessage("Cant use this bruv");
                return;
            }
            WeavePlayer weaver = WeavePlayer.getWeaver(activeUser);
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
    public void onDrop() {
        this.activeUser = null;
    }

    @Override
    public void onPickup(Player p) {
        this.activeUser = p;
        this.handler = activeUser.getUniqueId();
    }
}
