package org.tmmi;

import org.bukkit.entity.Player;

import java.util.UUID;

public class Spell {
    private final UUID handler;
    private final SpellBase base;

    Spell(UUID handler, SpellBase base) {
        this.handler = handler;
        this.base = base;
        new SpellBase("spell", SpellBase.CastAreaEffect.DIRECT, SpellBase.MainElement.FIRE);
    }

    public UUID getHandler() {
        return handler;
    }

    public SpellBase base() {
        return base;
    }

    @Override
    public String toString() {
        return "Spell{" +
                "handler=" + handler +
                ", base=" + base +
                '}';
    }
}
