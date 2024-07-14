package org.tmmi.spell.atributes;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Weight {
    CANTRIP,
    SORCERY,
    INCANTATION;
    @Contract(pure = true)
    public static @Nullable Weight getSpellType(@NotNull String type) {
        return switch (type) {
            case "CANTRIP" -> CANTRIP;
            case "SORCERY" -> SORCERY;
            case "INCANTATION" -> INCANTATION;
            default -> null;
        };
    }
}
