package dev.bountyplugin.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Kapselt jeglichen Zugriff auf externe Economy-Plugins. Der Rest des
 * Bounty-Systems kennt ausschliesslich diese Klasse - niemals Vault oder
 * EternalEconomy direkt.
 * <p>
 * EternalEconomy stellt keine eigene, separat zu integrierende API bereit;
 * es registriert sich selbst als Vault-{@link Economy}-Provider. Eine reine
 * Vault-Integration deckt daher automatisch sowohl Vault mit einem beliebigen
 * kompatiblen Provider als auch EternalEconomy im Speziellen ab, ohne dass
 * erfundene APIs oder Klassen noetig waeren.
 */
public final class EconomyManager {

    private final JavaPlugin plugin;
    private Economy economy;

    public EconomyManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Sucht einen aktiven Vault-Economy-Provider. Gibt false zurueck, wenn
     * weder Vault noch ein Provider gefunden werden konnte - das Plugin
     * stuerzt in diesem Fall nicht ab, sondern bleibt aktiv und meldet
     * Economy-Operationen als fehlgeschlagen.
     */
    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            this.economy = null;
            return false;
        }

        RegisteredServiceProvider<Economy> provider =
                Bukkit.getServicesManager().getRegistration(Economy.class);

        if (provider == null) {
            plugin.getLogger().log(Level.WARNING,
                    "Vault ist installiert, aber es wurde kein Economy-Provider gefunden. " +
                    "Installiere z.B. EternalEconomy, damit Vault einen Provider registrieren kann.");
            this.economy = null;
            return false;
        }

        this.economy = provider.getProvider();
        return this.economy != null && this.economy.isEnabled();
    }

    public boolean isReady() {
        return economy != null && economy.isEnabled();
    }

    public String getProviderName() {
        return isReady() ? economy.getName() : "keine";
    }

    public BigDecimal getBalance(UUID uuid) {
        if (!isReady()) {
            return BigDecimal.ZERO;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        double balance = economy.getBalance(player);
        return BigDecimal.valueOf(balance);
    }

    public boolean has(UUID uuid, BigDecimal amount) {
        if (!isReady()) {
            return false;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return economy.has(player, toDouble(amount));
    }

    public EconomyResult withdraw(UUID uuid, BigDecimal amount) {
        if (!isReady()) {
            return EconomyResult.failure("Keine Economy verfuegbar.");
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        EconomyResponse response = economy.withdrawPlayer(player, toDouble(amount));
        if (response.transactionSuccess()) {
            return EconomyResult.success();
        }
        return EconomyResult.failure(response.errorMessage != null
                ? response.errorMessage
                : "Abbuchung fehlgeschlagen.");
    }

    public EconomyResult deposit(UUID uuid, BigDecimal amount) {
        if (!isReady()) {
            return EconomyResult.failure("Keine Economy verfuegbar.");
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        EconomyResponse response = economy.depositPlayer(player, toDouble(amount));
        if (response.transactionSuccess()) {
            return EconomyResult.success();
        }
        return EconomyResult.failure(response.errorMessage != null
                ? response.errorMessage
                : "Gutschrift fehlgeschlagen.");
    }

    /**
     * Vaults Economy-Schnittstelle (und damit auch jeder darauf registrierte
     * Provider wie EternalEconomy) arbeitet ausschliesslich mit double.
     * Der Bounty-Betrag selbst bleibt plugin-intern immer als BigDecimal
     * exakt (siehe Bounty/BountyManager/AmountParser) - erst unmittelbar
     * vor dem eigentlichen Vault-Aufruf erfolgt diese unvermeidliche,
     * technisch bedingte Konvertierung.
     */
    private double toDouble(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
