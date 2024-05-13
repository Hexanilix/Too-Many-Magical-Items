package org.tmmi;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityMultiplier {
    private UUID e;
    private double dmg;
    private double rsp;
    private double rtd;
    public static List<EntityMultiplier> instances = new ArrayList<>();
    EntityMultiplier(UUID e, double dmg, double rsp, double rtd) {
        this.e = e;
        this.dmg = dmg;
        this.rsp = rsp;
        this.rtd = rtd;
    }
    EntityMultiplier(@NotNull Entity e) {
        this(e.getUniqueId(), 1, 1, 1);
    }

    public static @Nullable EntityMultiplier get(Entity ce) {
        for (EntityMultiplier em : instances)
            if (em.getEntity() == ce.getUniqueId())
                return em;
        return null;
    }

    public static @NotNull EntityMultiplier getOrNew(Entity ce) {
        EntityMultiplier em = get(ce);
        if (em != null) return em;
        else return new EntityMultiplier(ce);
    }

    public void setDmg(double dmg) {
        this.dmg = dmg;
    }

    public void setRsp(double rsp) {
        this.rsp = rsp;
    }

    public void setRtd(double rtd) {
        this.rtd = rtd;
    }

    public void addDmg(double dmg) {
        this.dmg += dmg;
    }

    public void addRsp(double rsp) {
        this.rsp += rsp;
    }

    public void addRtd(double rtd) {
        this.rtd += rtd;
    }

    public double getDmg() {
        return dmg;
    }

    public double getRsp() {
        return rsp;
    }

    public double getRtd() {
        return rtd;
    }

    public UUID getEntity() {
        return e;
    }
}
