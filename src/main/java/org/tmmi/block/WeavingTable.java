package org.tmmi.block;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.ChiseledBookshelf;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
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
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
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

import static org.hetils.Util.*;
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
    private final List<Thread> particles = new ArrayList<>();
    private boolean built = false;
    private int level = 3;
    private GlowItemFrame gi;
    final Location c = block.getLocation().clone().add(0.5, 0.7, 0.5);
    public WeavingTable(@NotNull Location l) {
        this(l.getBlock());
    }
    public WeavingTable(Block b) {
        super(Material.ENCHANTING_TABLE, b);
//        this.built = base.isSim(b.getLocation().add(-1, -1, -1));
        this.built = true;
        for (Entity en : b.getWorld().getNearbyEntities(b.getLocation().add(0.5, 0.5, 1), 0.1, 0.1, 0.1))
            if (en instanceof GlowItemFrame gf) {
                this.gi = gf;
                break;
            }
        if (this.gi == null) {
            this.gi = (GlowItemFrame) block.getWorld().spawnEntity(block.getLocation().add(0.5, 0.5, 0), EntityType.GLOW_ITEM_FRAME);
            this.gi.setItem(new ItemStack(Material.ENCHANTED_BOOK));
            this.gi.setFixed(true);
            this.gi.setInvulnerable(true);
            this.gi.setVisible(false);
            this.gi.setMetadata("tmmi_weavingtable_frame", new FixedMetadataValue(plugin, "yes"));
            this.gi.setTicksLived(100000);
        }
        this.e = new WTLis(b);
        Bukkit.getPluginManager().registerEvents(this.e, plugin);
        this.particles.add(newThread(new Thread() {
            private final World w = b.getWorld();
            private final Location a1 = new Location(w, b.getX(), b.getY()+0.75, b.getZ());
            private final Location a2 = new Location(w, b.getX(), b.getY()+0.75, b.getZ()+1);
            private final Location a3 = new Location(w, b.getX()+1, b.getY()+0.75, b.getZ());
            private final Location a4 = new Location(w, b.getX()+1, b.getY()+0.75, b.getZ()+1);
            @Override
            public void run() {
                try {
                    int i = 0;
                    while (true) {
                        Thread.sleep(100);
                        if (built) {
                            w.spawnParticle(Particle.ASH, a1, 1, 0, 0, 0, 0);
                            w.spawnParticle(Particle.ASH, a2, 1, 0, 0, 0, 0);
                            w.spawnParticle(Particle.ASH, a3, 1, 0, 0, 0, 0);
                            w.spawnParticle(Particle.ASH, a4, 1, 0, 0, 0, 0);
                            if (i > 10) {
                                w.spawnParticle(Particle.END_ROD, c, 1, 0.2, 0.1, 0.2, 0.001);
                                i = 0;
                            }
                            i++;
                        }
                    }
                } catch (InterruptedException ignored) {}
            }
        }));
        this.particles.add(t(block.getLocation().add(4, 1, 0).getBlock(), 1));
        this.particles.add(t(block.getLocation().add(-4, 1, 0).getBlock(), 2));
        this.particles.add(t(block.getLocation().add(0, 1, 4).getBlock(), 3));
        this.particles.add(t(block.getLocation().add(0, 1, -4).getBlock(), 4));
        for (Thread t : this.particles) t.start();
    }
    @Contract("_, _ -> !null")
    private @NotNull Thread t(@NotNull Block b, int i) {
        return newThread(new Thread() {
            final World world = b.getWorld();
            final Location loc = b.getLocation();
            final int v = 30;
            final Random r = new Random();
            static double f(double x, float s) {return (-Math.sin(x*2)*x)/s;}
            @Override
            public void run() {
                try {
                    while (true) {
                        int ran = r.nextInt(6);
                        for (int a : ((org.bukkit.block.data.type.ChiseledBookshelf) b.getBlockData()).getOccupiedSlots()) {
                            final double bk = ran % 3 == 0 ? 0.1875 : (ran % 3 == 1 ? 0.5 : 0.8125);
                            final double xf = (i > 2 ? bk : 0) + (i == 2 ? 1 : 0);
                            final double zf = (i == 4 ? 1 : (i != 3 ? bk : 0));
                            if (ran == a) {
                                double bx = loc.getX() + xf;
                                double bz = loc.getZ() + zf;
                                double by = loc.getY() + (ran > 2 ? 0.25 : 0.75);
                                double x = (c.getX() + (double) r.nextInt(10)/10 - bx) / v;
                                double z = (c.getZ() + (double) r.nextInt(10)/10 - bz) / v;
                                int dl = r.nextInt(50) + 50;
                                float s = 5+(float) r.nextInt(20)/10;
                                for (int j = 0; j < v; j++) {
                                    Thread.sleep(dl);
                                    world.spawnParticle(Particle.ENCHANT,
                                            new Location(world, bx + (i < 3 ? x*j : f(z*j, s)), by + f(x * j, s), bz + (i < 3 ? f(x * j, s) : z*j)),
                                            1, 0, 0, 0, 0);
                                }
                                world.spawnParticle(Particle.END_ROD,
                                        new Location(world, bx + (i < 3 ? x*v : f(z*v, s)), by + f(x * v, s), bz + (i < 3 ? f(x * v, s) : z*v)),
                                        1, 0, 0, 0, 0);
                                break;
                            }
                        }
                        Thread.sleep(r.nextInt(1000)+500);
                    }
                } catch (InterruptedException ignore) {}
            }
        });
    }
    public class WTLis implements Listener {
        private boolean isWeaving = false;

        private final Block block;
        private BukkitTask isAccepting = null;
        private final List<Item> itm = new ArrayList<>();
        private final World world;
        WTLis(@NotNull Block b) {
            this.block = b;
            this.world = b.getWorld();
        }
        @EventHandler
        public void onTake(org.bukkit.event.player.PlayerInteractEvent event) {

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
                if (this.isAccepting == null) {
                    for (Location bl : List.of(
                            block.getLocation().add(1.7, -1, 0.5),
                            block.getLocation().add(-0.7, -1, 0.5),
                            block.getLocation().add(0.5, -1, 1.7),
                            block.getLocation().add(0.5, -1, -0.7)
                    )) {
                        new BukkitRunnable() {
                            final World w = bl.getWorld();
                            Item it = null;
                            int t = 0;
                            @Override
                            public void run() {
                                w.spawnParticle(Particle.COMPOSTER, bl, 1, 0, 0, 0, 0);
                                if (it == null) {
                                    Collection<Entity> ents = w.getNearbyEntities(bl, 0.2, 0.3, 0.2);
                                    for (Entity e : ents)
                                        if (e instanceof Item i) {
                                            it = i;
                                            w.playSound(i.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0.5f);
                                            i.setPickupDelay(Integer.MAX_VALUE);
                                            i.setGravity(false);
                                            i.teleport(bl);
                                            it.setVelocity(new Vector(0, 0, 0));
                                            break;
                                        }
                                } else {
                                    if (t > 240) {
                                        it.setVelocity(genVec(b.getLocation().add(0.5, 0, 0.5), it.getLocation()).add(new Vector(0, 2.1, 0)).multiply(0.17));
                                        it.setGravity(true);
                                        it.setPickupDelay(60);
                                        it = null;
                                        t = 0;
                                    }
                                    if (it != null && !it.isDead()) w.spawnParticle(Particle.END_ROD, it.getLocation().add(0, 0.1, 0), 1, 0.1, 0.1, 0.1, 0.01);
                                    else {
                                        it = null;
                                    }
                                    t++;
                                }
                            }
                        }.runTaskTimer(plugin, 0, 5);
                    }
                    world.playSound(b.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1,1);
                    this.isAccepting = new BukkitRunnable() {
                        private int w = 0;
                        @Override
                        public void run() {
                            if (itm.size() == 3) {
                                cancel();
                                Element e = Element.getElement(itm.getFirst().getItemStack());
                                AreaEffect ae = AreaEffect.getAreaEffect(itm.get(2).getItemStack());
                                if (e == null || ae == null) {
                                    for (Item i : itm) i.teleport(c);
                                    world.playSound(c, Sound.ITEM_BUNDLE_DROP_CONTENTS, 1, 1);
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
                                                Spell s = new ATK(Bukkit.getPlayer("Hexanilix").getUniqueId(), "atk", Weight.CANTRIP, e, null, ae);
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
                                                            List<Block> sb = Stream.of(
                                                                            world.getBlockAt(block.getLocation().add(4, 1, 0)),
                                                                            world.getBlockAt(block.getLocation().add(-4, 1, 0)),
                                                                            world.getBlockAt(block.getLocation().add(0, 1, 4)),
                                                                            world.getBlockAt(block.getLocation().add(0, 1, -4)))
                                                                    .filter(blo -> ((ChiseledBookshelf) blo.getBlockData()).getOccupiedSlots().size() < 6)
                                                                    .toList();
                                                            Block bl = sb.get(r.nextInt(sb.size()));
                                                            ChiseledBookshelf cb = (ChiseledBookshelf) bl.getBlockData();
                                                            Set<Integer> s = cb.getOccupiedSlots();
                                                            int[] fr = new int[6-s.size()];
                                                            int p = 0;
                                                            for (int j = 0; j < 6; j++)
                                                                if (!s.contains(j)) {
                                                                    fr[p] = j;
                                                                    p++;
                                                                }
                                                            int c = fr[r.nextInt(fr.length)];
                                                            addBookToChiseledBookshelf(bl, c, newBook("Name", null, -1, "Hex", "Fuck you", BookMeta.Generation.ORIGINAL, "AAA", "nvm"));
                                                            Location loc = bl.getLocation().add(0, c > 2 ? 0.25 : 0.75, c%3==0 ? 0.1875 : (c%3==1 ? 0.5 : 0.8125));
                                                            world.spawnParticle(Particle.END_ROD, loc, 6, 0.02, 0.03, 0.02, 0.01);
                                                            world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 2, 1);
                                                            isWeaving = false;
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
                                                        (ManaCauldron) org.tmmi.block.Block.get(b.getLocation().clone().add(1.5, -3, 1.5)),
                                                        (ManaCauldron) org.tmmi.block.Block.get(b.getLocation().clone().add(1.5, -3, -0.5)),
                                                        (ManaCauldron) org.tmmi.block.Block.get(b.getLocation().clone().add(-0.5, -3, 1.5)),
                                                        (ManaCauldron) org.tmmi.block.Block.get(b.getLocation().clone().add(-0.5, -3, -0.5)))
                                                        .filter(Objects::nonNull).filter(m -> m.getMana() > 0).toList();
                                                ManaCauldron mc = (list.isEmpty() ? null : list.getFirst());
                                                for (ManaCauldron mac : list)
                                                    if (mac.getMana() > mc.getMana()) mc = mac;
                                                Location loca = null;
                                                if (mc != null) {
                                                    loca = mc.getLoc();
                                                    mc.subMana(75);
                                                }
                                                if (loca == null) {
                                                    Collection<Entity> av = world.getNearbyEntities(l, 2, 2, 2);
                                                    for (Entity e : av)
                                                        if (e instanceof Player as) {
                                                            WeavePlayer wa = WeavePlayer.getWeaver(as);
                                                            if (wa != null) {
                                                                loca = as.getLocation();
                                                                wa.subMana(10);
                                                                break;
                                                            }
                                                        }
                                                }
                                                if (loca != null) {
                                                    Location fl = loca;
                                                    newThread(new Thread() {
                                                        int d = 20;
                                                        int v = 15;
                                                        int i = 0;
                                                        final Location loc = fl;
                                                        static double d(double x, float v) {return (-Math.pow(x*v-3, 2)+9)/v;}
                                                        @Override
                                                        public void run() {
                                                            double bx = loc.getX()+0.5;
                                                            double bz = loc.getZ()+0.5;
                                                            double by = loc.getY()+0.7;
                                                            double x = (c.getX() - bx) / v;
                                                            double z = (c.getZ() - bz) / v;
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
                                        }
                                    }.runTaskTimer(plugin, 0, 0);
                                }
                                itm.clear();
                                isAccepting = null;
                            }
                        }
                    }.runTaskTimer(plugin, 0, 0);
                } else {
                    this.isAccepting.cancel();
                    this.isAccepting = null;
                    this.itm.forEach(i -> i.teleport(c));
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
        gi.remove();
        HandlerList.unregisterAll(e);
        this.particles.forEach(Thread::interrupt);
    }
}
