package dev.bountyplugin.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class ConfirmationHolder implements InventoryHolder {

    private final PendingBounty pendingBounty;
    private Inventory inventory;

    public ConfirmationHolder(PendingBounty pendingBounty) {
        this.pendingBounty = pendingBounty;
    }

    public PendingBounty getPendingBounty() {
        return pendingBounty;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
