package org.tmmi;

import org.bukkit.inventory.Inventory;

import java.util.List;

import static org.tmmi.Main.background;

public class GUI {

    private Inventory inv;
    public GUI(Inventory inv) {
        this.inv = inv;
    }

    public Inventory getInv() {
        return inv;
    }
}
