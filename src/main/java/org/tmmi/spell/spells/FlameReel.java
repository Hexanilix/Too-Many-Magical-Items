package org.tmmi.spell.spells;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Element;
import org.tmmi.spell.ATK;
import org.tmmi.spell.CastSpell;
import org.tmmi.spell.Spell;
import org.tmmi.spell.atributes.AreaEffect;
import org.tmmi.spell.atributes.Weight;

import java.util.Collection;
import java.util.HashSet;

import static org.hetils.Vector.genVec;
import static org.tmmi.Main.*;

public class FlameReel extends ATK {
    public FlameReel() {
        super(null, "Flame Cast", Weight.CANTRIP, 1, 0, 10, Element.FIRE, null, AreaEffect.WIDE, 1, 30, 20, false);
    }

    @Override
    public CastSpell cast(@NotNull Location castLocation, float multiplier, Entity e) {
        return new CastSpell(this, castLocation, getCc()) {
            @Override
            public Thread cast() {
                return newThread(new Thread() {
                    final World w = castLocation.getWorld();
                    boolean shoot = false;
                    private class Lis implements Listener {
                        @EventHandler
                        public void onRck(@NotNull PlayerInteractEvent event) {
                            if (event.getAction() == Action.LEFT_CLICK_AIR)
                                if (!shoot) {
                                    shoot = true;
                                    HandlerList.unregisterAll(this);
                                }
                        }
                        @EventHandler
                        public void onEntityDamageByEntity(@NotNull ProjectileHitEvent event) {
                            if (event.getEntity() == ent && event.getHitEntity() != null) ent = event.getHitEntity();
                        }
                    }
                    Entity ent = w.spawnEntity(castLocation, EntityType.SNOWBALL);
                    @Override
                    public void run() {
                        ent.setVelocity(genVec(castLocation, castLocation.clone().add(castLocation.getDirection())).normalize().multiply(2));
                        Bukkit.getPluginManager().registerEvents(new Lis(), plugin);
                        try {
                            while (!shoot) {
                                Thread.sleep(50);
                                Location l = e.getLocation().add(0, 1.2, 0);
                                Location el = ent.getLocation().add(0, ent.getHeight()/2, 0);
                                Vector direction = l.toVector().subtract(el.toVector());
                                Vector step = direction.multiply(.5 / -l.distance(ent.getLocation()));
                                for (int i = 0; i <= l.distance(el)*2; i++)
                                    l.getWorld().spawnParticle(Particle.ASH, l.clone().add(step.clone().multiply(i)), 1, 0, 0, 0, 0);
                                w.spawnParticle(Particle.COMPOSTER, ent.getLocation().add(0, ent.getHeight()/2, 0), 5, .05, .05, .05, 0);
                            }
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    e.setVelocity(e.getVelocity().add(genVec(e.getLocation(), ent.getLocation()).multiply(-2)));
                                    Location l = e.getLocation().add(0, 1.2, 0);
                                    Collection<LivingEntity> dme = new HashSet<>();
                                    dme.add((LivingEntity) e);
                                    Vector direction = l.toVector().subtract(ent.getLocation().add(0, ent.getHeight()/2, 0).toVector());
                                    double st = l.distance(ent.getLocation().add(0, ent.getHeight()/2, 0));
                                    Vector step = direction.multiply(.5 / -st);
                                    int i = 0;
                                    while (i < st*2) {
                                        Location lo = l.clone().add(step.clone().multiply(i));
                                        if (lo.getBlock().getType().isSolid()) break;
                                        w.spawnParticle(Particle.DRIPPING_LAVA, lo, 5, .1, .1, .1, .1);
                                        w.spawnParticle(Particle.FLAME, lo, 10, .15, .15, .15, 0.02);
                                        for (Entity e : w.getNearbyEntities(lo, .4, .4, .4))
                                            if (e instanceof LivingEntity li)
                                                if (!dme.contains(li)) {
                                                    Spell.damageEnt(li, getBaseDamage() / (li.getLocation().distance(l)));
                                                    dme.add(li);
                                                }
                                        i++;
                                    }
                                    w.spawnParticle(Particle.LAVA, l.clone().add(step.clone().multiply(i)), 250, 1, 1, 1, 0.3);
                                    for (Entity e : w.getNearbyEntities(l.clone().add(step.clone().multiply(i)), 1.5, 1.5, 1.5)) {
                                        if (e instanceof LivingEntity li)
                                            Spell.damageEnt(li, getBaseDamage());
                                    }
                                }
                            }.runTask(plugin);
                            if (!ent.isDead()) new BukkitRunnable() {
                                @Override
                                public void run() {
                                    ent.remove();
                                }
                            };
                        } catch (InterruptedException ignored) {}
                    }
                });
            }
        };
    }
}
