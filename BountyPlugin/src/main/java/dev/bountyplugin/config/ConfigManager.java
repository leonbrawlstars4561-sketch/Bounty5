package dev.bountyplugin.config;

import dev.bountyplugin.bounty.MoneyFormatter;
import dev.bountyplugin.util.PlaceholderUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.Map;

public final class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private MoneyFormatter moneyFormatter;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        this.moneyFormatter = new MoneyFormatter(config.getString("settings.currency-symbol", "$"));
    }

    public FileConfiguration raw() {
        return config;
    }

    public MoneyFormatter getMoneyFormatter() {
        return moneyFormatter;
    }

    public BigDecimal getMinBounty() {
        return BigDecimal.valueOf(config.getDouble("settings.min-bounty", 1.0D));
    }

    public boolean isMaxBountyEnabled() {
        return config.contains("settings.max-bounty") && !config.getString("settings.max-bounty", "").isBlank();
    }

    public String getMaxBountyRaw() {
        return config.getString("settings.max-bounty", "");
    }

    public boolean isBroadcastOnSet() {
        return config.getBoolean("settings.broadcast-on-set", true);
    }

    public boolean isBroadcastOnKill() {
        return config.getBoolean("settings.broadcast-on-kill", true);
    }

    public boolean isRequireTargetHasPlayed() {
        return config.getBoolean("settings.require-target-has-played", true);
    }

    public int getGuiSize() {
        return config.getInt("gui.size", 27);
    }

    public int getConfirmSlot() {
        return config.getInt("gui.confirm-slot", 11);
    }

    public int getCancelSlot() {
        return config.getInt("gui.cancel-slot", 15);
    }

    public int getInfoSlot() {
        return config.getInt("gui.info-slot", 13);
    }

    public String getGuiTitle() {
        return color(config.getString("gui.title", "&8Bounty bestaetigen"));
    }

    public String getMessage(String key) {
        String raw = config.getString("messages." + key, "&cFehlende Nachricht: " + key);
        return color(raw);
    }

    public void send(CommandSender sender, String key) {
        sender.sendMessage(getPrefixed(getMessage(key)));
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        String message = PlaceholderUtil.apply(getMessage(key), placeholders);
        sender.sendMessage(getPrefixed(message));
    }

    private String getPrefixed(String message) {
        String prefix = color(config.getString("messages.prefix", ""));
        return prefix + message;
    }

    public String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}
