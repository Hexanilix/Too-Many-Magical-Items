package org.tmmi.spells.atributes;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum Weight {
    CANTRIP,
    SORCERY,
    INCANTATION;
    @Contract(pure = true)
    public static Weight getSpellType(@NotNull String type) {
        Weight s = null;
        switch (type) {
            case "CANTRIP" -> s = CANTRIP;
            case "SORCERY" -> s = SORCERY;
            case "AREA_EFFECT" -> s = INCANTATION;
        }
        return s;
    }
}
