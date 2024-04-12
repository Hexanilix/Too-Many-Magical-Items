package org.tmmi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Spell {
    public static List<Spell> spells = new ArrayList<>();

    private final UUID handler;
    private final SpellBase base;

    Spell(UUID handler, SpellBase base) {
        this.handler = handler;
        this.base = base;
        spells.add(this);
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
                ", base=" + base.toString() +
                '}';
    }
}
