package org.tmmi;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.tmmi.Main.log;

public class MagicChunk {

    public static class ChunkInstanceExists extends Exception {
        public ChunkInstanceExists(String e) {
            super(e);
        }
    }

    public static int treeDepth = 3;
    public static Collection<MagicChunk> insances = new HashSet<>();

    public static @NotNull MagicChunk getOrNew(World world, int x, int z, int tree) {
        for (MagicChunk m : insances)
            if (m.world == world && m.x == x && m.z == z) return m;
        return new MagicChunk(world, x, z, 1000, tree);
    }
    public static @NotNull MagicChunk getOrNew(World world, int x, int z) {
        return getOrNew(world, x, z, treeDepth);
    }
    public static @NotNull MagicChunk getOrNew(@NotNull Location location) {
        return getOrNew(location.getWorld(), location.getChunk().getX(), location.getChunk().getZ(), treeDepth);
    }
    public static @NotNull MagicChunk getOrNew(@NotNull Entity e) {
        return getOrNew(e.getWorld(), e.getLocation().getChunk().getX(), e.getLocation().getChunk().getZ(), treeDepth);
    }
    public static @Nullable MagicChunk get(World world, int x, int z) {
        for (MagicChunk m : insances)
            if (m.world == world && m.x == x && m.z == z) return m;
        return null;
    }
    public static @Nullable MagicChunk get(@NotNull Location loc) {
        World w = loc.getWorld();
        int x = loc.getChunk().getX();
        int z = loc.getChunk().getZ();
        for (MagicChunk m : insances)
            if (m.world == w && m.x == x && m.z == z) return m;
        return null;
    }

    World world;
    int mana;
    int x;
    int z;
    private final List<MagicChunk> neighbours = new ArrayList<>();
    private ArmorStand dis;
    MagicChunk(World world, int x, int z, int mana, int tree) {
        try {
            if (get(world, x, z) != null) throw new ChunkInstanceExists("Chunk data already exists for chunk in " + ChatColor.ITALIC + world.getName() + ChatColor.RESET + ChatColor.RED + " at " + x + ", " + z  + ")");
        } catch (ChunkInstanceExists e) {
            e.printStackTrace();
        }
        this.x = x;
        this.z = z;
        if (tree > 0) {
            tree--;
            neighbours.add(MagicChunk.getOrNew(world, x + 1, z + 1, tree));
            neighbours.add(MagicChunk.getOrNew(world, x + 1, z, tree));
            neighbours.add(MagicChunk.getOrNew(world, x + 1, z - 1, tree));
            neighbours.add(MagicChunk.getOrNew(world, x, z + 1, tree));
            neighbours.add(MagicChunk.getOrNew(world, x, z - 1, tree));
            neighbours.add(MagicChunk.getOrNew(world, x - 1, z + 1, tree));
            neighbours.add(MagicChunk.getOrNew(world, x - 1, z , tree));
            neighbours.add(MagicChunk.getOrNew(world, x - 1, z - 1, tree));
        } else {
            neighbours.add(MagicChunk.get(world, x + 1, z + 1));
            neighbours.add(MagicChunk.get(world, x + 1, z));
            neighbours.add(MagicChunk.get(world, x + 1, z - 1));
            neighbours.add(MagicChunk.get(world, x, z + 1));
            neighbours.add(MagicChunk.get(world, x, z - 1));
            neighbours.add(MagicChunk.get(world, x - 1, z + 1));
            neighbours.add(MagicChunk.get(world, x - 1, z));
            neighbours.add(MagicChunk.get(world, x - 1, z - 1));
        }
        this.mana = mana;
        insances.add(this);
        this.world =    world;
    }

    public World getWorld() {
        return world;
    }

    public int mean() {
        List<MagicChunk> ne = neighbours;
        if (ne.isEmpty()) return mana;
        int nc = 0;
        for (Object o : ne) if (o == null) nc++;
        if (nc == ne.size()) return mana;
        int[] nl = new int[ne.size()-nc];
        int n = 0;
        for (int i = 0; i < 8; i++) {
            if (ne.get(i) != null) nl[i-n] = ne.get(i).mana;
            else n++;
        }
        int sum = 0;
        for (int j : nl) {
            sum += j;
        }
//        for (int i = 0; i < nl.length-1; i++) {
//            int m = i;
//            for (int j = i+1; j < nl.length; j++)
//                if (nl[j] < nl[m]) m = j;
//            int p = nl[m];
//            nl[m] = nl[i];
//            nl[i] = p;
//        }
//        for (int i = 0; i < nl.length/2; i++)
//            sum += nl[i] - (nl[nl.length-i-1]/2);
        return sum/nl.length;
    }

    public int getMana() {
        return mana;
    }

    public void addMana(int amount) {
        mana += amount;
    }

    public void subMana(int m) {
        mana -= m;
    }

    public void update() {
        int avg = mean();
        if (!neighbours.isEmpty() && avg < this.mana) {
            List<MagicChunk> mc = new ArrayList<>();
            for (int i = 0; i < neighbours.size(); i++) {
                MagicChunk c = neighbours.get(i);
                if (c == null || c.mana >= this.mana) continue;
                for (int j = i; j < neighbours.size(); j++)
                    if (neighbours.get(j) != null && c.mana < neighbours.get(j).mana) c = neighbours.get(j);
                mc.add(c);
            }
            if (!mc.isEmpty()) {
                int a = Math.max(Math.min(mana - avg, 20), 0);
                log("a = " + a);
                List<Integer> out = getIntegers(a, mc.size());
                int i = 0;
                if (a > 0) for (MagicChunk m : mc) {
                    if (a <= 0) break;
                    m.addMana(out.get(i));
                    this.mana -= out.get(i);;
                    a -= out.get(i);;
                    i++;
                }
            }
        }
    }

    private static @NotNull List<Integer> getIntegers(int a, int div) {
        List<Integer> val = new ArrayList<>();
        for (int i = 3; i < div+3; i++)
            val.add((int) Math.max(Math.round((float) a / (i + (double) div%i)), 0));
        List<Integer> out = new ArrayList<>();
        int sum = 0;
        for (int i = 0; i < div; i++) {
            if (sum < a) {
                sum += Math.min(val.get(i), a - sum);
                out.add(Math.min(val.get(i), a - sum));
            } else out.add(0);
        }
        return out;
    }
}
