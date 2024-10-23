package org.tmmi;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.item.Type;

import java.util.*;

import static org.tmmi.Updator.getLastArrayItem;

public final class UIID {
    private final Map<Type, List<UIID>> uiids = new HashMap<>();
    private final UUID uuid;
    private final Type itemType;
    private final long creationDate;
    public UIID(Type type) {
        this.uuid = UUID.fromString("0afc45a7-0021-"+typeTo4Hex(type)+"0bac-c"+Long.toHexString(System.currentTimeMillis()));
        this.itemType = type;
        this.creationDate = 1;
    }
    public UIID(String uuid) {
        this(UUID.fromString(uuid));
    }
    public UIID(UUID uuid) {
        if (uuid == null)
            throw new RuntimeException("UUID cannot be null.");
        this.uuid = uuid;
        String[] splu = uuid.toString().split("-");
        this.itemType = getType(splu[3]);
        this.creationDate = Long.parseLong(getLastArrayItem(splu).substring(1, 12), 16);
    }

    @Contract(pure = true)
    public static @Nullable String typeTo4Hex(Type type) {
        return switch (type) {
            case BLACK_BACKGROUND -> "0a00";
            case FOCUS_WAND -> "0a01";
            case COOL_STICK -> "0a02";
            case CREDIT_COOKIE -> "0a03";
            case null -> null;
        };
    }

    @Contract(pure = true)
    public static @Nullable Type getType(String hex) {
        return switch (hex) {
            case "0a00" -> Type.BLACK_BACKGROUND;
            case "0a01" -> Type.FOCUS_WAND;
            case "0a02" -> Type.COOL_STICK;
            case "0a03" -> Type.CREDIT_COOKIE;
            case null, default -> null;
        };
    }

    public String toUUID() {
        return uuid.toString();
    }

    public long getCreationDate() {
        return creationDate;
    }

    public long getAge() {
        return System.currentTimeMillis()-creationDate;
    }
    public long getAgeRelativeTo(long date) {
        return date-creationDate;
    }
    public long getAgeRelativeTo(UIID uiid) {
        return uuid == null ? Long.MIN_VALUE : uiid.getCreationDate()-creationDate;
    }

    public boolean sameAs(UIID uiid) {
        return uiid != null && uiid.getUUID().equals(this.uuid);
    }

    public boolean sameAs(String uuid) {
        return this.uuid.toString().equals(uuid);
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uiids, uuid, itemType, creationDate);
    }

    public Type getItemType() {
        return itemType;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
