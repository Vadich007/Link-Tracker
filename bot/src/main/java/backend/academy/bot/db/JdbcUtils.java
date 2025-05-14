package backend.academy.bot.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcUtils {
    public static Connection getConnection(String url, String username, String password) {
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            log.info("Open connection: {}", connection.getClientInfo("name"));
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException e) {
            log.error("Error when trying to open a connection: {}", e.getMessage());
            return null;
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                log.info("Close connection: {}", connection);
            } catch (SQLException e) {
                log.error("Error when trying to close the connection: {}", e.getMessage());
            }
        }
    }

    public static ResultSet execute(Connection connection, String sql, Object... params) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            ResultSet rs = preparedStatement.executeQuery();
            log.info("SQL query sent successfully {}", preparedStatement);
            return rs;
        } catch (SQLException e) {
            log.info("Failed attempt to send sql query {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static int update(Connection connection, String sql, Object... params) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            int num = preparedStatement.executeUpdate();
            connection.commit();
            log.info("SQL query sent successfully {}", preparedStatement);
            return num;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            log.info("Failed attempt to send sql query {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
