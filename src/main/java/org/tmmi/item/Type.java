package org.tmmi.item;

import org.jetbrains.annotations.Nullable;

public enum Type {
    BLACK_BACKGROUND,
    COOL_STICK,
    FOCUS_WAND,
    CREDIT_COOKIE;
    public static @Nullable Type getType(String type) {
        for (int i = 0; i < Type.values().length; i++) {
            Type t = Type.values()[i];
            if (t.name().equals(type)) return t;
        }
        return null;
    }
}
