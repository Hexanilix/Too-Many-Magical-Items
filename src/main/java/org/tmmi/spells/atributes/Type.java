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
        switch (s) {
            case "ATK" -> {
                return ATK;
            }
            case "DEF" -> {
                return DEF;
            }
            case "PAS" -> {
                return PAS;
            }
            case "STI" -> {
                return STI;
            }
            case "UTL" -> {
                return UTL;
            }
        }
        return null;
    }
}
