package net.pistonmaster.pistonchat.tools;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.utils.UniqueSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class HardIgnoreTool {
    private final PistonChat plugin;
    private final LoadingCache<IgnorePair, Boolean> ignoreCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(this::loadIsHardIgnored);

    public HardReturn hardIgnorePlayer(Player ignoringReceiver, Player ignoredChatter) {
        try (Connection connection = plugin.getDs().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `pistonchat_hard_ignores` WHERE `uuid`=? AND `ignored_uuid`=?");
            preparedStatement.setString(1, ignoringReceiver.getUniqueId().toString());
            preparedStatement.setString(2, ignoredChatter.getUniqueId().toString());

            if (preparedStatement.executeQuery().next()) {
                preparedStatement = connection.prepareStatement("DELETE FROM `pistonchat_hard_ignores` WHERE `uuid`=? AND `ignored_uuid`=?");
                preparedStatement.setString(1, ignoringReceiver.getUniqueId().toString());
                preparedStatement.setString(2, ignoredChatter.getUniqueId().toString());
                preparedStatement.execute();

                ignoreCache.put(new IgnorePair(ignoredChatter.getUniqueId(), ignoringReceiver.getUniqueId()), false);
                return HardReturn.UN_IGNORE;
            } else {
                preparedStatement = connection.prepareStatement("INSERT INTO `pistonchat_hard_ignores` (`uuid`, `ignored_uuid`) VALUES (?, ?)");
                preparedStatement.setString(1, ignoringReceiver.getUniqueId().toString());
                preparedStatement.setString(2, ignoredChatter.getUniqueId().toString());
                preparedStatement.execute();

                ignoreCache.put(new IgnorePair(ignoredChatter.getUniqueId(), ignoringReceiver.getUniqueId()), true);
                return HardReturn.IGNORE;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean isHardIgnored(CommandSender chatter, Player receiver) {
        UUID chatterUUID = new UniqueSender(chatter).getUniqueId();

        return Boolean.TRUE.equals(ignoreCache.get(new IgnorePair(chatterUUID, receiver.getUniqueId())));
    }

    private boolean loadIsHardIgnored(IgnorePair pair) {
        try (Connection connection = plugin.getDs().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `pistonchat_hard_ignores` WHERE `uuid`=? AND `ignored_uuid`=?");
            preparedStatement.setString(1, pair.receiver().toString());
            preparedStatement.setString(2, pair.chatter().toString());
            return preparedStatement.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UUID> getStoredList(Player player) {
        try (Connection connection = plugin.getDs().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `pistonchat_hard_ignores` WHERE `uuid`=?");
            preparedStatement.setString(1, player.getUniqueId().toString());

            ResultSet resultSet = preparedStatement.executeQuery();

            List<UUID> uuids = new ArrayList<>();
            while (resultSet.next()) {
                uuids.add(UUID.fromString(resultSet.getString("ignored_uuid")));
            }

            return uuids;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearIgnoredPlayers(Player player) {
        try (Connection connection = plugin.getDs().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `pistonchat_hard_ignores` WHERE `uuid`=?");
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.execute();

            ignoreCache.asMap().keySet().removeIf(pair -> pair.chatter().equals(player.getUniqueId()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public enum HardReturn {
        IGNORE, UN_IGNORE
    }

    private record IgnorePair(UUID chatter, UUID receiver) {
    }
}
