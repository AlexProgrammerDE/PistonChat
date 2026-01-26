package net.pistonmaster.pistonchat.utils;

import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class MiniPlaceholdersHook {
  public static TagResolver relationalGlobalPlaceholders() {
    return MiniPlaceholders.relationalGlobalPlaceholders();
  }
}
