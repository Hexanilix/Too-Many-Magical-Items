package org.tmmi.events;

import org.bukkit.entity.Player;
import org.tmmi.PlayerEvent;
import org.tmmi.OldSpell;

public class SpellCastEvent extends PlayerEvent {

    private final OldSpell spell;

    public SpellCastEvent(Player player, OldSpell spell) {
        super(player);
        this.spell = spell;
    }

    public OldSpell getSpell() {
        return spell;
    }
}