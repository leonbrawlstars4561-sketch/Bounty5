package dev.bountyplugin.gui;

import dev.bountyplugin.BountyPlugin;
import dev.bountyplugin.bounty.Bounty;
import dev.bountyplugin.config.ConfigManager;
import dev.bountyplugin.economy.EconomyResult;
import dev.bountyplugin.util.PlaceholderUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public final class GUIListener implements Listener {

    private final BountyPlugin plugin;

    public GUIListener(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        InventoryHolder topHolder = topInventory.getHolder();

        if (!(topHolder instanceof ConfirmationHolder holder)) {
            return;
        }

        // Jeglicher Klick innerhalb dieser GUI (auch Shift-Klicks aus dem
        // Spieler-Inventar heraus) wird komplett unterbunden, damit Items
        // weder entwendet noch verschoben werden koennen.
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(topInventory)) {
            return;
        }

        int slot = event.getSlot();
        ConfigManager config = plugin.getConfigManager();

        if (slot == config.getConfirmSlot()) {
            handleConfirm(player, holder.getPendingBounty());
            player.closeInventory();
        } else if (slot == config.getCancelSlot()) {
            config.send(player, "cancelled");
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory.getHolder() instanceof ConfirmationHolder) {
            event.setCancelled(true);
        }
    }

    private void handleConfirm(Player player, PendingBounty pendingBounty) {
        ConfigManager config = plugin.getConfigManager();

        // Betrag und Kontostand werden zum Zeitpunkt der Bestaetigung
        // erneut geprueft, falls sich der Kontostand seit dem Oeffnen
        // der GUI veraendert hat.
        if (!plugin.getEconomyManager().isReady()) {
            config.send(player, "no-economy");
            return;
        }

        BigDecimal amount = pendingBounty.getAmount();

        if (!plugin.getEconomyManager().has(pendingBounty.getPlacerUuid(), amount)) {
            config.send(player, "insufficient-funds");
            return;
        }

        EconomyResult result = plugin.getEconomyManager().withdraw(pendingBounty.getPlacerUuid(), amount);

        if (!result.isSuccess()) {
            config.send(player, "insufficient-funds");
            return;
        }

        Bounty bounty = plugin.getBountyManager().addOrIncrease(
                pendingBounty.getTargetUuid(), pendingBounty.getTargetName(), amount);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", config.getMoneyFormatter().formatFull(amount));
        placeholders.put("target", pendingBounty.getTargetName());
        placeholders.put("total", config.getMoneyFormatter().formatFull(bounty.getAmount()));

        config.send(player, "confirmed", placeholders);

        if (config.isBroadcastOnSet()) {
            String broadcastTemplate = "&8[&6Bounty&8] &e{placer} &7hat einen Bounty von &f{amount} " +
                    "&7auf &f{target} &7gesetzt!";
            Map<String, String> broadcastPlaceholders = new HashMap<>(placeholders);
            broadcastPlaceholders.put("amount", config.getMoneyFormatter().formatAbbreviated(amount));
            broadcastPlaceholders.put("placer", player.getName());
            plugin.getServer().broadcastMessage(
                    config.color(PlaceholderUtil.apply(broadcastTemplate, broadcastPlaceholders)));
        }
    }
}
