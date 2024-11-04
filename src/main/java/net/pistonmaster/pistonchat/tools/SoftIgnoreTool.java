package net.pistonmaster.pistonchat.tools;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.utils.UniqueSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class SoftIgnoreTool {
    private static final String METADATA_KEY = "pistonchat_softignore";
    private final PistonChat plugin;
    private final Gson gson = new Gson();

    public SoftReturn softIgnorePlayer(Player player, Player ignored) {
        List<UUID> list = new ArrayList<>(getStoredList(player));

        boolean contains = list.contains(ignored.getUniqueId());
        if (contains) {
            list.remove(ignored.getUniqueId());
        } else {
            list.add(ignored.getUniqueId());
        }

        player.setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, gson.toJson(list)));

        return contains ? SoftReturn.UN_IGNORE : SoftReturn.IGNORE;
    }

    protected boolean isSoftIgnored(CommandSender chatter, Player receiver) {
        UUID chatterUUID = new UniqueSender(chatter).getUniqueId();
        return getStoredList(receiver).contains(chatterUUID);
    }

    protected List<UUID> getStoredList(Player player) {
        List<MetadataValue> values = player.getMetadata(METADATA_KEY);
        if (values.isEmpty()) {
            return new ArrayList<>();
        }

        return gson.<List<String>>fromJson(values.get(0).asString(), List.class).stream().map(UUID::fromString).toList();
    }

    public void clearIgnoredPlayers(Player player) {
        player.removeMetadata(METADATA_KEY, plugin);
    }

    public enum SoftReturn {
        IGNORE, UN_IGNORE
    }
}
