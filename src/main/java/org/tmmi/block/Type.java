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
    WEAVINGTABLE;

    @Contract(pure = true)
    public static @Nullable Type getType(@NotNull String s) {
        switch (s.toUpperCase()) {
            case "CRAFTINGCAULDRON" -> {
                return CRAFTINGCAULDRON;
            }
            case "FORCEFIELD" -> {
                return FORCEFIELD;
            }
            case "SPELLSUCKER" -> {
                return SPELLSUCKER;
            }
            case "PRESENCEDETECTOR" -> {
                return PRESENCEDETECTOR;
            }
            case "SPELLWEAVER" -> {
                return SPELLWEAVER;
            }
            case "WEAVINGTABLE" -> {
                return WEAVINGTABLE;
            }
        }
        return null;
    }
}
