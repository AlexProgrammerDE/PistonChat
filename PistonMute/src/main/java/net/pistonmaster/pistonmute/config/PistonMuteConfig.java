package net.pistonmaster.pistonmute.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

@Configuration
public class PistonMuteConfig {
  @Comment({
      "When enabled, muted players can still send messages but only they can see them.",
      "Other players won't receive the messages, making the mute invisible to the muted player."
  })
  public boolean shadowMute = true;
}
