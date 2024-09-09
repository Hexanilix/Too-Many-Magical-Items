package org.tmmi.spell.spells;

import org.bukkit.*;
import org.bukkit.entity.*;
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
import java.util.Random;

import static org.hetils.Util.*;
import static org.hetils.Vector.genVec;
import static org.tmmi.Main.*;

public class MagicMissile extends ATK {

    public MagicMissile() {
        super(null, "Magic Missile", Weight.INCANTATION, 1, 0, 10, Element.AIR, null, AreaEffect.DIRECT, 10, 35, 2, false);
    }

    @Override
    public CastSpell cast(@NotNull Location castLocation, float multiplier, Entity e) {
        Location loc = castLocation.clone();
        Entity target = null;
        Location lab = null;
        int o = 3;
        while (o <= 40 && target == null) {
            Location lo = loc.clone().add(loc.getDirection().normalize().multiply(o));
            lab = lo;
            if (lo.getBlock().getType().isSolid()) break;
            for (Entity en : loc.getWorld().getNearbyEntities(lo, 1, 1, 1)) {
                if (en instanceof LivingEntity l && l.hasAI()) {
                    target = l;
                    break;
                }
            }
            o++;
        }
        Entity finalTarget = target;
        Location finalLab = lab;
        return new CastSpell(this, loc, getCc()) {
            @Override
            public Thread cast() {
                return newThread(new Thread() {
                    final double dmg = MagicMissile.this.getBaseDamage();
                    @Override
                    public void interrupt() {
//                            uncast();
                        MagicMissile.this.attemptLvlUP();
                        super.interrupt();
                    }
                    final Entity tar = finalTarget;
                    final World w = loc.getWorld();
                    final Random r = new Random();
                    boolean hit = false;
                    @Override
                    public void run() {
                        try {
                            Location hl = tar == null ? finalLab : tar.getLocation().add(0, tar.getHeight()/2, 0);
                            loc.setYaw(loc.getYaw()+r.nextInt(360)-180);
                            loc.setPitch(-r.nextInt(95)+5);
                            loc.add(loc.getDirection().normalize().multiply(0.3));
                            double dis = 0;
                            int i = 0;
                            while (!hit) {
                                Location ll = loc.clone();
                                i++;
                                if (i % 25 == 0) hl = tar == null ? finalLab : tar.getLocation().add(0, tar.getHeight()/2, 0);
                                loc.setDirection(loc.getDirection().clone().add(genVec(loc, hl).multiply(.1)));
                                loc.add(loc.getDirection().clone().normalize().multiply(.25));
                                w.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
                                Location finalHl = hl;
                                if (i % 5 == 0)
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            Collection<Entity> nearbyEntities = w.getNearbyEntities(loc, 1, 1, 1);
                                            for (Entity en : nearbyEntities) if (en instanceof LivingEntity l && en != tar) {
                                                    if (en == e) return;
                                                    Spell.damageEnt(l, dmg / (l.getLocation().distance(loc) * 2));
                                                    w.spawnParticle(Particle.CLOUD, l.getLocation(), 2, 1, 1, 1, 0.05);
                                                    l.setVelocity(l.getVelocity().add(genVec(l.getLocation().add(0, l.getHeight()/2, 0), loc).normalize().multiply(-0.1)));
                                                    hit = true;
                                                }
                                            if (finalHl.distance(loc) <= 1 && !hit && tar != null && tar instanceof LivingEntity liv) {
                                                Spell.damageEnt(liv, dmg);
                                                hit = true;
                                                liv.setVelocity(liv.getVelocity().add(genVec(liv.getLocation().add(0, liv.getHeight()/2, 0), loc).normalize().multiply(-0.2)));
                                            }
                                            w.playSound(loc, Sound.BLOCK_COPPER_BULB_HIT, 1, 1);
                                        }
                                    }.runTask(plugin);
                                if (dis >= getTravel() || loc.getBlock().getType().isSolid()) break;
                                dis += loc.distance(ll);
                                Thread.sleep(10);
                            }
                            MagicMissile.this.attemptLvlUP();
                        } catch (InterruptedException ignored) {}
                    }
                });
            }
        };
    }
}
