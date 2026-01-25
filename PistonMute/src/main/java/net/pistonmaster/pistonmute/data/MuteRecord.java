package net.pistonmaster.pistonmute.data;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a mute record with extended information.
 */
public class MuteRecord {
  private final String id;
  private final UUID playerUuid;
  private final UUID issuerUuid;
  private final String issuerName;
  private final String reason;
  private final String template;
  private final Instant issuedAt;
  private final Instant expiresAt;
  private final boolean permanent;

  public MuteRecord(String id, UUID playerUuid, UUID issuerUuid, String issuerName,
                    String reason, String template, Instant issuedAt, Instant expiresAt, boolean permanent) {
    this.id = id;
    this.playerUuid = playerUuid;
    this.issuerUuid = issuerUuid;
    this.issuerName = issuerName;
    this.reason = reason;
    this.template = template;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
    this.permanent = permanent;
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

  public String getTemplate() {
    return template;
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public boolean isPermanent() {
    return permanent;
  }

  public boolean isExpired() {
    if (permanent || expiresAt == null) {
      return false;
    }
    return Instant.now().isAfter(expiresAt);
  }

  public boolean isActive() {
    return !isExpired();
  }
}
