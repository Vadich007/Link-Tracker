package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.configs.DbConfig;
import backend.academy.scrapper.db.JdbcUtils;
import backend.academy.scrapper.schemas.models.Link;
import java.sql.Connection;
import java.sql.ResultSet;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@Getter
@ConditionalOnProperty(name = "app.access-type", havingValue = "jdbc")
public class JdbcChatRepository implements ChatRepository {
    private Connection connection;

    public JdbcChatRepository(DbConfig config) {
        connection = JdbcUtils.getConnection(config.url(), config.username(), config.password());
    }

    @Override
    @SneakyThrows
    public boolean containChat(Long chatId) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT EXISTS(SELECT 1 FROM chats WHERE chat_Id = ?)", chatId);
        return rs.next() && rs.getBoolean(1);
    }

    @Override
    public void deleteChat(Long chatId) {
        JdbcUtils.update(connection, "DELETE FROM chats WHERE chat_Id = ?", chatId);
    }

    @Override
    public void addChat(Long chatId) {
        JdbcUtils.update(connection, "INSERT INTO chats VALUES (?)", chatId);
    }

    @Override
    public void subscribeLink(Long chatId, Link link) {
        JdbcUtils.update(
            connection,
            "INSERT INTO subscriptions (chat_id, link_id, tags, filters) VALUES (?, ?, ?, ?)",
            chatId,
            link.id(),
            link.tags() != null ? String.join(",", link.tags()) : null,
            link.filters() != null ? String.join(",", link.filters()) : null);
    }

    @Override
    @SneakyThrows
    public int size() {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT COUNT(*) FROM chats");
        if (rs != null && rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }
}
