package dev.bountyplugin.bounty;

import dev.bountyplugin.storage.BountyStorage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BountyManager {

    private final Map<UUID, Bounty> bounties = new ConcurrentHashMap<>();
    private final BountyStorage storage;
    private final Object writeLock = new Object();

    public BountyManager(BountyStorage storage) {
        this.storage = storage;
    }

    public void loadAll() {
        List<Bounty> loaded = storage.load();
        bounties.clear();
        for (Bounty bounty : loaded) {
            bounties.put(bounty.getTargetUuid(), bounty);
        }
    }

    public void saveAll() {
        storage.save(bounties.values());
    }

    public boolean hasBounty(UUID targetUuid) {
        return bounties.containsKey(targetUuid);
    }

    public Bounty getBounty(UUID targetUuid) {
        return bounties.get(targetUuid);
    }

    public Collection<Bounty> getAllBounties() {
        return bounties.values();
    }

    /**
     * Fuegt einen neuen Bounty hinzu oder erhoeht einen bestehenden.
     * Synchronisiert, um Race-Conditions bei gleichzeitigen Bestaetigungen
     * auf dasselbe Ziel zu verhindern.
     */
    public Bounty addOrIncrease(UUID targetUuid, String targetName, BigDecimal amount) {
        synchronized (writeLock) {
            Bounty existing = bounties.get(targetUuid);
            if (existing == null) {
                Bounty bounty = new Bounty(targetUuid, targetName, amount, Instant.now());
                bounties.put(targetUuid, bounty);
                saveAll();
                return bounty;
            }

            existing.increase(amount);
            saveAll();
            return existing;
        }
    }

    /**
     * Entfernt und liefert den Bounty atomar - genutzt bei der Auszahlung,
     * damit ein Bounty niemals doppelt ausgezahlt werden kann, selbst wenn
     * mehrere Events "gleichzeitig" eintreffen.
     */
    public Bounty takeBounty(UUID targetUuid) {
        Bounty removed = bounties.remove(targetUuid);
        if (removed != null) {
            saveAll();
        }
        return removed;
    }
}
