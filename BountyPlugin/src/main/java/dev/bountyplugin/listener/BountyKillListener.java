package dev.bountyplugin.listener;

import dev.bountyplugin.BountyPlugin;
import dev.bountyplugin.bounty.Bounty;
import dev.bountyplugin.config.ConfigManager;
import dev.bountyplugin.economy.EconomyResult;
import dev.bountyplugin.util.PlaceholderUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class BountyKillListener implements Listener {

    private final BountyPlugin plugin;

    public BountyKillListener(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) {
            // Kein PVP-Kill (Sturz, Mobs, Umgebung, /kill, etc.) -> kein Payout.
            return;
        }

        if (killer.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }

        // Atomare Entnahme: Selbst bei mehrfach ausgeloesten Death-Events
        // kann derselbe Bounty niemals doppelt entnommen werden.
        Bounty bounty = plugin.getBountyManager().takeBounty(victim.getUniqueId());

        if (bounty == null) {
            return;
        }

        ConfigManager config = plugin.getConfigManager();

        if (!plugin.getEconomyManager().isReady()) {
            plugin.getLogger().log(Level.SEVERE,
                    "Bounty auf " + victim.getName() + " wurde faellig, aber es ist keine Economy " +
                    "verfuegbar! Der Betrag von " + bounty.getAmount() + " konnte nicht ausgezahlt werden.");
            return;
        }

        EconomyResult result = plugin.getEconomyManager().deposit(killer.getUniqueId(), bounty.getAmount());

        if (!result.isSuccess()) {
            plugin.getLogger().log(Level.SEVERE,
                    "Auszahlung des Bountys auf " + victim.getName() + " an " + killer.getName() +
                    " ist fehlgeschlagen: " + result.getErrorMessage());
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("killer", killer.getName());
        placeholders.put("victim", victim.getName());
        placeholders.put("amount", config.getMoneyFormatter().formatFull(bounty.getAmount()));

        String message = PlaceholderUtil.apply(config.getMessage("bounty-paid-out"), placeholders);

        if (config.isBroadcastOnKill()) {
            plugin.getServer().broadcastMessage(message);
        } else {
            killer.sendMessage(message);
        }
    }
}
