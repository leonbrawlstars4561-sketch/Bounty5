package dev.bountyplugin.storage;

import dev.bountyplugin.bounty.Bounty;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class YamlBountyStorage implements BountyStorage {

    private final JavaPlugin plugin;
    private final File file;

    public YamlBountyStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "bounties.yml");
    }

    @Override
    public List<Bounty> load() {
        List<Bounty> result = new ArrayList<>();

        if (!file.exists()) {
            return result;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<Map<?, ?>> rawList = config.getMapList("bounties");

        for (Map<?, ?> raw : rawList) {
            try {
                UUID targetUuid = UUID.fromString(String.valueOf(raw.get("target-uuid")));
                String targetName = String.valueOf(raw.get("target-name"));
                BigDecimal amount = new BigDecimal(String.valueOf(raw.get("amount")));
                long createdEpoch = raw.get("created-at") != null
                        ? Long.parseLong(String.valueOf(raw.get("created-at")))
                        : Instant.now().getEpochSecond();

                result.add(new Bounty(targetUuid, targetName, amount, Instant.ofEpochSecond(createdEpoch)));
            } catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING,
                        "Konnte einen Bounty-Eintrag nicht laden, er wird uebersprungen: " + raw, exception);
            }
        }

        return result;
    }

    @Override
    public synchronized void save(Collection<Bounty> bounties) {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> serialized = new ArrayList<>();

        for (Bounty bounty : bounties) {
            Map<String, Object> map = new HashMap<>();
            map.put("target-uuid", bounty.getTargetUuid().toString());
            map.put("target-name", bounty.getTargetName());
            map.put("amount", bounty.getAmount().toPlainString());
            map.put("created-at", bounty.getCreatedAt().getEpochSecond());
            serialized.add(map);
        }

        config.set("bounties", serialized);

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Konnte bounties.yml nicht speichern!", exception);
        }
    }
}
