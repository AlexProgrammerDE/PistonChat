package net.pistonmaster.pistonchat.hooks;

import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.miniplaceholders.api.types.RelationalAudience;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.pistonmaster.pistonchat.PistonChat;

public class MiniPlaceholdersHook {
  public static TagResolver relationalGlobalPlaceholders(PistonChat plugin) {
    if (plugin.getServer().getPluginManager().isPluginEnabled("MiniPlaceholders")) {
      return MiniPlaceholders.relationalGlobalPlaceholders();
    } else {
      return TagResolver.empty();
    }
  }

  public static Pointered getRelationalAudience(PistonChat plugin, Audience source, Audience target) {
    if (plugin.getServer().getPluginManager().isPluginEnabled("MiniPlaceholders")) {
      return new RelationalAudience<>(source, target);
    } else {
      return target;
    }
  }

  public static TagResolver audienceGlobalPlaceholders(PistonChat plugin) {
    if (plugin.getServer().getPluginManager().isPluginEnabled("MiniPlaceholders")) {
      return MiniPlaceholders.audienceGlobalPlaceholders();
    } else {
      return TagResolver.empty();
    }
  }
}
