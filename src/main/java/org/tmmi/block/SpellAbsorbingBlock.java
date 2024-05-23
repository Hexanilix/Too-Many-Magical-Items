package org.tmmi.block;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.tmmi.Main;
import org.tmmi.spells.CastSpell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.tmmi.Main.log;
import static org.tmmi.Main.newItemStack;

public class SpellAbsorbingBlock extends Block {
    public static List<SpellAbsorbingBlock> SAblocks = new ArrayList<>();
    public static ItemStack item = newItemStack(Material.LODESTONE, ChatColor.GOLD + "Spell Condenser", 200001);
    public Thread getSpellGrabThread() {
        return spellGrabThread;
    }

    private Thread spellGrabThread;
    private Thread dripTHread;
    private float magicules;
    public SpellAbsorbingBlock(Location loc, float magicules) {
        super(Material.LODESTONE, loc);
    }
    public SpellAbsorbingBlock(Location loc) {
        this(loc, 0);
    }

    @Override
    public void onPlace() {
        Location loc = this.getLoc();
        this.spellGrabThread = new Thread(() -> {
            while (true) {
                for (CastSpell s : CastSpell.instances) {
                    // fix the check
                    if (Main.inSphere(loc, 5, s.getLoc())) {
                        log("so it is");
                        this.magicules += (float) s.getCastCost() / 5;
                        s.uncast();
                    }
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        this.spellGrabThread.start();
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
        this.dripTHread.start();
    }

    @Override
    public void onBreak() {
        if (this.spellGrabThread != null) this.spellGrabThread.interrupt();
    }

    public float getMagicules() {
        return magicules;
    }

    @Override
    public String toJSON() {
        return  "\t\t{\n" +
                "\t\"type\":\"SPELL_WEAVER\",\n" +
                "\t\"world\":\"" + this.getWorld().getName() + "\",\n" +
                "\t\"x\":\"" + this.getLoc().getX() + "\",\n" +
                "\t\"y\":\"" + this.getLoc().getY() + "\",\n" +
                "\t\"z\":\"" + this.getLoc().getZ() + "\",\n" +
                "\t\"magicules\":\"" + this.getMagicules() + "\"\n" +
                "}";

    }
}
