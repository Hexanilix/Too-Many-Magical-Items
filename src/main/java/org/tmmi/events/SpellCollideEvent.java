package org.tmmi.events;

import org.bukkit.entity.Player;
import org.tmmi.spells.CastSpell;

import java.util.UUID;

public class SpellCollideEvent extends PlayerEvent {
    private final CastSpell hitSpell;
    private final CastSpell hittingSpell;
    public SpellCollideEvent(Player player, CastSpell hitSpell, CastSpell hittingSpell) {
        super(player);
        this.hitSpell = hitSpell;
        this.hittingSpell = hittingSpell;
    }

    public CastSpell getHittingSpell() {
        return this.hittingSpell;
    }

    public CastSpell getHitSpell() {
        return this.hitSpell;
    }

}
