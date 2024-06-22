package org.tmmi.spells.atributes;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Type {
    ATK,
    DEF,
    PAS,
    STI,
    UTL;

    @Contract(pure = true)
    public static @Nullable Type getType(@NotNull String s) {
        return switch (s) {
            case "ATK" -> ATK;
            case "DEF" -> DEF;
            case "PAS" -> PAS;
            case "STI" -> STI;
            case "UTL" -> UTL;
            default -> null;
        };
    }
}
