package org.tmmi.block;

import org.tmmi.Block;
import org.tmmi.Spell;

import java.util.Random;

public class SpellAbsorbingBlock extends Block {
    private Thread spellGrabTHread;
    private Thread dripTHread;
    private float magicules;
    public SpellAbsorbingBlock(Material material, Location loc) {
        super(material, loc);
        this.onPlace(loc);
    }

    @Override
    public void onPlace(Location location) {
        this.spellGrabTHread = new Thread(() -> {
            for (Spell s : Spell.spells) {
                if (s.base().isCast() && inSphere(this.getLoc(), 5, s.base().getCastLocation())) {
                    this.magicules += (float) s.base().getCastCost() / 5;
                    s.base().unCast();
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        this.dripTHread = new Thread(() -> {
            try {
                if (this.magicules > 100) {
                    if (this.getLoc().getBlockAt(this.getLoc().clone().subtract(0, 1, 0)) == Material.STONE) {

                    } else {
                        this.magicules -= new Random().nextInt(0, 5);
                    }
                }
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static boolean inSphere(Location center, int radius, Location location) {
        int dx = center.getBlockX() - location.getBlockX();
        int dy = center.getBlockY() - location.getBlockY();
        int dz = center.getBlockZ() - location.getBlockZ();
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    @Override
    public void onBreak(Location location) {

    }
}
