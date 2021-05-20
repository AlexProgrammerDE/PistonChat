package net.pistonmaster.pistonchat.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class UniqueSender {
    private static final Map<CommandSender, UUID> consoleUUID = new HashMap<>();
    @Getter
    private final CommandSender sender;

    public static CommandSender byUUID(UUID uuid) {
        for (Map.Entry<CommandSender, UUID> entry : consoleUUID.entrySet()) {
            if (entry.getValue().equals(uuid)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public UUID getUniqueId() {
        if (sender instanceof Player) {
            return ((Player) sender).getUniqueId();
        } else {
            consoleUUID.computeIfAbsent(sender, sender2 -> UUID.randomUUID());

            return consoleUUID.get(sender);
        }
    }

    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    public CommandSender.Spigot spigot() {
        return sender.spigot();
    }

    public String getDisplayName() {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (ConfigTool.getConfig().getBoolean("stripnamecolor")) {
                return ChatColor.stripColor(player.getDisplayName());
            } else {
                return player.getDisplayName();
            }
        } else if (sender instanceof ConsoleCommandSender) {
            return ChatColor.translateAlternateColorCodes('&', ConfigTool.getConfig().getString("consolename"));
        } else {
            return sender.getName();
        }
    }
}
