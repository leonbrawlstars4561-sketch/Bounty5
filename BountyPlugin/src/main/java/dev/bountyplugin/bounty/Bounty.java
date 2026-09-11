package dev.bountyplugin.bounty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class Bounty {

    private final UUID targetUuid;
    private final String targetName;
    private BigDecimal amount;
    private final Instant createdAt;

    public Bounty(UUID targetUuid, String targetName, BigDecimal amount, Instant createdAt) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.amount = amount;
        this.createdAt = createdAt;
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

    public void increase(BigDecimal additional) {
        this.amount = this.amount.add(additional);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
