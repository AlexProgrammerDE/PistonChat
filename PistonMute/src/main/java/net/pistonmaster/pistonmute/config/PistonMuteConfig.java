package net.pistonmaster.pistonmute.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class PistonMuteConfig {
  @Comment({
      "When enabled, muted players can still send messages but only they can see them.",
      "Other players won't receive the messages, making the mute invisible to the muted player."
  })
  public boolean shadowMute = true;

  @Comment({
      "",
      "Staff Hierarchy Settings",
      "Enable staff immunity/hierarchy system to prevent lower-ranked staff from muting higher-ranked."
  })
  public boolean staffHierarchyEnabled = true;

  @Comment({
      "Hierarchy mode: 'immune' or 'weight'",
      "immune: Players with pistonmute.immune permission cannot be muted",
      "weight: Players with higher pistonmute.weight.<number> can mute those with lower weights"
  })
  public String hierarchyMode = "immune";

  @Comment({
      "",
      "Command Logging Settings",
      "Enable logging of all punishment commands to punishments.log"
  })
  public boolean commandLoggingEnabled = true;

  @Comment("Format for log entries. Placeholders: %timestamp%, %staff%, %command%, %target%, %details%")
  public String logFormat = "[%timestamp%] [%staff%] [%command%] [%target%] %details%";

  @Comment({
      "",
      "Notes System Settings",
      "Enable the staff notes system for players"
  })
  public boolean notesEnabled = true;

  @Comment({
      "",
      "Command Blocking Settings",
      "Block muted players from using chat-related commands."
  })
  public boolean blockCommands = true;

  @Comment({
      "List of commands that muted players cannot use.",
      "Commands should be listed without the leading slash.",
      "Aliases are automatically handled (e.g., 'msg' also blocks 'tell', 'w', etc.)"
  })
  public List<String> blockedCommands = List.of(
      "me",
      "say",
      "msg",
      "tell",
      "w",
      "whisper",
      "reply",
      "r",
      "mail",
      "m",
      "pm",
      "dm",
      "message"
  );

  @Comment({
      "Message shown to muted players when they try to use a blocked command.",
      "Supports color codes with &."
  })
  public String blockedCommandMessage = "&cYou cannot use this command while muted!";

  @Comment({
      "",
      "Alt Account Detection Settings",
      "Track player IPs to detect and manage alt accounts."
  })
  public boolean enableAltDetection = true;

  @Comment({
      "Automatically mute all known alt accounts when muting a player.",
      "This uses IP tracking to identify alt accounts."
  })
  public boolean autoMuteAlts = false;

  @Comment({
      "Notify staff when a muted player's alt joins the server.",
      "Players with pistonmute.notify permission will be notified."
  })
  public boolean notifyStaffOnAltJoin = true;

  @Comment({
      "Message shown to staff when a muted player's alt joins.",
      "Placeholders: %player%, %alt%, %ip%"
  })
  public String altJoinNotifyMessage = "&e[PistonMute] &c%player% &emay be an alt of muted player &c%alt%";

  @Comment({
      "",
      "Mute Notification Settings",
      "Notify players about their mute status."
  })
  public boolean notifyOnJoin = true;

  @Comment({
      "Message shown to muted players when they join the server.",
      "Supports color codes with &."
  })
  public String muteNotifyMessage = "&c&lYou are currently muted and cannot chat.";

  @Comment({
      "Show mute reason to the player (if one was provided)."
  })
  public boolean showMuteReason = true;

  @Comment({
      "Message format for showing mute reason.",
      "Placeholder: %reason%"
  })
  public String muteReasonFormat = "&cReason: &7%reason%";

  @Comment({
      "",
      "Warning System Settings",
      "Enable the warning system for players."
  })
  public boolean warningsEnabled = true;

  @Comment({
      "Time in days after which warnings expire.",
      "Set to 0 for warnings that never expire."
  })
  public int warningExpiryDays = 30;

  @Comment("Maximum warnings before auto-escalation kicks in.")
  public int maxWarningsBeforeAction = 3;

  @Comment({
      "Action to take when a player reaches max warnings.",
      "Options: none, mute, tempmute",
      "If tempmute, uses the duration specified below."
  })
  public String warningMaxAction = "tempmute";

  @Comment("Duration for tempmute when max warnings reached (e.g., 1h, 1d, 1w).")
  public String warningMaxActionDuration = "1h";

  @Comment({
      "",
      "Progressive Punishment Escalation Templates",
      "Define escalation rules for different violation types.",
      "Format: template_name -> offense_number -> duration",
      "Use 'permanent' for permanent mutes.",
      "Duration format: Xs (seconds), Xm (minutes), Xh (hours), Xd (days), Xw (weeks)"
  })
  public Map<String, Map<Integer, String>> escalation = createDefaultEscalation();

  private static Map<String, Map<Integer, String>> createDefaultEscalation() {
    Map<String, Map<Integer, String>> templates = new LinkedHashMap<>();

    // Swearing template
    Map<Integer, String> swearing = new LinkedHashMap<>();
    swearing.put(1, "10m");
    swearing.put(2, "1h");
    swearing.put(3, "1d");
    swearing.put(4, "permanent");
    templates.put("swearing", swearing);

    // Spam template
    Map<Integer, String> spam = new LinkedHashMap<>();
    spam.put(1, "5m");
    spam.put(2, "30m");
    spam.put(3, "6h");
    spam.put(4, "1d");
    spam.put(5, "permanent");
    templates.put("spam", spam);

    // Advertising template
    Map<Integer, String> advertising = new LinkedHashMap<>();
    advertising.put(1, "1h");
    advertising.put(2, "1d");
    advertising.put(3, "permanent");
    templates.put("advertising", advertising);

    // Harassment template
    Map<Integer, String> harassment = new LinkedHashMap<>();
    harassment.put(1, "1h");
    harassment.put(2, "1d");
    harassment.put(3, "7d");
    harassment.put(4, "permanent");
    templates.put("harassment", harassment);

    return templates;
  }

  @Comment({
      "",
      "Warning Messages",
      "Customize warning-related messages."
  })
  public String warnedMessage = "&c&lYou have been warned! &eReason: &7%reason%";

  @Comment("Message shown when a player receives a warning.")
  public String warnedByMessage = "&7Warned by: &e%issuer%";

  @Comment("Message format for listing warnings.")
  public String warningListFormat = "&7[%id%] &e%reason% &7- by %issuer% (%time_ago%)";

  @Comment("Message shown when a warning is about to expire.")
  public String warningExpiryNote = "&7(Expires in %time%)";

  @Comment({
      "",
      "Punishment History Settings",
      "Configure the punishment history display."
  })
  public int historyPageSize = 10;

  @Comment("Format for punishment history entries.")
  public String historyEntryFormat = "&7[%type%] &e%reason% &7- %duration% by %issuer% (%date%)";

  @Comment("Header format for punishment history.")
  public String historyHeader = "&6--- Punishment History for %player% (Page %page%/%total%) ---";

  @Comment("Footer format for punishment history.")
  public String historyFooter = "&7Use /history %player% <page> to view more.";

  @Comment({
      "",
      "Mute Info Messages",
      "Messages for mute information display."
  })
  public String muteInfoHeader = "&6--- Mute Info for %player% ---";

  @Comment("Format for mute info details.")
  public String muteInfoReason = "&7Reason: &e%reason%";

  public String muteInfoIssuer = "&7Muted by: &e%issuer%";

  public String muteInfoDuration = "&7Duration: &e%duration%";

  public String muteInfoExpires = "&7Expires: &e%expires%";

  public String muteInfoPermanent = "&cPermanent";

  public String muteInfoTemplate = "&7Template: &e%template%";

  @Comment({
      "",
      "Mute List Messages"
  })
  public String muteListHeader = "&6--- Muted Players (Page %page%/%total%) ---";

  public String muteListEntryFormat = "&e%player% &7- %duration% &8(%reason%)";

  public String muteListFooter = "&7Use /mutelist <page> to view more.";
}
