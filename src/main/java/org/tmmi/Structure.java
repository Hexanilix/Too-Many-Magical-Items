package org.tmmi;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Openable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
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
        b.setBlockData(setData(b.getBlockData(), v));
    }
    public static void setData(@NotNull Block b, Object v) {
        b.setBlockData(setData(b.getBlockData(), v));
    }
    public static BlockData setData(BlockData b, Object v) {
        if (b instanceof Directional s) {
            if (v instanceof BlockFace f)
                s.setFacing(f);
            else if (b instanceof Openable o) {
                if (v instanceof SBD.Open op)
                    o.setOpen(op == SBD.Open.TRUE);
            }
        } else if (b instanceof Bisected bs) {
            if (v instanceof Bisected.Half h)
                bs.setHalf(h);
        }
        return b;
    }

    private final SBD[][][] layers;

    public Structure(@NotNull String[][] layers, Map<Character, SBD> map) {
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
    public void build(Location loc) {
        for (int y = 0; y < layers.length; y++) {
            SBD[][] mats = layers[y];
            for (int z = 0; z < mats.length; z++) {
                SBD[] sbd = mats[z];
                for (int x = 0; x < sbd.length; x++) {
                    Block b = loc.clone().add(x, y, z).getBlock();
                    SBD sb = sbd[x];
                    b.setType(sb.m);
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
