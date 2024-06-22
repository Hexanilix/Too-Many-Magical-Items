package org.tmmi.block;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Type {
    CRAFTINGCAULDRON,
    FORCEFIELD,
    SPELLSUCKER,
    PRESENCEDETECTOR,
    SPELLWEAVER,
    WEAVINGTABLE,
    MANACAULDRON;
    @Contract(pure = true)
    public static @Nullable Type getType(@NotNull String s) {
        return switch (s.toUpperCase()) {
            case "CRAFTINGCAULDRON" -> CRAFTINGCAULDRON;
            case "FORCEFIELD" -> FORCEFIELD;
            case "SPELLSUCKER" -> SPELLSUCKER;
            case "PRESENCEDETECTOR" -> PRESENCEDETECTOR;
            case "SPELLWEAVER" -> SPELLWEAVER;
            case "WEAVINGTABLE" -> WEAVINGTABLE;
            case "MANACAULDRON" -> MANACAULDRON;
            default -> null;
        };
    }
}
