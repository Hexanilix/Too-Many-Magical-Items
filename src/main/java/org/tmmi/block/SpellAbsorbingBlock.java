package org.tmmi.block;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.tmmi.Main;
import org.tmmi.spells.CastSpell;

import java.util.*;

import static org.hetils.Util.inSphere;
import static org.hetils.Util.newItemStack;
import static org.tmmi.Main.*;

public class SpellAbsorbingBlock extends Block {
    public static Collection<SpellAbsorbingBlock> instances = new HashSet<>();
    public static ItemStack item = newItemStack(Material.LODESTONE, ChatColor.GOLD + "Spell Condenser", 200001);
    public Thread getMainThread() {
        return mainThread;
    }

    private final Thread mainThread;
    private int magicules;
    private int cap;
    public SpellAbsorbingBlock(Location loc, int magicules, int cap) {
        super(Material.LODESTONE, loc);
        this.magicules = magicules;
        this.cap = cap;
        this.mainThread = newThread(() -> {
                try {
                    if (magicules > 100) {
                        //Spell distribution
                        if (Objects.requireNonNull(getLoc().getWorld()).getBlockAt(getLoc().clone().subtract(0, 1, 0)).getType() == Material.STONE) {

                        } else {
                            SpellAbsorbingBlock.this.magicules -= new Random().nextInt(0, 5);
                        }
                    }
                    while (true) {
                        Thread.sleep(200);
                        for (CastSpell s : CastSpell.instances) {
                            // fix the check
                            if (inSphere(getLoc(), 5, s.getLoc())) {
                                getWorld().spawnParticle(Particle.CLOUD, s.getLoc(), s.getS().getLevel(), 1, 1, 1, 0.02);
                                SpellAbsorbingBlock.this.magicules += s.getCastCost();
                                s.uncast(false);
                            }
                        }

                    }
                } catch (InterruptedException ignore) {}
        });
        this.mainThread.start();

        instances.add(this);
    }
    public SpellAbsorbingBlock(Location loc) {
        this(loc, 0, 100);
    }

    @Override
    public void onBreak() {
        if (this.mainThread != null) this.mainThread.interrupt();
    }

    public float getMagicules() {
        return magicules;
    }

    @Override
    public String json() {
        return "\t\"magicules\":\"" + this.getMagicules() + "\"\n";
    }
}
