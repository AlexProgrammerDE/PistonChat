package net.pistonmaster.pistonchat.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SoftIgnoreTool {
    private final HashMap<UUID, List<UUID>> map = new HashMap<>();

    public SoftReturn softIgnorePlayer(Player player, Player ignored) {
        indexPlayer(player);
        indexPlayer(ignored);

        List<UUID> list = map.get(player.getUniqueId());

        if (list.contains(ignored.getUniqueId())) {
            list.remove(ignored.getUniqueId());

            return SoftReturn.UNIGNORE;
        } else {
            list.add(ignored.getUniqueId());

            return SoftReturn.IGNORE;
        }
    }

    protected boolean isSoftIgnored(CommandSender chatter, CommandSender receiver) {
        indexPlayer(receiver);
        indexPlayer(chatter);

        return map.get(new UniqueSender(receiver).getUniqueId()).contains(new UniqueSender(chatter).getUniqueId());
    }

    protected List<OfflinePlayer> getSoftIgnoredPlayers(Player player) {
        indexPlayer(player);

        List<UUID> listUUID = map.get(player.getUniqueId());

        List<OfflinePlayer> returnedPlayers = new ArrayList<>();

        for (UUID uuid : listUUID) {
            returnedPlayers.add(Bukkit.getOfflinePlayer(uuid));
        }

        return returnedPlayers;
    }

    private void indexPlayer(CommandSender player) {
        if (!map.containsKey(new UniqueSender(player).getUniqueId())) {
            map.put(new UniqueSender(player).getUniqueId(), new ArrayList<>());
        }
    }

    public void onPlayerQuit(CommandSender player) {
        this.map.remove(new UniqueSender(player).getUniqueId());
    }

    public enum SoftReturn {
        IGNORE, UNIGNORE
    }
}
