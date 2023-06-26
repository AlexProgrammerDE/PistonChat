package net.pistonmaster.pistonchat.tools;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@RequiredArgsConstructor
public class TempDataTool {
    private final PistonChat plugin;

    public void setWhisperingEnabled(Player player, boolean value) {
        try (Connection connection = plugin.getDs().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO `pistonchat_settings_whisper` (`uuid`, `whisper_enabled`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `whisper_enabled` = ?;");
            statement.setString(1, player.getUniqueId().toString());
            statement.setBoolean(2, value);
            statement.setBoolean(3, value);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setChatEnabled(Player player, boolean value) {
        try (Connection connection = plugin.getDs().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO `pistonchat_settings_chat` (`uuid`, `chat_enabled`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `chat_enabled` = ?;");
            statement.setString(1, player.getUniqueId().toString());
            statement.setBoolean(2, value);
            statement.setBoolean(3, value);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isWhisperingEnabled(Player player) {
        try (Connection connection = plugin.getDs().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `whisper_enabled` FROM `pistonchat_settings_whisper` WHERE `uuid` = ?;");
            statement.setString(1, player.getUniqueId().toString());
            return statement.executeQuery().getBoolean("whisper_enabled");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isChatEnabled(Player player) {
        try (Connection connection = plugin.getDs().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `chat_enabled` FROM `pistonchat_settings_chat` WHERE `uuid` = ?;");
            statement.setString(1, player.getUniqueId().toString());
            return statement.executeQuery().getBoolean("chat_enabled");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
