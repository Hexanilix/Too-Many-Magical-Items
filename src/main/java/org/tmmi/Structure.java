package org.tmmi;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static org.tmmi.Main.log;

public class Structure {

    public static class SBD {
        public enum Lit {
            TRUE,
            FALSE
        }

        public enum Open {
            TRUE,
            FALSE
        }
        Material m;
        Object[] meta = null;
        boolean hasMeta;
        @Contract(pure = true)
        public SBD(@NotNull Material m, Object... meta) {
            this.m = m;
            this.hasMeta = meta != null && meta.length > 0;
            if (hasMeta) this.meta = meta;
        }
    }
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
    int width;
    int depth;
    int height;
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
    public void build(Location loc) {
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
                setData(loc.clone().add(x, y, z).getBlock(), true, true, s.meta);
            t++;
        }
    }
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

    public static Structure grandWeaver;
    static {
        Map<Character, Material> map = new HashMap<>();
        map.put('A', Material.BUDDING_AMETHYST);
        map.put('O', Material.OAK_STAIRS);
        map.put('S', Material.STONE);
        map.put('B', Material.STONE_BRICK_STAIRS);
        map.put('C', Material.CAULDRON);
        map.put('T', Material.STRIPPED_OAK_LOG);
        map.put('N', Material.STONE_BRICKS);
        map.put('R', Material.BAMBOO_TRAPDOOR);
        map.put('L', Material.LECTERN);
        map.put('K', Material.DEEPSLATE_TILE_WALL);
        map.put('I', Material.OAK_SLAB);
        map.put('W', Material.OAK_TRAPDOOR);
        map.put('F', Material.OCHRE_FROGLIGHT);
        map.put('H', Material.HOPPER);
        map.put('c', Material.CHISELED_BOOKSHELF);
        map.put('P', Material.SOUL_CAMPFIRE);
        grandWeaver = new Structure(new String[][]{
                new String[]{
                        "   O   O   ",
                        "  BBBNBBB  ",
                        " BNSBABSNB ",
                        "OBSSBBBSSNO",
                        " BBBCSCBBB ",
                        " NABSSSBSN ",
                        " BBBCSCBBB ",
                        "OBSSBBBSSNO",
                        " BNSBABSNB ",
                        "  BBBNBBB  ",
                        "   O   O   "
                },
                new String[]{
                        "           ",
                        "   R N R   ",
                        "  NO   ON  ",
                        " ROT   TOR ",
                        "     L     ",
                        " N  LTL  N ",
                        "     L     ",
                        " ROT   TOR ",
                        "  NO   ON  ",
                        "   R N R   ",
                        "           "
                },
                new String[]{
                        "           ",
                        "     K     ",
                        "  K     K  ",
                        "   IO OI   ",
                        "   O W O   ",
                        " K  WFW  K ",
                        "   O W O   ",
                        "   IO OI   ",
                        "  K     K  ",
                        "     K     ",
                        "           "
                },
                new String[]{
                        "           ",
                        "     H     ",
                        "  H     H  ",
                        "     W     ",
                        "           ",
                        " H W   W H ",
                        "           ",
                        "     W     ",
                        "  H     H  ",
                        "     H     ",
                        "           "
                },
                new String[]{
                        "           ",
                        "    RcR    ",
                        "  P     P  ",
                        "           ",
                        " R       R ",
                        " c       c ",
                        " R       R ",
                        "           ",
                        "  P     P  ",
                        "    RcR    ",
                        "           "
                },
                new String[]{
                        "           ",
                        "     R     ",
                        "           ",
                        "           ",
                        "           ",
                        " R       R ",
                        "           ",
                        "           ",
                        "           ",
                        "     R     ",
                        "           "
                }
        }, map, new String[][]{
                new String[]{
                        " | | |S| | | |S| | | ",
                        " | |W|N|E| |S|N|E| | ",
                        " |N| | |E| |W| | |N| ",
                        "E|W| | |N|N|N| | |E|W",
                        " |S|S|N| | | |N|S|S| ",
                        " | | |W| | | |E| | | ",
                        " |N|N|W| | | |E|N|N| ",
                        "E|W| | |S|S|S| | |E|W",
                        " |S| | |E| |W| | |S| ",
                        " | |W|S|E| |W|S|E| | ",
                        " | | |N| | | |N| | | ",
                },
                new String[]{
                        " | | | | | | | | | | ",
                        " | | |N| | | |N| | | ",
                        " | | |S| | | |S| | | ",
                        " |W|E| | | | | |W|E| ",
                        " | | | | |N| | | | | ",
                        " | | | |W| |E| | | | ",
                        " | | | | |S| | | | | ",
                        " |W|E| | | | | |W|E| ",
                        " | | |N| | | |N| | | ",
                        " | | |S| | | |S| | | ",
                        " | | | | | | | | | | "
                },
                new String[]{
                        " | | | | | | | | | | ",
                        " | | | | | | | | | | ",
                        " | | | | | | | | | | ",
                        " | | | |WT| |ET| | | | ",
                        " | | |NT| |NO| |NT| | | ",
                        " | | | |WO| |EO| | | | ",
                        " | | |ST| |SO| |ST| | | ",
                        " | | | |WT| |ET| | | | ",
                        " | | | | | | | | | | ",
                        " | | | | | | | | | | ",
                        " | | | | | | | | | |"
                },
                new String[]{
                        " | | | | | | | | | | ",
                        " | | | | | | | | | | ",
                        " | | | | | | | | | | ",
                        " | | | | |S| | | | | ",
                        " | | | | | | | | | | ",
                        " | | |E| | | |W| | | ",
                        " | | | | | | | | | | ",
                        " | | | | |N| | | | | ",
                        " | | | | | | | | | | ",
                        " | | | | | | | | | | ",
                        " | | | | | | | | | | "
                },
                new String[]{
                        " | | | | | | | | | | ",
                        " | | | |WO|S|EO| | | | ",
                        " | |L| | | | | |L| | ",
                        " | | | | | | | | | | ",
                        " |NO| | | | | | | |NO| ",
                        " |E| | | | | | | |W| ",
                        " |SO| | | | | | | |SO| ",
                        " | | | | | | | | | | ",
                        " | |L| | | | | |L| | ",
                        " | | | |WO|N|EO| | | | ",
                        " | | | | | | | | | |"
                }
        }, Map.of(
                'N', BlockFace.NORTH,
                'S', BlockFace.SOUTH,
                'E', BlockFace.EAST,
                'W', BlockFace.WEST,
                'O', Structure.SBD.Open.TRUE,
                'T', Bisected.Half.TOP,
                'B', Bisected.Half.BOTTOM,
                'L', Structure.SBD.Lit.FALSE
        ));
    }
}
