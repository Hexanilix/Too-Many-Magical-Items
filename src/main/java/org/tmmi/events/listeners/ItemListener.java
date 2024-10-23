package org.tmmi.events.listeners;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.hetils.minecraft.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.GUI;
import org.tmmi.Main;
import org.tmmi.WeavePlayer;
import org.tmmi.block.InteractiveBlock;
import org.tmmi.spell.Spell;

import java.util.Objects;
import java.util.UUID;

import static org.hetils.minecraft.Entity.Player.isCriticalHit;
import static org.hetils.minecraft.General.isSim;
import static org.hetils.minecraft.Item.setItemCoolDown;
import static org.hetils.minecraft.Material.SWORDS;
import static org.tmmi.Main.log;
import static org.tmmi.WeavePlayer.getOrNew;
import static org.tmmi.WeavePlayer.getWeaver;

public class ItemListener implements Listener {
    private final Main main;

    public ItemListener(Main main) {
        this.main = main;
    }

    private boolean isSword(ItemStack item) {
        return item != null && item.getType().isItem() && switch (item.getType()) {
            case WOODEN_SWORD, STONE_SWORD, GOLDEN_SWORD, IRON_SWORD, DIAMOND_SWORD, NETHERITE_SWORD -> true;
            default -> false;
        };
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                PersistentDataContainer data = meta.getPersistentDataContainer();
                if (data.has(org.tmmi.item.Item.ITEM_UIID))
                    org.tmmi.item.Item.get(UUID.fromString(data.get(org.tmmi.item.Item.ITEM_UIID, PersistentDataType.STRING)));
            }
        }
        if (event.getClickedBlock() != null) {
            for (InteractiveBlock b : InteractiveBlock.instances)
                if (b.getLoc().getBlock().equals(event.getClickedBlock().getLocation().getBlock())) {
                    b.onClick(event.getAction(), event.getPlayer(), event);
                    return;
                }
        }
        Player p = event.getPlayer();
        //Code for sword shielding
        if (isSword(event.getItem()) && event.getAction() == Action.RIGHT_CLICK_AIR) {
            new BukkitRunnable() {
                boolean cancelled = false;
                public class Listener implements org.bukkit.event.Listener {
                    @EventHandler
                    public void onClick(@NotNull PlayerInteractEvent event1) {
                        if (event1.getPlayer() == p && !cancelled) {
                            cancel();
                            a.remove();
                            cancelled = true;
                        }
                    }
                    @EventHandler
                    public void onClick(@NotNull PlayerInteractAtEntityEvent event1) {
                        if (event1.getRightClicked() == a && event1.getPlayer() == p && !cancelled) {
                            cancel();
                            a.remove();
                            cancelled = true;
                        }
                    }
                    @EventHandler
                    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
                        if (event.getEntity() == a && event.getDamager() == p && !cancelled) {
                            cancel();
                            a.remove();
                            cancelled = true;
                        } else if ((event.getEntity() == p || event.getEntity() == a)
                                && event.getDamager() instanceof Player damager) {
                            if (isEntityBehindPlayer(p, damager)) return;
                            if (sp == 0 && !isCriticalHit(event)) return;
                            else if (sp == 1 && isCriticalHit(event)) return;
                            else if (sp == 2 && damager.getLocation().getY() > p.getLocation().getY()-1) return;
                            cancelled = true;
                            event.setCancelled(true);
                            cancel();
                            a.getWorld().playSound(a.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1.9f);
                            damager.setVelocity(damager.getVelocity().add(damager.getLocation().toVector().subtract(p.getLocation().toVector())).multiply(.2));
                            damager.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 1, 3));
                            setItemCoolDown(damager, SWORDS, 10);
                            setItemCoolDown(p, SWORDS, 20);
                        }
                    }
                    public static boolean isEntityBehindPlayer(@NotNull Player player, @NotNull Entity entity) {
                        Vector playerDirection = player.getLocation().getDirection();
                        Vector playerToEntity = entity.getLocation().toVector().subtract(player.getLocation().toVector());
                        playerToEntity.normalize();
                        double dotProduct = playerDirection.dot(playerToEntity);
                        return dotProduct < 0;
                    }
                    @EventHandler
                    public void onPlayerItemHeld(@NotNull PlayerItemHeldEvent event) {
                        if (event.getPlayer() == p) {
                            sp = Math.max(Math.min(sp + (event.getNewSlot() - event.getPreviousSlot()), 2), 0);
                            log(sp);
                            a.setRightArmPose(ang[sp]);
                            event.setCancelled(true);
                        }
                    }
                }
                int sp = 0;
                final Player p = event.getPlayer();
                final ArmorStand a = (ArmorStand) p.getWorld().spawnEntity(p.getLocation(), EntityType.ARMOR_STAND);
                final ItemStack it = event.getItem();
                final Listener l = new Listener();
                final EulerAngle[] ang = new EulerAngle[]{
                        new EulerAngle(4.4, .6, 1.3),
                        new EulerAngle(4.6, .3, 2.3),
                        new EulerAngle(-.2, -1.5, .5)
                };
                {
                    a.setVisible(false);
                    a.setGravity(false);
                    a.setInvisible(true);
                    a.getEquipment().setItemInMainHand(it);
                    a.setRightArmPose(ang[0]);
                    a.getWorld().playSound(a.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1, .9f);
                    Bukkit.getPluginManager().registerEvents(l, main);
                }
                final int s = p.getInventory().getHeldItemSlot();
                public void run() {
                    Vector v = p.getVelocity();
                    v.setX(Math.min(0.3, v.getX()));
                    v.setZ(Math.min(0.3, v.getZ()));
                    Location pl = p.getLocation();
//                        if (!a.isDead()) a.teleport(new Location(p.getWorld(), pl.getX()+Math.log(v.getX()*10), pl.getY()+v.getY(), pl.getZ()+Math.log(v.getZ()*10), pl.getYaw(), pl.getPitch()));
                    if (!a.isDead()) a.teleport(pl);
                    p.setVelocity(v);
                }
                @Override
                public void cancel() {
                    super.cancel();
                    p.getInventory().setItem(s, it);
                    HandlerList.unregisterAll(l);
                }
            }.runTaskTimer(main, 0, 0);
            p.getInventory().setItemInMainHand(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        if (event.getAction() == Action.LEFT_CLICK_AIR  && p.getInventory().getItemInMainHand().getType() == Material.AIR) {
//                Location l = p.getEyeLocation().clone().subtract(0, 0.5, 0);
//                for (int i = 0; i < 12; i++) {
//                    Entity e = nearestEntity(l.add(l.getDirection().multiply(1)), 0.5);
////                    if (e != p && e instanceof LivingEntity liv) {
////                        BoundingBox b = liv.getBoundingBox();
////                        log((b.getMinX() - b.getMaxX()));
////                        log(new Location(liv.getWorld(), b.getMinX() +  ((b.getMinX() - b.getMaxX())/10) , b.getMinY() + ((b.getMinY() - b.getMaxY())/10) , b.getMinZ() + ((b.getMinZ() - b.getMaxZ())/10)));
////                        for (int j = -2; j < 12; j++) {
////                            Location locer = new Location(liv.getWorld(),
////                                    b.getMinX() - (((b.getMinX() - b.getMaxX())/10) * j),
////                                    b.getMinY() - (((b.getMinY() - b.getMaxY())/10) * j),
////                                    b.getMinZ() - (((b.getMinZ() - b.getMaxZ())/10) * j));
////                            locer.getWorld().spawnParticle(Particle.FLAME, locer, 1, 0, 0, 0, 0);
////                        }
////                        liv.damage(2);
////                        liv.setFireTicks(20);
////                        break;
////                    }
//                    if (e != p && e instanceof LivingEntity liv) {
//                        liv.getWorld().spawnParticle(Particle.CLOUD, liv.getLocation(), 5, 0.2, 0.2, 0.2, 0.1);
//                        liv.damage(0.2);
//                        liv.setVelocity(liv.getVelocity().subtract(genVec(liv.getLocation(), p.getLocation())).normalize().multiply(0.7));
//                        break;
//                    }
//                }
            WeavePlayer w = getOrNew(p);
            log(w.getMain());
            if (w.getMain() != null) w.cast();
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && p.getInventory().getItemInMainHand().getType() == Material.AIR) {
//                org.bukkit.block.Block b = p.getTargetBlockExact(5);
//                if (b != null) b.getWorld().spawnParticle(Particle.CLOUD, b.getLocation().add(0.5, 1, 0.5), 3, 0.1, 0, 0.1, 0.04);
//                p.setVelocity(p.getVelocity().add(p.getEyeLocation().getDirection().multiply(-0.3d)));
        }
    }
    @EventHandler
    public void onItemPickup(@NotNull PlayerPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (!item.hasItemMeta()) return;
        if (!Objects.requireNonNull(item.getItemMeta()).hasCustomModelData()) return;
        for (org.tmmi.item.Item i : org.tmmi.item.Item.items)
            if (isSim(item, i))
                i.onPickup(event);
    }
    @EventHandler
    public void onItemPickup(@NotNull PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (!item.hasItemMeta()) return;
        if (!Objects.requireNonNull(item.getItemMeta()).hasCustomModelData()) return;
        for (org.tmmi.item.Item i : org.tmmi.item.Item.items)
            if (isSim(item, i))
                i.onDrop(event);
    }
    //        @EventHandler
//        public void onPickup(@NotNull PlayerPickupItemEvent event) {
//            this.activeUser = event.getPlayer();
//            this.handler = activeUser.getUniqueId();
//            this.setSlot(Main.getItemSlot(this.getItem(), event.getPlayer().getInventory()));
//        }

    public static final NamespacedKey GUI_TYPE = new NamespacedKey(Main.name, "gui_type");
    public enum GUIType {
        Pres;
        public static final int l = GUIType.values().length;
        public static @Nullable GUIType getType(String type) {
            for (int i = 0; i < l; i++) {
                GUIType t = GUIType.values()[i];
                if (t.name().equals(type)) return t;
            }
            return null;
        }
    }
    public static GUIType getGUITypeFromItem(ItemStack item) {
        GUIType t = null;
        if (item == null || !item.hasItemMeta()) return t;
        ItemMeta m = item.getItemMeta();
        PersistentDataContainer c = m.getPersistentDataContainer();
        if (!c.has(GUI_TYPE)) return t;
        t= GUIType.getType(c.get(GUI_TYPE, PersistentDataType.STRING));
        return t;
    }

    @EventHandler
    public void invClick(@NotNull InventoryClickEvent event) {
        if (event.getClickedInventory() != null) {
            GUIType guiType = getGUITypeFromItem(event.getClickedInventory().getItem(0));

            ItemMeta meta = ci.getItemMeta();
            if (meta == null) return;
            PersistentDataContainer data = meta.getPersistentDataContainer();
            if (main.allItemInv.contains(event.getClickedInventory())) {
                if (ci != null) event.getWhoClicked().getInventory().addItem(ci);
                event.setCancelled(true);
            } else {
                WeavePlayer weaver = getWeaver(event.getWhoClicked());
                if (weaver != null && isSim(event.getClickedInventory(), weaver.inventory())) {
                    event.setCancelled(true);
                    boolean us = event.getClick() == ClickType.LEFT;
                    ItemStack mi = us ? (weaver.getMain() == null ? null : weaver.getMain().toItem()) :
                            (weaver.getSecondary() == null ? null : weaver.getSecondary().toItem());
                    for (Spell s : weaver.getSpells()) {
                        if (isSim(ci, mi)) {
                            if (us) weaver.setMain(null);
                            else weaver.setSecondary(null);
                        } else if (isSim(ci, s.toItem())) {
                            if (mi != null)
                                for (ItemStack itemStack : event.getClickedInventory().getContents())
                                    if (isSim(itemStack, mi)) {
                                        Objects.requireNonNull(ci.getItemMeta()).setEnchantmentGlintOverride(false);
                                        break;
                                    }
                            if (us) weaver.setMain(s);
                            else weaver.setSecondary(s);
                            Objects.requireNonNull(ci.getItemMeta()).setEnchantmentGlintOverride(true);
                            break;
                        }
                    }
                }
            }
        }
    }
}
