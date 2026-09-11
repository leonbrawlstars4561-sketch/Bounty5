package dev.bountyplugin;

import dev.bountyplugin.bounty.BountyManager;
import dev.bountyplugin.commands.BountyAdminCommand;
import dev.bountyplugin.commands.BountyCommand;
import dev.bountyplugin.config.ConfigManager;
import dev.bountyplugin.economy.EconomyManager;
import dev.bountyplugin.gui.GUIListener;
import dev.bountyplugin.listener.BountyKillListener;
import dev.bountyplugin.storage.BountyStorage;
import dev.bountyplugin.storage.YamlBountyStorage;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class BountyPlugin extends JavaPlugin {

    private static BountyPlugin instance;

    private ConfigManager configManager;
    private EconomyManager economyManager;
    private BountyStorage bountyStorage;
    private BountyManager bountyManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.configManager.reload();

        this.economyManager = new EconomyManager(this);
        boolean economyReady = this.economyManager.setup();

        if (!economyReady) {
            getLogger().log(Level.SEVERE,
                    "Keine kompatible Economy gefunden! BountyPlugin benoetigt Vault mit " +
                    "einem Economy-Provider (z.B. EternalEconomy) oder EternalEconomy direkt " +
                    "(welches sich selbst bei Vault registriert). Das Plugin bleibt aktiv, " +
                    "aber Bounty-Funktionen, die Geld benoetigen, werden Spielern mit einer " +
                    "Fehlermeldung verweigert.");
        } else {
            getLogger().info("Economy erfolgreich verbunden: " + this.economyManager.getProviderName());
        }

        this.bountyStorage = new YamlBountyStorage(this);
        this.bountyManager = new BountyManager(this.bountyStorage);
        this.bountyManager.loadAll();
        getLogger().info(bountyManager.getAllBounties().size() + " Bounty(s) aus bounties.yml geladen.");

        registerCommands();
        registerListeners();

        getLogger().info("BountyPlugin wurde aktiviert.");
    }

    @Override
    public void onDisable() {
        if (bountyManager != null) {
            bountyManager.saveAll();
        }
        getLogger().info("BountyPlugin wurde deaktiviert.");
    }

    private void registerCommands() {
        BountyCommand bountyCommand = new BountyCommand(this);
        Objects.requireNonNull(getCommand("bounty")).setExecutor(bountyCommand);
        Objects.requireNonNull(getCommand("bounty")).setTabCompleter(bountyCommand);

        BountyAdminCommand adminCommand = new BountyAdminCommand(this);
        Objects.requireNonNull(getCommand("bountyad")).setExecutor(adminCommand);
        Objects.requireNonNull(getCommand("bountyad")).setTabCompleter(adminCommand);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new BountyKillListener(this), this);
    }

    public static BountyPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public BountyManager getBountyManager() {
        return bountyManager;
    }
}
