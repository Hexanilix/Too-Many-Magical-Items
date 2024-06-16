package org.tmmi;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Openable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.logging.Level;

import static org.tmmi.Main.log;

public class Structure {

    public static class SBD {
        public enum Open {
            TRUE,
            FALSE
        }
        Material m;
        Object[] meta;
        boolean hasMeta;
        @Contract(pure = true)
        public SBD(@NotNull Material m, Object... meta) {
            this.m = m;
            this.meta = meta;
            this.hasMeta = meta != null && meta.length > 0;
        }
    }
    @Contract("_, _ -> param1")
    public static BlockData setData(BlockData b, Object @NotNull ... v) {
        for (Object o : v) setData(b, o);
        return b;
    }
    public static void setData(@NotNull Block b, Object... v) {
        for (Object ob : v)
            b.setBlockData(setData(b.getBlockData(), b));
    }
    public static void setData(@NotNull Block b, Object v) {
        b.setBlockData(setData(b.getBlockData(), v, false, false));
    }
    public static void setData(Block bl, Object v, boolean effects, boolean sound) {
        BlockData b = bl.getBlockData();
        if (b instanceof Directional s) {
            if (v instanceof BlockFace f) {
                s.setFacing(f);
                if (sound)
                    bl.getWorld().playSound(bl.getLocation().add(0.5, 0.5, 0.5), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 1);
                if (effects)
                    bl.getWorld().spawnParticle(Particle.BLOCK, bl.getLocation().add(0.5, 0.5, 0.5), 10, 0.25, 0.25, 0.25, 0.01, b);
            }
            else if (b instanceof Openable o) {
                if (v instanceof SBD.Open op) {
                    o.setOpen(op == SBD.Open.TRUE);
                    if (sound)
                        bl.getWorld().playSound(bl.getLocation().add(0.5, 0.5, 0.5), (op == SBD.Open.TRUE ? Sound.BLOCK_WOODEN_TRAPDOOR_OPEN : Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE), 1, 1);
                }
            }
        } else if (b instanceof Bisected bs) {
            if (v instanceof Bisected.Half h)
                bs.setHalf(h);
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
            // Add more cases as needed for different block types
            default -> null;
        };
    }

    private final SBD[][][] layers;
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
    public Structure(@NotNull String[] @NotNull [] layers, Map<Character, Material> map, String[][][] masks, Map<Character, Object> maksMap) {
        int xw = 0;
        int zw = 0;
        for (String[] sl : layers)
            for (String s : sl)
                if (s.length() > xw) xw = s.length();
        for (String[] s : layers)
            if (s.length > zw) zw = s.length;
        SBD[][][] sbds = new SBD[layers.length][zw][xw];
        Material[][][] mats = new Material[layers.length][zw][xw];
        int h = 0;
        for (String[] layer : layers) {
            for (int z = 0; z < layer.length; z++) {
                String s = layer[z];
                for (int x = 0; x < xw; x++) {
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
        if (masks != null) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < zw; z++) {
                    for (int x = 0; x < xw; x++) {
                        String s = masks[y][z][x];
                        Object[] bo = new Object[s.length()];
                        for (int i = 0; i < s.length(); i++)
                            bo[i] = maksMap.get(s.charAt(i));
                        sbds[y][z][x] = new SBD(mats[y][z][x], bo);
                    }
                }
            }
        } else {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < zw; z++) {
                    for (int x = 0; x < xw; x++) {
                        sbds[y][z][x] = new SBD(mats[y][z][x]);
                    }
                }
            }
        }
        this.layers = sbds;
    }
    public void build(Location loc) {
        for (int y = 0; y < layers.length; y++) {
            SBD[][] mats = layers[y];
            for (int z = 0; z < mats.length; z++) {
                SBD[] sbd = mats[z];
                for (int x = 0; x < sbd.length; x++) {
                    Block b = loc.clone().add(x, y, z).getBlock();
                    SBD sb = sbd[x];
                    b.setType(sb.m != null ? sb.m : Material.AIR);
                    if (sb.hasMeta) {
                        setData(b, sb.meta);
                    }
                }
            }
        }
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
