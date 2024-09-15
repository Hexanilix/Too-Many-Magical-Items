package org.tmmi.spell.spells;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.tmmi.EntityMultiplier;
import org.tmmi.spell.CastSpell;
import org.tmmi.spell.STI;
import org.tmmi.spell.atributes.Weight;

import java.util.List;

import static org.hetils.minecraft.Location.nearestEntity;
import static org.tmmi.Main.newThread;
import static org.tmmi.Main.plugin;

public class AthenasRage extends STI {
    public AthenasRage() {
        super(null, "Athenas Rage", Weight.CANTRIP, 1, 0, 10, 5000, 1.2);
    }
    @Override
    public CastSpell cast(Location loc, float multiplier, Entity e) {
        double mul = getMultiplier() * multiplier;
        for (int i = 0; i < 20; i++) {
            loc.add(loc.getDirection());
            Entity ne = nearestEntity(loc, 0.25, e);
            if (ne != null) {
                EntityMultiplier em = EntityMultiplier.getOrNew(ne);
                em.addDmg(mul);
                BukkitTask task = new BukkitRunnable() {
                    final World w = ne.getWorld();
                    @Override
                    public void run() {
                        w.spawnParticle(Particle.COMPOSTER,
                                ne.getLocation().add(0, ne.getLocation().getY() - ne.getBoundingBox().getCenterY() + (ne.getBoundingBox().getHeight()), 0),
                                5, ne.getBoundingBox().getWidthX()/4, ne.getBoundingBox().getHeight()/4, ne.getBoundingBox().getWidthZ()/4, 0.1);
                    }
                }.runTaskTimer(plugin, 0, 0);
                newThread(() -> {
                    try {Thread.sleep(getEffectTime());
                    } catch (InterruptedException ignored) {}
                    ne.setGlowing(false);
                    em.subDmg(mul);
                    task.cancel();
                }).start();
                break;
            }
        }
        return null;
    }

    @Override
    public void onLevelUP() {

    }
}
