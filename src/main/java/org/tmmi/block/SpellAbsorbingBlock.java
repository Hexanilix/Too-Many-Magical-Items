package org.tmmi.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tmmi.Block;
import org.tmmi.Main;
import org.tmmi.Spell;

import java.util.Objects;
import java.util.Random;

import static org.tmmi.Main.log;

public class SpellAbsorbingBlock extends Block {
    public static ItemStack item;

    private Thread spellGrabTHread;
    private Thread dripTHread;
    private float magicules;
    public SpellAbsorbingBlock(Location loc) {
        super(Material.LODESTONE, loc);
        this.onPlace(loc);
    }

    @Override
    public void onPlace(@NotNull Location location) {
        Location loc = this.getLoc();
        this.spellGrabTHread = new Thread(() -> {
            while (true) {
                for (Spell s : Spell.spells) {
                    log("a spell");
                    // fix the check
                    if (s.isCast() && Main.inSphere(loc, 5, s.getCastLocation())) {
                        log("so it is");
                        this.magicules += (float) s.getCastCost() / 5;
                        s.unCast();
                    }
                }
                log("Doing it");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        this.spellGrabTHread.start();
        this.dripTHread = new Thread(() -> {
            try {
                if (this.magicules > 100) {
                    //Spell distribution
                    if (Objects.requireNonNull(this.getLoc().getWorld()).getBlockAt(this.getLoc().clone().subtract(0, 1, 0)).getType() == Material.STONE) {

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

    @Override
    public void onBreak(Location location) {

    }
}
