package org.tmmi.spell.spells;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.NotNull;
import org.tmmi.spell.CastSpell;
import org.tmmi.spell.STI;
import org.tmmi.spell.atributes.Weight;

import static org.tmmi.Main.*;

public class AphrodytiesBlessing extends STI {
    public AphrodytiesBlessing() {
        super(null, "Aphrodyties Blessing", Weight.CANTRIP, 1, 0, 10, 15000, 1.2);
    }
    private static class noAtk implements Listener {
        final Entity p;
        public noAtk(Entity p, int time) {
            this.p = p;
            newThread(() -> {
                try {Thread.sleep(time);
                } catch (InterruptedException ignore) {}
                HandlerList.unregisterAll(this);
            }).start();
        }
        @EventHandler
        public void onEntityTarget(@NotNull EntityTargetEvent event) {
            if (event.getTarget() == p)
                if (event.getEntity() instanceof Monster e) {
                    e.getWorld().spawnParticle(Particle.HEART, e.getLocation().add(0, e.getHeight(), 0), 1, .5, .5, .5, 0);
                    event.setCancelled(true);
                }
        }
    }
    @Override
    public CastSpell cast(Location loc, float multiplier, @NotNull Entity e) {
        for (Entity en : e.getWorld().getEntities()) {
            if (en instanceof Monster em && em.getTarget() == e) {
                em.setTarget(null);
            }
        }
        Bukkit.getPluginManager().registerEvents(new noAtk(e, getEffectTime()), plugin);
        return null;
    }

    @Override
    public void onLevelUP() {

    }
}
