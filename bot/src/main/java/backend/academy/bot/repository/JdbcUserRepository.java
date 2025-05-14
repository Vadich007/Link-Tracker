package backend.academy.bot.repository;

import backend.academy.bot.configs.DbConfig;
import backend.academy.bot.db.JdbcUtils;
import backend.academy.bot.schemas.models.User;
import backend.academy.bot.schemas.models.UserStates;
import backend.academy.bot.schemas.requests.AddLinkRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "jdbc")
public class JdbcUserRepository implements UserRepository {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Connection connection;

    public JdbcUserRepository(DbConfig config) {
        connection = JdbcUtils.getConnection(config.url(), config.username(), config.password());
    }

    @Override
    @SneakyThrows
    public boolean contain(long chatId) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT COUNT(*) FROM users WHERE chat_Id = ?", chatId);
        return rs != null && rs.next() && rs.getInt(1) == 1;
    }

    @Override
    public void addUser(long chatId) {
        JdbcUtils.update(connection, "INSERT INTO users VALUES (?, ?, NULL)", chatId, UserStates.FREE.toString());
    }

    @Override
    @SneakyThrows
    public User getUser(long chatId) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT * FROM users WHERE chat_Id = ?", chatId);
        if (rs != null && rs.next()) {
            String rawRequest = rs.getString("add_link_request");
            AddLinkRequest request = null;

            if (rawRequest != null) {
                request = objectMapper.readValue(rawRequest, AddLinkRequest.class);
            }

            return new User(rs.getLong("chat_id"), UserStates.valueOf(rs.getString("state")), request);

        } else {
            return null;
        }
    }

    @Override
    public void updateState(long chatId, UserStates state) {
        JdbcUtils.update(connection, "UPDATE users SET state = ? WHERE chat_Id = ?", state.toString(), chatId);
    }

    @Override
    public void addUrl(long chatId, String url) {
        AddLinkRequest addLinkRequest = new AddLinkRequest(url, null, null);
        JdbcUtils.update(
                connection,
                "UPDATE users SET add_link_request = ? WHERE chat_Id = ?",
                addLinkRequest.toString(),
                chatId);
    }

    @Override
    @SneakyThrows
    public void addTags(long chatId, List<String> tags) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT add_link_request FROM users WHERE chat_Id = ?", chatId);
        if (rs.next()) {
            String rawRequest = rs.getString("add_link_request");
            AddLinkRequest addLinkRequest = objectMapper.readValue(rawRequest, AddLinkRequest.class);
            addLinkRequest.tags(tags);

            JdbcUtils.update(
                    connection,
                    "UPDATE users SET add_link_request = ? WHERE chat_Id = ?",
                    addLinkRequest.toString(),
                    chatId);
        }
    }

    @Override
    @SneakyThrows
    public void addFilters(long chatId, List<String> filters) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT add_link_request FROM users WHERE chat_Id = ?", chatId);
        if (rs.next()) {
            String rawRequest = rs.getString("add_link_request");
            AddLinkRequest addLinkRequest = objectMapper.readValue(rawRequest, AddLinkRequest.class);
            addLinkRequest.filters(filters);
            JdbcUtils.update(
                    connection,
                    "UPDATE users SET add_link_request = ? WHERE chat_Id = ?",
                    addLinkRequest.toString(),
                    chatId);
        }
    }

    @Override
    @SneakyThrows
    public AddLinkRequest getAddLinkRequest(long chatId) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT add_link_request FROM users WHERE chat_Id = ?", chatId);
        if (rs != null && rs.next()) {
            String rawRequest = rs.getString("add_link_request");
            if (rawRequest == null) return null;
            return objectMapper.readValue(rawRequest, AddLinkRequest.class);
        } else {
            return null;
        }
    }

    @Override
    public void deleteAddLinkRequest(long chatId) {
        JdbcUtils.update(connection, "UPDATE users SET add_link_request = NULL WHERE chat_Id = ?", chatId);
    }

    @Override
    public void clear() {
        JdbcUtils.update(connection, "DELETE FROM users");
    }

    @Override
    @SneakyThrows
    public int size() {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT COUNT(*) FROM users");
        if (rs != null && rs.next()) return rs.getInt(1);
        else return -1;
    }
}
