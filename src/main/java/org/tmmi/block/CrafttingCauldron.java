package org.tmmi.block;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Block;
import org.tmmi.Recipe;

import java.util.*;

import static org.tmmi.Main.log;
import static org.tmmi.Main.plugin;

public class CrafttingCauldron extends Block {
    public static ItemStack item;
    public static List<CrafttingCauldron> cauldron = new ArrayList<>();

    public static List<Location> craftingCauldronLocations = new ArrayList<>();
    private Boolean isCrafting;
    private Boolean isGathering;
    private BukkitTask thread;

    public CrafttingCauldron(Location loc) {
        super(Material.CAULDRON, loc);
        this.onPlace(loc);
        this.isCrafting = false;
        this.isGathering = false;
        cauldron.add(this);
    }

    @Override
    public void onPlace(@NotNull Location location) {
        location.getBlock().setType(Material.CAULDRON);
        CrafttingCauldron craft = this;
        this.thread = new BukkitRunnable() {
            private final CrafttingCauldron craftIn = craft;
            private int i = 0;
            private int iter = 0;
            private final List<ItemStack> ingredients = new ArrayList<>();
            @Override
            public void run() {
                i++;
                Location loc = craftIn.getLoc();
                org.bukkit.block.Block mat = Objects.requireNonNull(loc.getWorld()).getBlockAt(loc);
                if (!mat.getType().equals(Material.CAULDRON)) {
                    this.cancel();
                }
                double angle = i * ((2 * Math.PI) / 10);
                double x = loc.getX() + 0.3 * Math.cos(angle);
                double z = loc.getZ() + 0.3 * Math.sin(angle);
                if (!craftIn.isCrafting()) {
                    if (i >= 10) i = 0;
                    Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.ENCHANTMENT_TABLE, loc.clone().add(0.5, 2, 0.5), (craftIn.isCrafting() ? 10 : 1), 0, 1, 0);
                    if (craftIn.isGathering()) {
                        iter++;
                        if (iter == 25) {
                            craftIn.setCrafting(true);
                            craftIn.setGathering(false);
                            iter = 0;
                        }
                        loc.getWorld().spawnParticle(Particle.SPELL_WITCH, new Location(loc.getWorld(), x + 0.5, loc.getY() + 0.4, z + 0.5), 1);
                    } else {
                        loc.getWorld().spawnParticle(Particle.COMPOSTER, new Location(loc.getWorld(), x + 0.5, loc.getY() + 0.4, z + 0.5), 1);
                    }
                    List<Entity> nearbyEntities = (List<Entity>) Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc.clone().add(0.5, 0.3, 0.5), 0.5, 0.3, 0.5);
                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof Item trIt) {
                            if (trIt.getThrower() == null) {
                                continue;
                            }
                            Player player = Bukkit.getPlayer(trIt.getThrower());
                            assert player != null;
                            entity.remove();
                            if (!player.hasPermission(org.tmmi.Main.permission)) {
                                loc.getWorld().playSound(loc.clone().add(0.5, 0.5, 0.5), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, -2);
                                loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 5);
                                loc.getWorld().dropItem(loc.clone().add(0.5, 1, 0.5), trIt.getItemStack());
                                continue;
                            }
                            ingredients.add(new ItemStack(trIt.getItemStack()));
                            log(ingredients.size());
                            loc.getWorld().playSound(loc.clone().add(0.5, 0.5, 0.5), Sound.ENTITY_ITEM_PICKUP, Math.min( trIt.getItemStack().getAmount(), 5), 1);
                            craftIn.setGathering(true);
                        }
                    }
                } else {
                    if (Recipe.isValidRecipe(ingredients)) {
                        Objects.requireNonNull(loc.getWorld()).playSound(loc, Sound.BLOCK_LAVA_POP, 1, 1);
                        loc.getWorld().spawnParticle(Particle.ASH, loc.clone().add(0.5, 2, 0.5), 20, 0, 0, 0, 1);
                        Recipe r = Recipe.getRecipe(ingredients);
                        loc.getWorld().playSound(loc.clone().add(0.5, 0.5, 0.5), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 4);
                        new BukkitRunnable() {
                            int it = 0;

                            @Override
                            public void run() {
                                it++;
                                double angle = it * ((2 * Math.PI) / 10);
                                double x = loc.getX() + 0.5 * Math.cos(angle);
                                double z = loc.getZ() + 0.5 * Math.sin(angle);
                                Objects.requireNonNull(loc.getWorld()).spawnParticle((it < 30 ? Particle.WAX_ON : Particle.WAX_OFF), new Location(loc.getWorld(), x + 0.5, loc.getY() + 1 + (double) it / 15, z + 0.5), 1);
                                loc.getWorld().playSound(loc.clone().add(0, (double) it / 7 + 1, 0), Sound.ENTITY_ITEM_PICKUP, 1, (float) it / 50);
                                if (it == 60) {
                                    loc.getWorld().playSound(loc, Sound.ENTITY_ITEM_PICKUP, 1, -3);
                                    assert r != null;
                                    loc.getWorld().dropItem(loc.clone().add(0, (double) it / 15 + 1, 0), r.getOutcome());
                                    craftIn.setCrafting(false);
                                    cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 10, 0);
                        craftIn.setCrafting(false);
                        loc.getWorld().playSound(loc.clone().add(0.5, 0.5, 0.5), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, -2);
                        loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 5);
                        for (ItemStack i : ingredients) {
                            loc.getWorld().dropItem(loc.clone().add(0.5, 1, 0.5), i);
                        }
                    } else {
                        loc.getWorld().playSound(loc.clone().add(0.5, 0.5, 0.5), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, -2);
                        loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc.clone().add(0.5, 0.5, 0.5), 5);
                        for (ItemStack i : ingredients) {
                            loc.getWorld().dropItem(loc.clone().add(0.5, 1, 0.5), i);
                        }
                    }
                    craftIn.setCrafting(false);
                    ingredients.clear();
                }
            }
        }.runTaskTimer(plugin, 0, 2);
        this.setLoc(location);
    }

    @Override
    public void onBreak(@NotNull Location location) {
        Objects.requireNonNull(location.getWorld()).dropItem(location.clone().add(0.5,0.5,0.5), item);
        this.thread.cancel();
        cauldron.remove(this);
    }

    private void setCrafting(boolean b) {
        this.isCrafting = b;
    }

    private void setGathering(boolean b) {
        this.isGathering = b;
    }

//    private @NotNull Thread gatherThread() {
//        return new Thread(() -> {
//            int iter = 0;
//            List<ItemStack> ingredients = new ArrayList<>();
//            while (this.placed) {
//                org.bukkit.block.Block mat = Objects.requireNonNull(this.loc.getWorld()).getBlockAt(this.loc);
//                if (!mat.getType().equals(Material.CAULDRON)) {
//                    this.placed = false;
//                    continue;
//                }
//                double angle = iter * ((2 * Math.PI) / 10);
//                double x = this.loc.getX() + 0.3 * Math.cos(angle);
//                double z = this.loc.getZ() + 0.3 * Math.sin(angle);
//                iter++;
//                isGathering = !ingredients.isEmpty();
//                if (!this.isCrafting) {
//                    if (iter >= 10) iter = 0;
//                    this.loc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc.clone().add(0.5, 2, 0.5), (isGathering ? 10 : 1), 0, 1, 0);
//                    if (isGathering) {
//                        isCrafting = iter == 5;
//                        this.loc.getWorld().spawnParticle(Particle.SPELL_WITCH, new Location(loc.getWorld(), x + 0.5, loc.getY() + 0.4, z + 0.5), 1);
//                    } else {
//                        this.loc.getWorld().spawnParticle(Particle.COMPOSTER, new Location(loc.getWorld(), x + 0.5, loc.getY() + 0.4, z + 0.5), 1);
//                    }
//                    List<Entity> nearbyEntities = (List<Entity>) Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc.clone().add(0.5, 0.3, 0.5), 0.5, 0.3, 0.5);
//                    for (Entity entity : nearbyEntities) {
//                        if (entity instanceof Item trIt) {
//                            Player player = Bukkit.getPlayer(Objects.requireNonNull(trIt.getThrower()));
//                            if (player == null) {
//                                continue;
//                            }
//                            if (!player.hasPermission(permission)) {
//                                loc.getWorld().playSound(loc.clone().add(0.5, 0.5, 0.5), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, -2);
//                                loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 5);
//                                entity.remove();
//                                loc.getWorld().dropItem(loc.clone().add(0.5, 1, 0.5), trIt.getItemStack());
//                                continue;
//                            }
//                            if (trIt.getItemStack().getAmount() < 2) {
//                                ingredients.add(trIt.getItemStack());
//                                loc.getWorld().playSound(loc.clone().add(0.5, 0.5, 0.5), Sound.ENTITY_ITEM_PICKUP, 1, 1);
//                            } else {
//                                for (int i = 0; i < trIt.getItemStack().getAmount(); i++) {
//                                    ingredients.add(new ItemStack(trIt.getItemStack().getType()));
//                                    loc.getWorld().playSound(loc.clone().add(0.5, 0.5, 0.5), Sound.ENTITY_ITEM_PICKUP, 1, 1);
//                                }
//                            }
//                            isGathering = Recipe.isValidRecipe(ingredients);
//                        }
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ignored) {}
//                } else {
//                    loc.getWorld().playSound(loc, Sound.BLOCK_LAVA_POP, 1, 1);
//                    loc.getWorld().spawnParticle(Particle.ASH, loc.clone().add(0.5, 2, 0.5), 20, 0, 0, 0, 1);
//                    if (iter == 50) {
//                        Recipe r = Recipe.getRecipe(ingredients);
//                        isGathering = false;
//                        loc.getWorld().playSound(loc.clone().add(0.5, 0.5, 0.5), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 4);
//                        new BukkitRunnable() {
//                            int i = 0;
//
//                            @Override
//                            public void run() {
//                                i++;
//                                double angle = i * ((2 * Math.PI) / 10);
//                                double x = loc.getX() + 0.5 * Math.cos(angle);
//                                double z = loc.getZ() + 0.5 * Math.sin(angle);
//                                loc.getWorld().spawnParticle((i < 30 ? Particle.WAX_ON : Particle.WAX_OFF), new Location(loc.getWorld(), x + 0.5, loc.getY() + 1 + (double) i / 15, z + 0.5), 1);
//                                loc.getWorld().playSound(loc.clone().add(0, (double) i / 7 + 1, 0), Sound.ENTITY_ITEM_PICKUP, 1, (float) i / 50);
//                                if (i == 60) {
//                                    loc.getWorld().playSound(loc, Sound.ENTITY_ITEM_PICKUP, 1, -3);
//                                    loc.getWorld().dropItem(loc.clone().add(0, (double) i / 15 + 1, 0), r.getOutcome());
//                                    isCrafting = false;
//                                    cancel();
//                                }
//                            }
//                        }.runTaskTimer(plugin, 10, 0);
//                        isGathering = false;
//                        loc.getWorld().playSound(loc.clone().add(0.5, 0.5, 0.5), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, -2);
//                        loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 5);
//                        for (ItemStack i : ingredients) {
//                            loc.getWorld().dropItem(loc.clone().add(0.5, 1, 0.5), i);
//                        }
//                    }
//                    isCrafting = false;
//                    ingredients.clear();
//                }
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException ignored) {}
//            }
//        });
//    }

    public ItemStack getItem() {
        return item;
    }

    public Boolean isCrafting() {
        return isCrafting;
    }

    public Boolean isGathering() {
        return isGathering;
    }
}
