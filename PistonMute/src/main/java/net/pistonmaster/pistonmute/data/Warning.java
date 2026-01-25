package net.pistonmaster.pistonmute.data;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a warning issued to a player.
 */
public class Warning {
  private final String id;
  private final UUID playerUuid;
  private final UUID issuerUuid;
  private final String issuerName;
  private final String reason;
  private final Instant issuedAt;
  private final Instant expiresAt;

  public Warning(String id, UUID playerUuid, UUID issuerUuid, String issuerName, String reason, Instant issuedAt, Instant expiresAt) {
    this.id = id;
    this.playerUuid = playerUuid;
    this.issuerUuid = issuerUuid;
    this.issuerName = issuerName;
    this.reason = reason;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
  }

  public String getId() {
    return id;
  }

  public UUID getPlayerUuid() {
    return playerUuid;
  }

  public UUID getIssuerUuid() {
    return issuerUuid;
  }

  public String getIssuerName() {
    return issuerName;
  }

  public String getReason() {
    return reason;
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public boolean isExpired() {
    if (expiresAt == null) {
      return false; // Never expires
    }
    return Instant.now().isAfter(expiresAt);
  }

  public boolean isActive() {
    return !isExpired();
  }
}
