package net.pistonmaster.pistonchat.tools;

import com.google.gson.Gson;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.utils.UniqueSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SoftIgnoreTool {
    private final Gson gson = new Gson();

    public SoftReturn softIgnorePlayer(Player player, Player ignored) {
        List<String> list = getStoredList(player);

        boolean contains = list.contains(ignored.getUniqueId().toString());
        if (contains) {
            list.remove(ignored.getUniqueId().toString());
        } else {
            list.add(ignored.getUniqueId().toString());
        }

        player.setMetadata("pistonchat_softignore", new FixedMetadataValue(PistonChat.getInstance(), gson.toJson(list)));

        return contains ? SoftReturn.UN_IGNORE : SoftReturn.IGNORE;
    }

    protected boolean isSoftIgnored(CommandSender chatter, Player receiver) {
        UUID chatterUUID = new UniqueSender(chatter).getUniqueId();
        return getStoredList(receiver).contains(chatterUUID.toString());
    }

    protected List<String> getStoredList(Player player) {
        List<MetadataValue> values = player.getMetadata("pistonchat_softignore");
        if (values.isEmpty()) {
            return new ArrayList<>();
        }

        return gson.<List<String>>fromJson(values.get(0).asString(), List.class);
    }

    public enum SoftReturn {
        IGNORE, UN_IGNORE
    }
}