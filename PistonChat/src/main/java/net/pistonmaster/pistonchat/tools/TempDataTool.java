package net.pistonmaster.pistonchat.tools;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class TempDataTool {
    private final PistonChat plugin;
    private final LoadingCache<UUID, Boolean> whisperCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(this::loadIsWhisperingEnabled);
    private final LoadingCache<UUID, Boolean> chatCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(this::loadIsChatEnabled);

    public void setWhisperingEnabled(Player player, boolean value) {
        try (Connection connection = plugin.getDs().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO `pistonchat_settings_whisper` (`uuid`, `whisper_enabled`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `whisper_enabled` = ?;");
            statement.setString(1, player.getUniqueId().toString());
            statement.setBoolean(2, value);
            statement.setBoolean(3, value);
            statement.execute();

            whisperCache.put(player.getUniqueId(), value);
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

            chatCache.put(player.getUniqueId(), value);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isWhisperingEnabled(Player player) {
        return Boolean.TRUE.equals(whisperCache.get(player.getUniqueId()));
    }

    private boolean loadIsWhisperingEnabled(UUID uuid) {
        try (Connection connection = plugin.getDs().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `whisper_enabled` FROM `pistonchat_settings_whisper` WHERE `uuid` = ?;");
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return true;
            }

            return resultSet.getBoolean("whisper_enabled");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isChatEnabled(Player player) {
        return Boolean.TRUE.equals(chatCache.get(player.getUniqueId()));
    }

    private boolean loadIsChatEnabled(UUID uuid) {
        try (Connection connection = plugin.getDs().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `chat_enabled` FROM `pistonchat_settings_chat` WHERE `uuid` = ?;");
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return true;
            }

            return resultSet.getBoolean("chat_enabled");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
