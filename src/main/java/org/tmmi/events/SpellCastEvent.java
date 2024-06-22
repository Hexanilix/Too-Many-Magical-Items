package org.tmmi.events;

import org.bukkit.entity.Player;
import org.tmmi.spells.Spell;

public class SpellCastEvent extends PlayerEvent {

    private final Spell spell;

    public SpellCastEvent(Player player, Spell spell) {
        super(player);
        this.spell = spell;
    }

    public Spell getSpell() {
        return spell;
    }
}