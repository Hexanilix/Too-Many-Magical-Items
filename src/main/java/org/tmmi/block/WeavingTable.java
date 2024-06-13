package org.tmmi.block;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Element;
import org.tmmi.Structure.SBD;
import org.tmmi.Structure;
import org.tmmi.items.ItemCommand;
import org.tmmi.spells.ATK;
import org.tmmi.spells.Spell;
import org.tmmi.spells.atributes.AreaEffect;
import org.tmmi.spells.atributes.Weight;

import java.util.*;

import static org.hetils.Util.isSimBlk;
import static org.hetils.Util.newItemStack;
import static org.tmmi.Main.*;

public class WeavingTable extends InteractiveBlock {
    public static final Collection<WeavingTable> instances = new HashSet<>();
    public static final ItemStack item = newItemStack(Material.ENCHANTING_TABLE, "WeavingTable", 346722);
    public static final Structure struct = new Structure(new String[][]{
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
    private GlowItemFrame gi;
    public WeavingTable(@NotNull Location l) {
        this(l.getBlock());
    }
    public WeavingTable(Block b) {
        super(Material.ENCHANTING_TABLE, b);
        this.built = struct.isSim(b.getLocation().add(-1, -1, -1));
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
    }
    public class WTLis implements Listener {
        public void interupt() {
            if (a != null) a.remove();
            for (ItemStack i : itm) world.dropItem(block.getLocation().add(0.5, 0.5, 0.5), i);
        }

        private final Block block;
        WTLis(@NotNull Block b) {
            this.block = b;
            this.world = b.getWorld();
        }
        private UUID pid = null;
        private boolean bool = true;
        private BukkitTask isAccepting = null;
        private ArmorStand a = null;
        private final List<ItemStack> itm = new ArrayList<>();
        private final World world;
        @EventHandler
        public void onPlace(@NotNull BlockPlaceEvent event) {
            if (within3(event.getBlock(), block))
                Bukkit.getScheduler().runTask(plugin, () -> WeavingTable.this.built = struct.isSim(block.getLocation().add(-1, -1, -1)));
        }
        @EventHandler
        public void onBreak(@NotNull BlockBreakEvent event) {
            if (within3(event.getBlock(), block))
                Bukkit.getScheduler().runTask(plugin, () -> WeavingTable.this.built = struct.isSim(block.getLocation().add(-1, -1, -1)));
        }
        public static boolean within3(@NotNull Block cb, @NotNull Block tb) {
            return Math.abs(cb.getX() - tb.getX()) <= 1 &&
                    Math.abs(cb.getY() - tb.getY()) <= 1 &&
                    Math.abs(cb.getZ() - tb.getZ()) <= 1;
        }
        @EventHandler
        public void blkClick(@NotNull PlayerInteractEvent event) {
            Block b = event.getClickedBlock();
            Player p = event.getPlayer();
            if (b != null && isSimBlk(b, block) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getPlayer().isSneaking()) {
                if (bool) {
                    pid = event.getPlayer().getUniqueId();
                    bool = false;
                    if (this.isAccepting == null) {
                        a = (ArmorStand) p.getWorld().spawnEntity(block.getLocation().add(0, 250, 0), EntityType.ARMOR_STAND);
                        a.setVisible(false);
                        a.setGravity(false);
                        a.setVisualFire(false);
                        a.setSmall(true);
                        a.setInvulnerable(true);
                        a.setCustomNameVisible(true);
                        a.teleport(block.getLocation().add(0.5, 0, 0.5));
                        this.isAccepting = new BukkitRunnable() {
                            private int w = 0;
                            @Override
                            public void run() {
                                Collection<Entity> ents = world.getNearbyEntities(block.getLocation().add(0.5, 1.1, 0.5), 0.2, 0.6, 0.2);
                                for (Entity e : ents)
                                    if (e instanceof Item i)
                                        if (i.getOwner() == pid) {
                                            itm.add(i.getItemStack());
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
                                        Spell s = new ATK(pid, "atk", Weight.CANTRIP, e, null, ae);
                                        ItemCommand ic = new ItemCommand(world.dropItem(b.getLocation().add(0.5, 0.5, 0.5), s.toItem()));
                                        ic.moveTo(block.getLocation().add(0.5, 2, 0.5))
                                                .newOperation(() -> {
                                                    ic.getItem().setVelocity(new Vector(0, 0, 0));
                                                    ic.getItem().setGravity(false);
                                                    try {
                                                        Thread.sleep(2000);
                                                    } catch (InterruptedException ex) {
                                                        throw new RuntimeException(ex);
                                                    }
                                                    ic.remove();
                                                });
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
                    }
                } else bool = true;
                event.setCancelled(true);
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

    @Override
    public String toJSON() {
        return  "\t\t{\n" +
                "\t\"type\":\"" + Type.WEAVING_TABLE + "\",\n" +
                "\t\"world\":\"" + this.getWorld().getName() + "\",\n" +
                "\t\"x\":\"" + this.getLoc().getX() + "\",\n" +
                "\t\"y\":\"" + this.getLoc().getY() + "\",\n" +
                "\t\"z\":\"" + this.getLoc().getZ() + "\",\n" +
                "}";

    }
}
