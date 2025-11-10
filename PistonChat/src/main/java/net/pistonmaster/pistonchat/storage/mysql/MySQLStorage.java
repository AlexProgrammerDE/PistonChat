package net.pistonmaster.pistonchat.storage.mysql;

import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonchat.storage.PCStorage;
import net.pistonmaster.pistonchat.utils.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.mariadb.jdbc.MariaDbPoolDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class MySQLStorage implements PCStorage {
  private final MariaDbPoolDataSource ds;

  public MySQLStorage(Logger log, ConfigManager configManager) {
    log.info(ChatColor.DARK_GREEN + "Connecting to database");
    ds = new MariaDbPoolDataSource();
    FileConfiguration config = configManager.get();
    try {
      ds.setUser(config.getString("mysql.username"));
      ds.setPassword(config.getString("mysql.password"));
      ds.setUrl("jdbc:mariadb://" + config.getString("mysql.host") + ":" + config.getInt("mysql.port") +
          "/" + config.getString("mysql.database")
          + "?sslMode=disable&serverTimezone=UTC&maxPoolSize=10"
      );

      try (Connection connection = ds.getConnection();
           var stmt1 = connection.createStatement();
           var stmt2 = connection.createStatement();
           var stmt3 = connection.createStatement()) {
        stmt1.execute("CREATE TABLE IF NOT EXISTS `pistonchat_settings_chat` (`uuid` VARCHAR(36) NOT NULL," +
            "`chat_enabled` tinyint(1) NOT NULL," +
            "PRIMARY KEY (`uuid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        stmt2.execute("CREATE TABLE IF NOT EXISTS `pistonchat_settings_whisper` (`uuid` VARCHAR(36) NOT NULL," +
            "`whisper_enabled` tinyint(1) NOT NULL," +
            "PRIMARY KEY (`uuid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        stmt3.execute("CREATE TABLE IF NOT EXISTS `pistonchat_hard_ignores` (`uuid` VARCHAR(36) NOT NULL," +
            "`ignored_uuid` VARCHAR(36) NOT NULL," +
            "PRIMARY KEY (`uuid`, `ignored_uuid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    log.info(ChatColor.DARK_GREEN + "Connected to database");
  }

  @Override
  public void setChatEnabled(UUID uuid, boolean enabled) {
    try (Connection connection = ds.getConnection();
         PreparedStatement statement = connection.prepareStatement("INSERT INTO `pistonchat_settings_chat` (`uuid`, `chat_enabled`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `chat_enabled` = ?;")) {
      statement.setString(1, uuid.toString());
      statement.setBoolean(2, enabled);
      statement.setBoolean(3, enabled);
      statement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isChatEnabled(UUID uuid) {
    try (Connection connection = ds.getConnection();
         PreparedStatement statement = connection.prepareStatement("SELECT `chat_enabled` FROM `pistonchat_settings_chat` WHERE `uuid` = ?;")) {
      statement.setString(1, uuid.toString());

      try (ResultSet resultSet = statement.executeQuery()) {
        if (!resultSet.next()) {
          return true;
        }

        return resultSet.getBoolean("chat_enabled");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setWhisperingEnabled(UUID uuid, boolean enabled) {
    try (Connection connection = ds.getConnection();
         PreparedStatement statement = connection.prepareStatement("INSERT INTO `pistonchat_settings_whisper` (`uuid`, `whisper_enabled`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `whisper_enabled` = ?;")) {
      statement.setString(1, uuid.toString());
      statement.setBoolean(2, enabled);
      statement.setBoolean(3, enabled);
      statement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isWhisperingEnabled(UUID uuid) {
    try (Connection connection = ds.getConnection();
         PreparedStatement statement = connection.prepareStatement("SELECT `whisper_enabled` FROM `pistonchat_settings_whisper` WHERE `uuid` = ?;")) {
      statement.setString(1, uuid.toString());

      try (ResultSet resultSet = statement.executeQuery()) {
        if (!resultSet.next()) {
          return true;
        }

        return resultSet.getBoolean("whisper_enabled");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public HardReturn hardIgnorePlayer(UUID ignoringReceiver, UUID ignoredChatter) {
    try (Connection connection = ds.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `pistonchat_hard_ignores` WHERE `uuid`=? AND `ignored_uuid`=?")) {
      preparedStatement.setString(1, ignoringReceiver.toString());
      preparedStatement.setString(2, ignoredChatter.toString());

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          try (PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM `pistonchat_hard_ignores` WHERE `uuid`=? AND `ignored_uuid`=?")) {
            deleteStmt.setString(1, ignoringReceiver.toString());
            deleteStmt.setString(2, ignoredChatter.toString());
            deleteStmt.execute();
          }
          return HardReturn.UN_IGNORE;
        } else {
          try (PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO `pistonchat_hard_ignores` (`uuid`, `ignored_uuid`) VALUES (?, ?)")) {
            insertStmt.setString(1, ignoringReceiver.toString());
            insertStmt.setString(2, ignoredChatter.toString());
            insertStmt.execute();
          }
          return HardReturn.IGNORE;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isHardIgnored(UUID chatter, UUID receiver) {
    try (Connection connection = ds.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `pistonchat_hard_ignores` WHERE `uuid`=? AND `ignored_uuid`=?")) {
      preparedStatement.setString(1, receiver.toString());
      preparedStatement.setString(2, chatter.toString());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        return resultSet.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<UUID> getIgnoredList(UUID uuid) {
    try (Connection connection = ds.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `pistonchat_hard_ignores` WHERE `uuid`=?")) {
      preparedStatement.setString(1, uuid.toString());

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        List<UUID> uuids = new ArrayList<>();
        while (resultSet.next()) {
          uuids.add(UUID.fromString(resultSet.getString("ignored_uuid")));
        }

        return uuids;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void clearIgnoredPlayers(UUID player) {
    try (Connection connection = ds.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `pistonchat_hard_ignores` WHERE `uuid`=?")) {
      preparedStatement.setString(1, player.toString());
      preparedStatement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
