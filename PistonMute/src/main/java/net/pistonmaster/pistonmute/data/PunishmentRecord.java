package net.pistonmaster.pistonmute.data;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a historical punishment record.
 */
public class PunishmentRecord {
  private final String id;
  private final PunishmentType type;
  private final UUID playerUuid;
  private final UUID issuerUuid;
  private final String issuerName;
  private final String reason;
  private final Instant issuedAt;
  private final Instant expiresAt;
  private final boolean permanent;
  private final String template;

  public PunishmentRecord(String id, PunishmentType type, UUID playerUuid, UUID issuerUuid, String issuerName,
                          String reason, Instant issuedAt, Instant expiresAt, boolean permanent, String template) {
    this.id = id;
    this.type = type;
    this.playerUuid = playerUuid;
    this.issuerUuid = issuerUuid;
    this.issuerName = issuerName;
    this.reason = reason;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
    this.permanent = permanent;
    this.template = template;
  }

  public String getId() {
    return id;
  }

  public PunishmentType getType() {
    return type;
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

  public boolean isPermanent() {
    return permanent;
  }

  public String getTemplate() {
    return template;
  }

  public boolean isExpired() {
    if (permanent || expiresAt == null) {
      return false;
    }
    return Instant.now().isAfter(expiresAt);
  }

  public boolean wasActive() {
    return !isExpired();
  }
}
