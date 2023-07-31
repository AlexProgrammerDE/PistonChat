package net.pistonmaster.pistonchat.utils;

import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record UniqueSender(CommandSender sender) {
    private static final Map<CommandSender, UUID> customUUID = new HashMap<>();

    public static Optional<CommandSender> byUUID(UUID uuid) {
        for (Map.Entry<CommandSender, UUID> entry : customUUID.entrySet()) {
            if (entry.getValue().equals(uuid)) {
                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }

    public UUID getUniqueId() {
        if (sender instanceof Player player) {
            return player.getUniqueId();
        } else {
            return customUUID.computeIfAbsent(sender, sender2 -> UUID.randomUUID());
        }
    }
}
