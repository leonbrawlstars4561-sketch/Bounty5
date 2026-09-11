package dev.bountyplugin.gui;

import java.math.BigDecimal;
import java.util.UUID;

public final class PendingBounty {

    private final UUID placerUuid;
    private final UUID targetUuid;
    private final String targetName;
    private final BigDecimal amount;

    public PendingBounty(UUID placerUuid, UUID targetUuid, String targetName, BigDecimal amount) {
        this.placerUuid = placerUuid;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.amount = amount;
    }

    public UUID getPlacerUuid() {
        return placerUuid;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
