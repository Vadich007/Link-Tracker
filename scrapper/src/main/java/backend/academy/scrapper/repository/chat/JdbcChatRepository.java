package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.configs.DbConfig;
import backend.academy.scrapper.db.JdbcUtils;
import backend.academy.scrapper.schemas.models.Link;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        ResultSet rs = JdbcUtils.execute(connection, "SELECT COUNT(*) FROM chats WHERE chat_Id = ?", chatId);
        return rs != null && rs.next() && rs.getInt(1) != 0;
    }

    @Override
    @SneakyThrows
    public List<Link> getLinks(Long chatId) {
        ResultSet rs = JdbcUtils.execute(
                connection,
                "SELECT links.id as link_id, subscriptions.tags, links.url, subscriptions.filters "
                        + "FROM subscriptions JOIN links ON links.id = subscriptions.link_id "
                        + "WHERE chat_id = ?",
                chatId);

        List<Link> links = new ArrayList<>();
        while (rs.next()) {
            String tags = rs.getString("tags");
            String filters = rs.getString("filters");

            links.add(new Link(
                    rs.getLong("link_id"),
                    rs.getString("url"),
                    tags != null ? Arrays.asList(tags.split(",")) : null,
                    filters != null ? Arrays.asList(filters.split(",")) : null));
        }
        return links;
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
                "INSERT INTO subscriptions VALUES (?, ?, ?, ?)",
                chatId,
                link.id(),
                link.tags() != null ? String.join(",", link.tags()) : null,
                link.filters() != null ? String.join(",", link.filters()) : null);
    }

    @Override
    public void clear() {
        JdbcUtils.update(connection, "DELETE FROM chats");
        JdbcUtils.update(connection, "DELETE FROM subscriptions");
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
