package org.tmmi.events;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Element;
import org.tmmi.EntityMultiplier;
import org.tmmi.Main;
import org.tmmi.WeavePlayer;
import org.tmmi.block.*;
import org.tmmi.spell.Spell;

import java.util.Objects;

import static org.hetils.minecraft.Entity.Player.isCriticalHit;
import static org.hetils.minecraft.General.isSim;
import static org.hetils.minecraft.Item.setItemCoolDown;
import static org.hetils.minecraft.Location.nearestEntity;
import static org.hetils.minecraft.Material.SWORDS;
import static org.hetils.minecraft.Vector.vecAvg;
import static org.tmmi.Main.spellPerms;
import static org.tmmi.WeavePlayer.getOrNew;
import static org.tmmi.WeavePlayer.getWeaver;

public final class Listeners {
    public class BukkitListener implements Listener {



        @EventHandler
        public void onBow(@NotNull EntityShootBowEvent event) {
            if (event.getEntity() instanceof Player player) {
                new BukkitRunnable() {
                    LivingEntity liver = null;
                    final Arrow ar = (Arrow) event.getProjectile();

                    @Override
                    public void run() {
                        Location cl = ar.getLocation().clone();
                        cl.setYaw(-cl.getYaw());
                        cl.setPitch(-cl.getPitch());
                        double m = Math.pow(vecAvg(ar.getVelocity())*4, 2) / 4;
                        cl.add(cl.getDirection().multiply(m));
                        World w = ar.getLocation().getWorld();
                        if (w == null) return;
                        w.spawnParticle(Particle.COMPOSTER, cl, 1, 0, 0, 0,0);
                        liver = (nearestEntity(cl, 10, ar, event.getEntity()) instanceof LivingEntity e ? e : null);
                        liver = (nearestEntity(cl.add(cl.getDirection().multiply(m)), 5, ar, event.getEntity()) instanceof LivingEntity e ? e : liver);
                        w.spawnParticle(Particle.FLAME, cl, 1, 0, 0, 0,0);
                        if (ar.isOnGround()) cancel();
                        if (liver != null && !liver.isDead()) {
                            if (ar.getLocation().distance(liver.getLocation()) < 1) cancel();
                            Vector direction = liver.getLocation().toVector().subtract(ar.getLocation().toVector()).normalize();
                            ar.setVelocity(ar.getVelocity().add(direction.multiply(Math.cbrt(m*((Math.pow(ar.getLocation().distance(liver.getLocation())/4, 2)/10)+0.2)))));
                        }
                    }
                }.runTaskTimer(main, 0, 1);
            }
        }

        @EventHandler
        public void onEntityDmg(@NotNull EntityDamageEvent event) {
            Entity de = event.getEntity();
            if (de instanceof Player p) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    WeavePlayer w = WeavePlayer.getWeaver(p);
                    if (w != null && w.getElement() == Element.AIR) {
                        event.setCancelled(true);
                        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 0.2, 0), 3, 0.1, 0, 0.1, Math.sqrt(event.getDamage())/25);
                    }
                }
            }
            Entity ce = event.getDamageSource().getCausingEntity();
            if (ce != null) {
                EntityMultiplier em = EntityMultiplier.getOrNew(ce);
                event.setDamage(event.getDamage()*em.getDmg());
            }
        }
        //        @EventHandler
//        public void onPlayerItemHeld(@NotNull PlayerItemHeldEvent event) {
//            Player player = event.getPlayer();
//            WeavePlayer weaver = getWeaver(player);
//            if (weaver != null) {
//                if (weaver.isWeaving()) {
//                    int nxt = event.getNewSlot();
//                    int move = (weaver.getWandSlot() - nxt > 0 ? 1 : -1);
//                }
//            }
//        }

    }
}
