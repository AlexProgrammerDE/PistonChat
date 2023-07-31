package net.pistonmaster.pistonchat.tools;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
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

@RequiredArgsConstructor
public class HardIgnoreTool {
    private final PistonChat plugin;

    public HardReturn hardIgnorePlayer(Player player, Player ignored) {
        try (Connection connection = plugin.getDs().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `pistonchat_hard_ignores` WHERE `uuid`=? AND `ignored_uuid`=?");
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.setString(2, ignored.getUniqueId().toString());

            if (preparedStatement.executeQuery().next()) {
                preparedStatement = connection.prepareStatement("DELETE FROM `pistonchat_hard_ignores` WHERE `uuid`=? AND `ignored_uuid`=?");
                preparedStatement.setString(1, player.getUniqueId().toString());
                preparedStatement.setString(2, ignored.getUniqueId().toString());
                preparedStatement.execute();
                return HardReturn.UN_IGNORE;
            } else {
                preparedStatement = connection.prepareStatement("INSERT INTO `pistonchat_hard_ignores` (`uuid`, `ignored_uuid`) VALUES (?, ?)");
                preparedStatement.setString(1, player.getUniqueId().toString());
                preparedStatement.setString(2, ignored.getUniqueId().toString());
                preparedStatement.execute();
                return HardReturn.IGNORE;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean isHardIgnored(CommandSender chatter, Player receiver) {
        UUID chatterUUID = new UniqueSender(chatter).getUniqueId();

        try (Connection connection = plugin.getDs().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `pistonchat_hard_ignores` WHERE `uuid`=? AND `ignored_uuid`=?");
            preparedStatement.setString(1, receiver.getUniqueId().toString());
            preparedStatement.setString(2, chatterUUID.toString());
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

    public enum HardReturn {
        IGNORE, UN_IGNORE
    }
}
