package org.tmmi;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Candle;
import org.bukkit.scheduler.BukkitRunnable;
import org.hetils.FileVersion;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmmi.block.WeavingTable;

import java.util.*;
import java.util.logging.Level;

import static org.tmmi.Main.log;
import static org.tmmi.Main.plugin;

/**
 * A custom structure class
 *
 *
 * @author  Hexanilix
 * @since   1.0
 */
public class Structure {

    /**
     * The class {@code SBD} stands for Structure Block Data. It's simply a package of a {@link org.bukkit.Material} and a undisclosed amount of block data,
     * <i>not specifically classes extending {@link org.bukkit.block.data.BlockData}</i><br><br>
     * Custom block data for number and boolean values are the following:
     * <ul>
     *     <li> {@link Lit} </li>
     *     <li> {@link CandleAmount}</li>
     *     <li> {@link Open} </li>
     * </ul>
     *
     * @author  Hexanilix
     * @since   1.0
     */
    public static class SBD {
        /**
         * {@code SBD.Lit} is a reference to the {@code isLit} boolean in {@link org.bukkit.block.data.Lightable}.
         * <br><br><i><font color="gray">A boolean to be interpreted by the {@code setData()} method</i>.
         *
         * @author  Hexanilix
         * @since   1.0
         */
        public enum Lit {
            TRUE,
            FALSE
        }
        /**
         * {@code SBD.Open} is a reference to the {@code isOpen} boolean in the block data type {@link org.bukkit.block.data.Openable}.
         * <br><br><i><font color="gray">A boolean to be interpreted by the {@code setData()} method</i>.
         *
         * @author  Hexanilix
         * @since   1.0
         */
        public enum Open {
            TRUE,
            FALSE
        }
        /**
         * {@code SBD.Open} is a reference to the amount of candles in {@link org.bukkit.block.data.type.Candle}.
         * <br><br><i><font color="gray">An integer to be interpreted by the {@code setData()} method</i>.
         *
         * @author  Hexanilix
         * @since   1.0
         */
        public enum CandleAmount {
            ONE(1),
            TWO(2),
            THREE(3),
            FOUR(4);

            final int amount;
            CandleAmount(int a) {
                this.amount = a;
            }
            public int a() {
                return this.amount;
            }
        }
        public Material m;
        public Object[] meta = null;
        boolean hasMeta;
        @Contract(pure = true)
        public SBD(@NotNull Material m, Object... meta) {
            this.m = m;
            this.hasMeta = meta != null && meta.length > 0;
            if (hasMeta) this.meta = meta;
        }
    }
    /**
     * This function is mainly used in the {@link Structure} class to set the {@link BlockData} of the placed blocks,
     * like the facing direction, lit or unlit states, block half ect. It is used mainly with the {@link SBD} classes {@code meta} array.
     *
     * @param bl The {@link Block} to set the data at
     * @param vs The data
     * @param effects whether particles appear when applying block data
     * @param sound whether sound plays when applying block data
     * @author  Hexanilix
     * @since   1.0
     */
    public static void setData(@NotNull Block bl,  boolean effects, boolean sound, Object... vs) {
        if (vs == null || vs.length == 0) return;
        BlockData b = bl.getBlockData();
        for (Object v : vs) {
            if (b instanceof Directional s) {
                if (v instanceof BlockFace f) {
                    s.setFacing(f);
                    if (sound)
                        bl.getWorld().playSound(bl.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1, 1);
                    if (effects)
                        bl.getWorld().spawnParticle(Particle.END_ROD, bl.getLocation().add(0.5, 0.5, 0.5), 5, 0.25, 0.25, 0.25, 0.01);
                } else if (b instanceof Openable o) {
                    if (v instanceof SBD.Open op) {
                        o.setOpen(op == SBD.Open.TRUE);
                        if (sound)
                            bl.getWorld().playSound(bl.getLocation().add(0.5, 0.5, 0.5), (op == SBD.Open.TRUE ? Sound.BLOCK_WOODEN_TRAPDOOR_OPEN : Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE), 1, 1);
                    }
                }
            }
            if (b instanceof Bisected bs) {
                if (v instanceof Bisected.Half h)
                    bs.setHalf(h);
            }
            if (b instanceof Lightable l) {
                if (v instanceof Structure.SBD.Lit li)
                    l.setLit(li == SBD.Lit.TRUE);
            }
            if (b instanceof Candle c)
                if (v instanceof SBD.CandleAmount a)
                    c.setCandles(a.a());
        }
        bl.setBlockData(b);
    }
    @Contract(pure = true)
    public static @Nullable Sound getBlockBreakSound(@NotNull Material material) {
        return switch (material) {
            case STONE, COBBLESTONE, MOSSY_COBBLESTONE, STONE_BRICKS, CRACKED_STONE_BRICKS, MOSSY_STONE_BRICKS ->
                    Sound.BLOCK_STONE_BREAK;
            case DIRT, GRASS_BLOCK, PODZOL, MYCELIUM -> Sound.BLOCK_GRASS_BREAK;
            case SAND, RED_SAND -> Sound.BLOCK_SAND_BREAK;
            case GRAVEL -> Sound.BLOCK_GRAVEL_BREAK;
            case OAK_PLANKS, SPRUCE_PLANKS, BIRCH_PLANKS, JUNGLE_PLANKS, ACACIA_PLANKS, DARK_OAK_PLANKS ->
                    Sound.BLOCK_WOOD_BREAK;
            case GLASS, GLASS_PANE, WHITE_STAINED_GLASS, ORANGE_STAINED_GLASS, MAGENTA_STAINED_GLASS,
                 LIGHT_BLUE_STAINED_GLASS, YELLOW_STAINED_GLASS, LIME_STAINED_GLASS, PINK_STAINED_GLASS,
                 GRAY_STAINED_GLASS, LIGHT_GRAY_STAINED_GLASS, CYAN_STAINED_GLASS, PURPLE_STAINED_GLASS,
                 BLUE_STAINED_GLASS, BROWN_STAINED_GLASS, GREEN_STAINED_GLASS, RED_STAINED_GLASS, BLACK_STAINED_GLASS ->
                    Sound.BLOCK_GLASS_BREAK;
            default -> null;
        };
    }

    final SBD[][][] layers;
    public int width;
    public int depth;
    public int height;
    public Structure(@NotNull String[] @NotNull [] layers, Map<Character, SBD> map) {
        int xw = 0;
        int zw = 0;
        for (String[] sl : layers)
            for (String s : sl)
                if (s.length() > xw) xw = s.length();
        for (String[] s : layers)
            if (s.length > zw) zw = s.length;
        SBD[][][] mats = new SBD[layers.length][zw][xw];
        int h = 0;
        for (String[] layer : layers) {
            for (int z = 0; z < layer.length; z++) {
                String s = layer[z];
                for (int x = 0; x < xw; x++) {
                    if (x < s.length()) {
                        SBD sb = map.get(s.charAt(x));
                        if (sb == null) sb = new SBD(Material.AIR);
                        if (!sb.m.isBlock()) {
                            log(Level.WARNING, "[Structure] Material at x:" + z + ", z:" + x + " is not a block. Setting to air");
                            sb.m = Material.AIR;
                        }
                        mats[h][z][x] = sb;
                    } else mats[h][z][x] = new SBD(Material.AIR);
                }
            }
            h++;
        }
        this.layers = mats;
    }
    public Structure(@NotNull String[] @NotNull [] layers, Map<Character, Material> map, String[][] mask, Map<Character, Object> maksMap) {
        width = 0;
        depth = 0;
        height = layers.length;
        for (String[] sl : layers)
            for (String s : sl)
                if (s.length() > width) width = s.length();
        for (String[] s : layers)
            if (s.length > depth) depth = s.length;
        SBD[][][] sbds = new SBD[height][depth][width];
        Material[][][] mats = new Material[height][depth][width];
        int h = 0;
        for (String[] layer : layers) {
            for (int z = 0; z < layer.length; z++) {
                String s = layer[z];
                for (int x = 0; x < width; x++) {
                    if (x < s.length()) {
                        Material sb = map.get(s.charAt(x));
                        if (sb == null) sb = Material.AIR;
                        if (!sb.isBlock()) {
                            log(Level.WARNING, "[Structure] Material at x:" + z + ", z:" + x + " is not a block. Setting to air");
                            sb = Material.AIR;
                        }
                        mats[h][z][x] = sb;
                    } else mats[h][z][x] = Material.AIR;
                }
            }
            h++;
        }
        if (mask != null) {
            StringBuilder sb = new StringBuilder();
            for (String[] str : mask) {
                for (String sti : str)
                    sb.append(sti).append("|");
            }
            String[] m = sb.toString().split("\\|");
            for (int y = 0; y < h; y++)
                for (int z = 0; z < depth; z++)
                    for (int x = 0; x < width; x++) {
                        int l = (y*(depth * width))+(z* width)+x;
                        if (l < m.length) {
                            String s = m[l].replace(" ", "");
                            Object[] ob = new Object[s.length()];
                            for (int i = 0; i < s.length(); i++)
                                ob[i] = maksMap.get(s.charAt(i));
                            sbds[y][z][x] = new SBD(mats[y][z][x], ob);
                        } else sbds[y][z][x] = new SBD(mats[y][z][x]);
                    }
        } else {
            for (int y = 0; y < h; y++)
                for (int z = 0; z < depth; z++)
                    for (int x = 0; x < width; x++)
                        sbds[y][z][x] = new SBD(mats[y][z][x]);
        }
        this.layers = sbds;
    }
    /**
     * Builds the Structure at the desired location, loc being the edge point
     *
     * @param loc The edge location to build the structure from
     */
    public void build(Location loc) {
        build(loc, false, false);
    }
    /**
     * Builds the Structure at the desired location, loc being the edge point
     *
     * @param loc The edge location to build the structure from
     * @param particles whether particles appear when applying block data
     * @param sound whether sound plays when applying block data
     */
    public void build(Location loc, boolean particles, boolean sound) {
        final int zt = depth;
        final int xt = width;
        final int ar = zt*xt;
        final int h = layers.length*ar-1;
        int t = 0;
        while (t < h) {
            int y = t/ar;
            int z = (t%ar)/zt;
            int x = (t%ar)%xt;
            SBD s = layers[y][z][x];
            loc.clone().add(x, y, z).getBlock().setType(s.m != null ? s.m : Material.AIR);
            if (s.hasMeta)
                setData(loc.clone().add(x, y, z).getBlock(), particles, sound, s.meta);
            t++;
        }
    }
    public Collection<Block> getBlockPlacements(@NotNull Location loc) {
        int xf = this.width/2;
        int zf = this.depth/2;
        loc.subtract(xf, 0, zf);
        Collection<Block> cl = new HashSet<>();
        final int zt = depth;
        final int xt = width;
        final int ar = zt*xt;
        final int h = layers.length*ar-1;
        int t = 0;
        while (t < h) {
            cl.add(loc.clone().add((t%ar)%xt, t/ar, (t%ar)/zt).getBlock());
            t++;
        }
        return cl;
    }
    /**
     * Builds the Structure at the desired location from the location with a specified delay
     *
     * @param l The center location
     * @param r a Runnable which executes after the building is done
     */
    public void buildFromCenterDelay(Location l, Runnable r) {
        buildFromCenterDelay(l, false, false, r);
    }
    /**
     * Builds the Structure at the desired location from the location with a specified delay
     *
     * @param l The center location
     * @param particles whether particles appear when applying block data
     * @param sound whether sound plays when applying block data
     * @param r a Runnable which executes after the building is done
     */
    public void buildFromCenterDelay(Location l, boolean particles, boolean sound, Runnable r) {
        final int zt = this.depth;
        final int xt = this.width;
        final int ar = zt*xt;
        new BukkitRunnable() {
            int t = ar/2;
            @Override
            public void run() {
                Structure.SBD sb = Structure.this.getAt(t);
                while (sb != null && sb.m.isAir()) {
                    if (t % ar <= 0) t += ar + (ar / 2);
                    t--;
                    sb = Structure.this.getAt(t);
                }
                if (sb == null) cancel();
                else {
                    int y = t / ar;
                    int z = (t % ar) / zt;
                    int x = (t % ar) % xt;
                    org.bukkit.block.Block b = l.clone().add(x, y, z).getBlock();
                    b.setType(sb.m);
                    setData(b, particles, sound, sb.meta);
                }
                t--;
            }
        }.runTaskTimer(plugin, 0, 0);
        new BukkitRunnable() {
            int t = ar/2;
            @Override
            public void run() {
                Structure.SBD sb = Structure.this.getAt(t);
                while (sb != null && sb.m.isAir()) {
                    if (t%ar>=ar-1) t += ar-(ar/2);
                    t++;
                    sb = Structure.this.getAt(t);
                }
                if (sb == null) {
                    cancel();
                    r.run();
                }
                else {
                    int y = t / ar;
                    int z = (t % ar) / zt;
                    int x = (t % ar) % xt;
                    org.bukkit.block.Block b = l.clone().add(x, y, z).getBlock();
                    b.setType(sb.m);
                    setData(b, particles, sound, sb.meta);
                }
                t++;
            }
        }.runTaskTimer(plugin, 0, 0);
    }
    /**
     * This method linearizes the structures 3d space into a linearized table, able to be
     * looked through with a linearized index.
     *
     * @param a The linearized index of the structure
     * @return returns an SBD (Structure Block Data) at that linear index
     * <br><br>
     * 3D space coordinates are calculated from a linearized index in the following way:
     * <blockquote>
     *     <pre>a = <i>the linearized index</i></pre>
     *     <pre>X = <b>(a%(width*depth))%width</b></pre>
     *     <pre>Y = <b>a/(width*depth)</b></pre>
     *     <pre>Z = <b>(a%(width*depth))/depth</b></pre>
     * </blockquote>
     */
    public SBD getAt(int a) {
        int ar = width*depth;
        int y = a/ar;
        if (y >= height) return null;
        return layers[y][(a%ar)/depth][(a%ar)%width];
    }
    public boolean isSim(Location loc) {
        for (int y = 0; y < layers.length; y++) {
            SBD[][] mats = layers[y];
            for (int z = 0; z < mats.length; z++) {
                SBD[] sbd = mats[z];
                for (int x = 0; x < sbd.length; x++) {
                    if (sbd[x].m.isAir()) continue;
                    if (sbd[x].m != loc.clone().add(x, y, z).getBlock().getType()) return false;
                }
            }
        }
        return true;
    }
}
