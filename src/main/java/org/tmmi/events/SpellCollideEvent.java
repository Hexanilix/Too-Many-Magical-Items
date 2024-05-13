package org.tmmi.events;

import org.bukkit.entity.Player;
import org.tmmi.PlayerEvent;
import org.tmmi.Spells.Spell;

import java.util.UUID;

public class SpellCollideEvent extends PlayerEvent {
    private final Spell hitSpell;
    private final Spell hittingSpell;
    public SpellCollideEvent(Player player, Spell hitSpell, Spell hittingSpell) {
        super(player);
        this.hitSpell = hitSpell;
        this.hittingSpell = hittingSpell;
    }

    public Spell getHittingSpell() {
        return this.hittingSpell;
    }

    public Spell getHitSpell() {
        return this.hitSpell;
    }

    public UUID getHitter() {
        return this.hittingSpell.getHandler();
    }

    public UUID getHited() {
        return this.hitSpell.getHandler();
    }
}
