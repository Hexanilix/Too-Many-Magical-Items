package org.tmmi.block;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.ChiseledBookshelf;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Element;
import org.tmmi.Structure.SBD;
import org.tmmi.Structure;
import org.tmmi.WeavePlayer;
import org.tmmi.spells.ATK;
import org.tmmi.spells.Spell;
import org.tmmi.spells.atributes.AreaEffect;
import org.tmmi.spells.atributes.Weight;

import java.util.*;
import java.util.stream.Stream;

import static org.hetils.Util.isSimBlk;
import static org.hetils.Util.newItemStack;
import static org.tmmi.Main.*;

public class WeavingTable extends InteractiveBlock {
    public static final Collection<WeavingTable> instances = new HashSet<>();
    public static final ItemStack item = newItemStack(Material.ENCHANTING_TABLE, "WeavingTable", 346722);
    public static final Structure base = new Structure(new String[][]{
            new String[]{
                    "SOS",
                    "ORO",
                    "NON"
            },
            new String[]{
                    "",
                    " E",
                    ""
            }
    }, Map.of(
            'E', new SBD(Material.ENCHANTING_TABLE),
            'O', new SBD(Material.CRYING_OBSIDIAN),
            'N', new SBD(Material.POLISHED_BLACKSTONE_BRICK_STAIRS, BlockFace.NORTH),
            'S', new SBD(Material.POLISHED_BLACKSTONE_BRICK_STAIRS, BlockFace.SOUTH),
            'R', new SBD(Material.REINFORCED_DEEPSLATE)));

    private final WTLis e;
    private final Thread particles;
    private boolean built = false;
    private int level = 3;
    private GlowItemFrame gi;
    public WeavingTable(@NotNull Location l) {
        this(l.getBlock());
    }
    public WeavingTable(Block b) {
        super(Material.ENCHANTING_TABLE, b);
//        this.built = base.isSim(b.getLocation().add(-1, -1, -1));
        this.built = true;
        gi = (GlowItemFrame) block.getWorld().spawnEntity(block.getLocation().add(0.5, 0.5, 0), EntityType.GLOW_ITEM_FRAME);
        gi.setItem(new ItemStack(Material.ENCHANTED_BOOK));
        gi.setFixed(true);
        gi.setInvulnerable(true);
        gi.setVisible(false);
        gi.setTicksLived(100000);
        this.e = new WTLis(b);
        Bukkit.getPluginManager().registerEvents(this.e, plugin);
        this.particles = newThread(new Thread() {
            private final World w = b.getWorld();
            private final Location a1 = new Location(w, b.getX(), b.getY()+0.75, b.getZ());
            private final Location a2 = new Location(w, b.getX(), b.getY()+0.75, b.getZ()+1);
            private final Location a3 = new Location(w, b.getX()+1, b.getY()+0.75, b.getZ());
            private final Location a4 = new Location(w, b.getX()+1, b.getY()+0.75, b.getZ()+1);
            private final Location ec = new Location(w, b.getX()+0.5, b.getY()+0.8, b.getZ()+0.5);
            @Override
            public void run() {
                try {
                    int i = 0;
                    int j = 0;
                    while (true) {
                        Thread.sleep(100);
                        if (built) {
                            w.spawnParticle(Particle.ASH, a1, 1, 0, 0, 0, 0);
                            w.spawnParticle(Particle.ASH, a2, 1, 0, 0, 0, 0);
                            w.spawnParticle(Particle.ASH, a3, 1, 0, 0, 0, 0);
                            w.spawnParticle(Particle.ASH, a4, 1, 0, 0, 0, 0);
                            if (i > 10) {
                                w.spawnParticle(Particle.END_ROD, ec, 1, 0.2, 0.1, 0.2, 0.001);
                                i = 0;
                            }
                            if (j > 5) {
                                w.spawnParticle(Particle.ENCHANT, ec, 2, 1, 1, 1, 0);
                                j = 0;
                            }
                            i++;
                            j++;
                        }
                    }
                } catch (InterruptedException ignored) {}
            }
        });
        this.particles.start();
        instances.add(this);
    }
    public class WTLis implements Listener {
        private boolean isWeaving = false;

        private final Block block;
        private UUID pid = null;
        private BukkitTask isAccepting = null;
        private ArmorStand a = null;
        private final List<ItemStack> itm = new ArrayList<>();
        private final World world;
        WTLis(@NotNull Block b) {
            this.block = b;
            this.world = b.getWorld();
        }
        @EventHandler
        public void onPlace(@NotNull BlockPlaceEvent event) {
            if (within3(event.getBlock(), block))
                Bukkit.getScheduler().runTask(plugin, () -> WeavingTable.this.built = base.isSim(block.getLocation().add(-1, -1, -1)));
        }
        @EventHandler
        public void onBreak(@NotNull BlockBreakEvent event) {
            if (within3(event.getBlock(), block))
                Bukkit.getScheduler().runTask(plugin, () -> WeavingTable.this.built = base.isSim(block.getLocation().add(-1, -1, -1)));
        }
        public static boolean within3(@NotNull Block cb, @NotNull Block tb) {
            return Math.abs(cb.getX() - tb.getX()) <= 1 &&
                    Math.abs(cb.getY() - tb.getY()) <= 1 &&
                    Math.abs(cb.getZ() - tb.getZ()) <= 1;
        }
        @EventHandler
        public void onPrepareItemEnchant(@NotNull PrepareItemEnchantEvent event) {
            if (isSimBlk(event.getEnchantBlock(), block)) {
                EnchantmentOffer[] enc = event.getOffers();
                for (int i = 0; i < 3; i++) {
                    EnchantmentOffer e = enc[i];
                    if (e != null) {
                        e.setEnchantmentLevel(e.getEnchantment().getMaxLevel() + 1);
                        enc[i] = e;
                    }
                }
            }
        }
        @EventHandler
        public void onEnchant(@NotNull EnchantItemEvent event) {
            if (isSimBlk(event.getEnchantBlock(), block)) {
                Map<Enchantment, Integer> map = event.getEnchantsToAdd();
                int sum = 0;
                for (Enchantment e : map.keySet()) {
                    sum += e.getMaxLevel() + 1;
                    map.replace(e, e.getMaxLevel() + 1);
                }
                event.setExpLevelCost(sum);
            }
        }
        @EventHandler
        public void blkClick(@NotNull PlayerInteractEvent event) {
            Block b = event.getClickedBlock();
            WeavePlayer p = WeavePlayer.getWeaver(event.getPlayer());
            log(p);
            if (p == null) return;
            if (b != null && isSimBlk(b, block) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getPlayer().isSneaking()) {
                event.setCancelled(true);
                if (isWeaving) return;
                pid = event.getPlayer().getUniqueId();
                Location c = b.getLocation().clone().add(0.5, 0.5, 0.5);
                if (this.isAccepting == null) {
                    world.playSound(b.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1,1);
                    a = (ArmorStand) p.getPlayer().getWorld().spawnEntity(block.getLocation().add(0, 250, 0), EntityType.ARMOR_STAND);
                    a.setVisible(false);
                    a.setGravity(false);
                    a.setVisualFire(false);
                    a.setSmall(true);
                    a.setInvulnerable(true);
                    a.setCustomNameVisible(true);
                    a.teleport(c);
                    this.isAccepting = new BukkitRunnable() {
                        private int w = 0;
                        @Override
                        public void run() {
                            Collection<Entity> ents = world.getNearbyEntities(block.getLocation().add(0.5, 0.7, 0.5), 0.2, 0.3, 0.2);
                            for (Entity e : ents)
                                if (e instanceof Item i)
                                    if (i.getThrower() == pid) {
                                        itm.add(i.getItemStack());
                                        world.playSound(i.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1,0.5f);
                                        i.remove();
                                        break;
                                    }
                            if (itm.size() == 3) {
                                a.remove();
                                cancel();
                                Element e = Element.getElement(itm.getFirst());
                                AreaEffect ae = AreaEffect.getAreaEffect(itm.get(2));
                                if (e == null || ae == null) {
                                    for (ItemStack i : itm) world.dropItem(block.getLocation().add(0.5, 0.5, 0.5), i);
                                } else {
                                    isWeaving = true;
                                    new BukkitRunnable() {
                                        final Random r = new Random();
                                        final Location l = block.getLocation().add(.5, .5, .5);
                                        int i = 0;
                                        @Override
                                        public void run() {
                                            i++;
                                            if (i > 100) {
                                                Spell s = new ATK(pid, "atk", Weight.CANTRIP, e, null, ae);
                                                new BukkitRunnable() {
                                                    int i = 0;
                                                    Item it = null;
                                                    @Override
                                                    public void run() {
                                                        if (it == null) {
                                                            it = world.dropItem(b.getLocation().add(0.5, 1.5, 0.5), s.toItem());
                                                            it.setPickupDelay(101);
                                                            it.setGravity(false);
                                                            it.setVelocity(new Vector(0, 0.05, 0));
                                                        }
                                                        i++;
                                                        world.spawnParticle(Particle.END_ROD, it.getLocation(), 1, 0, 0, 0, 0);
                                                        if (i >= 100) {
                                                            world.spawnParticle(Particle.END_ROD, it.getLocation(), 10, 0.1, 0.1, 0.1, 0.1);
                                                            it.remove();
                                                            cancel();
                                                            for (Block b : List.of(
                                                                    world.getBlockAt(block.getLocation().add(2, 1, 2)),
                                                                    world.getBlockAt(block.getLocation().add(2, 1, -2)),
                                                                    world.getBlockAt(block.getLocation().add(-2, 1, 2)),
                                                                    world.getBlockAt(block.getLocation().add(-2, 1, -2))
                                                            )) {
                                                                ChiseledBookshelf cb = (ChiseledBookshelf) b.getBlockData();
                                                                if (cb.getOccupiedSlots().size() < 6) {
                                                                    int c = -1;
                                                                    for (int a : cb.getOccupiedSlots())
                                                                        if (a > c) c = a;
                                                                    c++;
                                                                    cb.setSlotOccupied(c, true);
                                                                    log(c);
                                                                    b.setBlockData(cb);
                                                                    Location loc = b.getLocation().add(0, c > 2 ? 0.25 : 0.75, c%3==0 ? 0.2 : (c%3==1 ? 0.5 : 0.8));
                                                                    world.spawnParticle(Particle.END_ROD, loc, 6, 0.02, 0.03, 0.02, 0.01);
                                                                    world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 2, 1);
                                                                    WeavePlayer.getWeaver(pid).addSpell(s);
                                                                    isWeaving = false;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }.runTaskTimer(plugin, 0, 0);
                                                cancel();
                                            }
                                            if (r.nextInt(10) > 8) {
                                                Sound s;
                                                switch (r.nextInt(4)) {
                                                    case 0 -> s = Sound.ITEM_BOOK_PAGE_TURN;
                                                    case 1 -> s = Sound.BLOCK_CHISELED_BOOKSHELF_INSERT_ENCHANTED;
                                                    case 2 -> s = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                                                    default -> s = Sound.BLOCK_ENCHANTMENT_TABLE_USE;
                                                }
                                                world.spawnParticle(Particle.END_ROD, l.add(0, 0.05, 0), 5, 0.3, 0.3, 0.3, 0.1);
                                                world.playSound(l, s, 1F, (float) (r.nextInt(5)*0.1+0.8));
                                                List<ManaCauldron> list = Stream.of(
                                                        (ManaCauldron) org.tmmi.block.Block.get(b.getLocation().clone().add(1.5, -1, 1.5)),
                                                        (ManaCauldron) org.tmmi.block.Block.get(b.getLocation().clone().add(1.5, -1, -0.5)),
                                                        (ManaCauldron) org.tmmi.block.Block.get(b.getLocation().clone().add(-0.5, -1, 1.5)),
                                                        (ManaCauldron) org.tmmi.block.Block.get(b.getLocation().clone().add(-0.5, -1, -0.5)))
                                                        .filter(Objects::nonNull).toList();
                                                ManaCauldron mc = list.getFirst();
                                                for (ManaCauldron mac : list)
                                                    if (mac.getMana() > mc.getMana()) mc = mac;
                                                ManaCauldron finalMc = mc;
                                                mc.subMana(75);
                                                newThread(new Thread() {
                                                    int d = 20;
                                                    int v = 15;
                                                    int i = 0;
                                                    static double d(double x, float v) {
                                                        return (-Math.pow(x*v-3, 2)+9)/v;
                                                    }
                                                    @Override
                                                    public void run() {
                                                        Location l = finalMc.getLoc();
                                                        double bx = l.getX()+0.5;
                                                        double bz = l.getZ()+0.5;
                                                        double by = l.getY() + 1;
                                                        double x = (c.getX() - l.getX()) / v;
                                                        double z = (c.getZ() - l.getZ()) / v;
                                                        try {
                                                            while (i <= d) {
                                                                Thread.sleep(5);
                                                                for (int j = Math.max(i - (d - v), 0); j < Math.min(v, i); j++) {
                                                                    Location loc = new Location(world, bx + x * j, by + d(Math.abs(x * j), 6f), bz + z * j);
                                                                    world.spawnParticle(Particle.COMPOSTER, loc, 1, 0, 0, 0, 0);
                                                                }
                                                                i++;
                                                            }
                                                        } catch (InterruptedException ignore) {}
                                                    }
                                                }).start();
                                            }
                                        }
                                    }.runTaskTimer(plugin, 0, 0);
                                }
                                itm.clear();
                                isAccepting = null;
                            } else if (w == itm.size()) {
                                switch (itm.size()) {
                                    case 0 -> a.setCustomName("Main Element");
                                    case 1 -> a.setCustomName("Secondary element");
                                    case 2 -> a.setCustomName("Area Effect");
                                }
                                w++;
                            }
                        }
                    }.runTaskTimer(plugin, 0, 0);
                } else {
                    this.a.remove();
                    this.isAccepting.cancel();
                    this.isAccepting = null;
                    this.itm.forEach(i -> world.dropItem(b.getLocation().add(0.5, 0.5, 0.5), i));
                    this.itm.clear();
                    world.playSound(b.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1,0.5f);
                }
            }
        }
    }

    @Override
    public void onClick(Action action, Player player, PlayerInteractEvent event) {

    }

    @Override
    public void onBreak() {
        HandlerList.unregisterAll(e);
        this.particles.interrupt();
        gi.remove();
    }
}
