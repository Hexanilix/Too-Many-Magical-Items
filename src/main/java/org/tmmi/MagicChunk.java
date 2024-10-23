package org.tmmi;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

import static org.tmmi.Main.*;

public class MagicChunk {
    public static int div = 16;
    public static int treeDepth = 3;
    public static int CGPTH = 8;
    public static Map<World, List<Integer>> instX = new HashMap<>();
    public static Map<World, List<Integer>> instZ = new HashMap<>();
    public static Map<World, List<int[][]>> insances = new HashMap<>();
    public static int chmc = CHUNK_MANA_CAP.d();

    public static int takeMana(Entity e, int i) {
        return takeMana(e.getLocation(), i);
    }
    public static int takeMana(Location l, int i) {
        return takeMana(l.getWorld(), (int) l.getX(), (int) l.getZ(), i);
    }
//    public static int takeMana(Chunk c, int i) {
//        getManaAtChunk(c);
//        return t;
//    }
    public static int takeMana(World w, int x, int z, int i) {
        int m = getManaAt(w, x, z);
        subMana(w, x, z, i);
        return i;
    }


    public static class CGThread extends Thread {
        private final List<int[][]>[] lts = new List[CGPTH];
        @SafeVarargs
        CGThread(List<int[][]>... lists) {
            System.arraycopy(lists, 0, lts, 0, CGPTH);
            if (lists.length > CGPTH)
                new BukkitRunnable() {
                final List<int[][]>[] l = new List[CGPTH];
                    @Override
                    public void run() {
                        System.arraycopy(lists, CGPTH, l, 0, CGPTH);
                        new CGThread(l);
                    }
                }.runTask(plugin);
        }
        public boolean add(List<int[][]> list) {
            for (int i = 0; i < CGPTH; i++)
                if (lts[i]==null) {
                    lts[i] = list;
                    return true;
                }
            return false;
        }
        @Override
        public void run() {
            while (true) {
                try {
                    for (List<int[][]> lt : lts) {
                        if (lt == null) break;
                        for (int[][] it : lt) {
                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {

                                }
                            }
                        }
                    }
                    Thread.sleep(60000);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    public static int[] @Nullable [] newChunks(World w, int x, int z) {
        List<Integer> xs = instX.get(w);
        List<Integer> zs = instZ.get(w);
        for (int i = 0; i < instX.size(); i++)
            if (xs.get(i) == x && zs.get(i) == z)
                return null;
        int[][] nmc = new int[div][div];
        instX.get(w).add(x);
        instZ.get(w).add(z);
        for (int i = 0; i < div; i++)
            for (int j = 0; j < div; j++)
                nmc[i][j] = 1000;
        insances.get(w).add(nmc);
        return nmc;
    }
    public static int getManaAtChunk(World w, int x, int z) {
        List<Integer> xs = instX.get(w);
        List<Integer> zs = instZ.get(w);
        for (int i = 0; i < instX.size(); i++)
            if (xs.get(i) == x && zs.get(i) == z)
                return insances.get(w).get(i)[x%div][z%div];
        newChunks(w, x, z);
        return 1000;
    }
    public static int getManaAt(World w, int x, int z) {
        return getManaAtChunk(w, x/div, z/div);
    }
    public static int getManaAt(@NotNull Location loc) {
        return getManaAtChunk(loc.getWorld(), (int) (loc.getX()/div), (int) (loc.getZ()/div));
    }
    public static int getManaAt(@NotNull Entity e) {
        return getManaAt(e.getLocation());
    }
    public static int[][] get(World w, int x, int z) {
        List<Integer> xs = instX.get(w);
        List<Integer> zs = instZ.get(w);
        for (int i = 0; i < instX.size(); i++)
            if (xs.get(i) == x/div && zs.get(i) == z/div)
                return insances.get(w).get(i);
        return newChunks(w, x, z);
    }

    public static void addMana(World w, int x, int z, int amount) {
        int[][] cks = get(w, x, z);
        int xc = x%div;
        int zc = z%div;
        if (cks == null) {
            cks = new int[div][div];
            instX.get(w).add(x/div);
            instZ.get(w).add(z/div);
            for (int i = 0; i < div; i++)
                for (int j = 0; j < div; j++)
                    cks[i][j] = 1000;
            insances.get(w).add(cks);
        }
        int ov = cks[xc][zc] + amount;
        if (ov > CHUNK_MANA_CAP.v()) {
            cks[xc][zc] = CHUNK_MANA_CAP.v();
            ov -= CHUNK_MANA_CAP.v();
            addMana(w, x+1, z, ov/4);
            addMana(w, x-1, z, ov/4);
            addMana(w, x, z+1, ov/4);
            addMana(w, x, z-1, ov/4+ov%4);
        } else cks[xc][zc] += amount;
    }
    public static void subMana(World w, int x, int z, int amount) {
        int[][] cks = get(w, x, z);
        int xc = x%div;
        int zc = z%div;
        if (cks == null) {
            cks = new int[div][div];
            instX.get(w).add(x/div);
            instZ.get(w).add(z/div);
            for (int i = 0; i < div; i++)
                for (int j = 0; j < div; j++)
                    cks[i][j] = 1000;
            insances.get(w).add(cks);
        }
        int ov = cks[xc][zc] - amount;
        if (ov < 0) {
            cks[xc][zc] = 0;
            ov *= -1;
            subMana(w, x+1, z, ov/4+ov%4);
            subMana(w, x-1, z, ov/4);
            subMana(w, x, z+1, ov/4);
            subMana(w, x, z-1, ov/4);
        } else cks[xc][zc] -= amount;
    }

    public static void setMana(World w, int x, int z, int amount) {
        int[][] cks = get(w, x, z);
        int xc = x%div;
        int zc = z%div;
        if (cks == null) {
            cks = new int[div][div];
            instX.get(w).add(x/div);
            instZ.get(w).add(z/div);
            for (int i = 0; i < div; i++)
                for (int j = 0; j < div; j++)
                    cks[i][j] = 1000;
            insances.get(w).add(cks);
        }
        cks[xc][zc] = amount;
    }

    public int mean(World w, int x, int z) {
        return
                (
                    getManaAt(w, x+1, z)
                    +getManaAt(w, x-1, z)
                    +getManaAt(w, x, z+1)
                    +getManaAt(w, x, z-1)
                    +getManaAt(w, x, z)
                )/5;
    }
}
