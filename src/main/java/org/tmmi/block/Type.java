package org.tmmi.block;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Type {
    CRAFTING_CAULDRON,
    FORCE_FIELD,
    SPELL_SUCKER,
    PRESENCE_DETECTOR,
    SPELL_WEAVER,
    WEAVING_TABLE;

    @Contract(pure = true)
    public static @Nullable Type getType(@NotNull String s) {
        switch (s.toUpperCase()) {
            case "CRAFTING_CAULDRON" -> {
                return CRAFTING_CAULDRON;
            }
            case "FORCE_FIELD" -> {
                return FORCE_FIELD;
            }
            case "SPELL_SUCKER" -> {
                return SPELL_SUCKER;
            }
            case "PRESENCE_DETECTOR" -> {
                return PRESENCE_DETECTOR;
            }
            case "SPELL_WEAVER" -> {
                return SPELL_WEAVER;
            }
        }
        return null;
    }
}
