package dev.bountyplugin.commands;

import dev.bountyplugin.BountyPlugin;
import dev.bountyplugin.bounty.AmountParseException;
import dev.bountyplugin.bounty.AmountParser;
import dev.bountyplugin.config.ConfigManager;
import dev.bountyplugin.gui.ConfirmationGUI;
import dev.bountyplugin.gui.PendingBounty;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class BountyCommand implements CommandExecutor, TabCompleter {

    private final BountyPlugin plugin;
    private final ConfirmationGUI confirmationGUI;

    public BountyCommand(BountyPlugin plugin) {
        this.plugin = plugin;
        this.confirmationGUI = new ConfirmationGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern verwendet werden.");
            return true;
        }

        ConfigManager config = plugin.getConfigManager();

        if (!player.hasPermission("bountyplugin.command.bounty")) {
            config.send(player, "no-permission");
            return true;
        }

        if (args.length != 3 || !args[0].equalsIgnoreCase("add")) {
            config.send(player, "usage-bounty-add");
            return true;
        }

        handleAdd(player, args[1], args[2]);
        return true;
    }

    private void handleAdd(Player player, String targetName, String rawAmount) {
        ConfigManager config = plugin.getConfigManager();

        if (!plugin.getEconomyManager().isReady()) {
            config.send(player, "no-economy");
            return;
        }

        OfflinePlayer target = resolveTarget(targetName);

        if (target == null || target.getName() == null || (config.isRequireTargetHasPlayed()
                && !target.hasPlayedBefore() && !target.isOnline())) {
            config.send(player, "player-not-found");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())
                && !player.hasPermission("bountyplugin.bypass.selftarget")) {
            config.send(player, "cannot-target-self");
            return;
        }

        BigDecimal amount;
        try {
            amount = AmountParser.parse(rawAmount);
        } catch (AmountParseException exception) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("input", rawAmount);
            config.send(player, "invalid-amount", placeholders);
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("input", rawAmount);
            config.send(player, "invalid-amount", placeholders);
            return;
        }

        if (amount.compareTo(config.getMinBounty()) < 0) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("min", config.getMoneyFormatter().formatFull(config.getMinBounty()));
            config.send(player, "amount-too-low", placeholders);
            return;
        }

        if (config.isMaxBountyEnabled()) {
            try {
                BigDecimal max = AmountParser.parse(config.getMaxBountyRaw());
                if (amount.compareTo(max) > 0) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("max", config.getMoneyFormatter().formatFull(max));
                    config.send(player, "amount-too-high", placeholders);
                    return;
                }
            } catch (AmountParseException ignored) {
                plugin.getLogger().warning("settings.max-bounty in der config.yml ist ungueltig konfiguriert.");
            }
        }

        if (!plugin.getEconomyManager().has(player.getUniqueId(), amount)) {
            config.send(player, "insufficient-funds");
            return;
        }

        UUID targetUuid = target.getUniqueId();
        String resolvedName = target.getName();

        PendingBounty pendingBounty = new PendingBounty(player.getUniqueId(), targetUuid, resolvedName, amount);
        Inventory inventory = confirmationGUI.build(pendingBounty, target);
        player.openInventory(inventory);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", config.getMoneyFormatter().formatFull(amount));
        placeholders.put("target", resolvedName);
        config.send(player, "confirmation-opened", placeholders);
    }

    /**
     * Loest zunaechst online-Spieler exakt auf. Nur falls kein Online-Spieler
     * gefunden wird, greift der (von Bukkit als deprecated markierte)
     * name-basierte Offline-Lookup - dieser bleibt fester Bestandteil der
     * Bukkit-API und liefert auf modernen Paper-Servern den lokal
     * zwischengespeicherten usercache-Eintrag, sofern der Spieler bereits
     * einmal auf dem Server war.
     */
    private OfflinePlayer resolveTarget(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }

        return Bukkit.getOfflinePlayer(name);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("add"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            List<String> names = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            return filter(names, args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            return filter(List.of("1000", "1k", "1.5m", "1b", "10t", "1q"), args[2]);
        }

        return new ArrayList<>();
    }

    private List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
