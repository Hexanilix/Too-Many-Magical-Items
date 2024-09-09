package org.tmmi.spell;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.tmmi.spell.atributes.Weight;

import java.util.UUID;

public abstract class STI extends Spell {
    private int effectTime;
    private double multiplier;
    public STI(UUID id, String name, Weight weight, int level, int XP, int cc, int time, double mul) {
        super(id, name, weight, null, level, XP, cc);
        this.effectTime = time;
        this.multiplier = mul;
    }

    public double getMultiplier() {
        return multiplier;
    }
    public void addMultiplier(double multiplier) {
        this.multiplier += multiplier;
    }

    public int getEffectTime() {
        return effectTime;
    }
    public void addEffectTime(int t) {
        effectTime += t;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String json() {
        return "\t\"effect_time\":" + this.effectTime + ",\n";
    }
}
