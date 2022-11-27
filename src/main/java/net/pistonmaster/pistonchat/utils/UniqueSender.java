package net.pistonmaster.pistonchat.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class UniqueSender {
    private static final Map<CommandSender, UUID> customUUID = new HashMap<>();
    @Getter
    private final CommandSender sender;

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

    public String getDisplayName() {
        if (sender instanceof Player player) {
            if (PistonChat.getPlugin(PistonChat.class).getConfig().getBoolean("stripnamecolor")) {
                return ChatColor.stripColor(player.getDisplayName());
            } else {
                return player.getDisplayName();
            }
        } else if (sender instanceof ConsoleCommandSender) {
            return ChatColor.translateAlternateColorCodes('&', PistonChat.getPlugin(PistonChat.class).getConfig().getString("consolename"));
        } else {
            return sender.getName();
        }
    }
}
