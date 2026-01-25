package net.pistonmaster.pistonmute.data;

/**
 * Enumeration of punishment types for history tracking.
 */
public enum PunishmentType {
  MUTE("Mute"),
  WARNING("Warning");

  private final String displayName;

  PunishmentType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
