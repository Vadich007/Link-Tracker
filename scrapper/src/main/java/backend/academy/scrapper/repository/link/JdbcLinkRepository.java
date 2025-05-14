package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.configs.DbConfig;
import backend.academy.scrapper.db.JdbcUtils;
import backend.academy.scrapper.schemas.responses.github.Event;
import backend.academy.scrapper.schemas.responses.stackoverflow.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@Getter
@ConditionalOnProperty(name = "app.access-type", havingValue = "jdbc")
public class JdbcLinkRepository implements LinkRepository {
    private final Connection connection;

    public JdbcLinkRepository(DbConfig config) {
        connection = JdbcUtils.getConnection(config.url(), config.username(), config.password());
    }

    @Override
    @SneakyThrows
    public boolean containLink(String url) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT COUNT(*) FROM links WHERE url = ?", url);
        return rs != null && rs.next() && rs.getInt(1) != 0;
    }

    @Override
    @SneakyThrows
    public Set<Long> getChats(String url) {
        ResultSet rs = JdbcUtils.execute(
                connection,
                "SELECT chat_id " + "FROM subscriptions JOIN links ON links.id = subscriptions.link_id "
                        + "WHERE url = ?",
                url);

        HashSet<Long> chats = new HashSet<>();
        while (rs.next()) {
            chats.add(rs.getLong("chat_id"));
        }
        return chats;
    }

    @Override
    public void deleteLink(String url) {
        JdbcUtils.update(connection, "DELETE FROM links WHERE url = ?", url);
    }

    @Override
    public void addLink(String url) {
        String type = url.startsWith("https://github.com/") ? "gitHub" : "stackOverflow";
        JdbcUtils.update(connection, "INSERT INTO links (url, type, last_event) VALUES (?, ?, NULL)", url, type);
    }

    @SneakyThrows
    @Override
    public Long getLinkId(String url) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT id FROM links WHERE url = ?", url);
        if (rs != null && rs.next()) {
            return rs.getLong("id");
        } else {
            return -1L;
        }
    }

    @SneakyThrows
    @Override
    public Set<String> getUrls(int limit, int offset) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT url FROM links OFFSET ? LIMIT ?", offset, limit);
        Set<String> urls = new HashSet<>();
        while (rs.next()) {
            urls.add(rs.getString("url"));
        }
        return urls;
    }

    @Override
    @SneakyThrows
    public void deleteChat(String url, long chatId) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT id FROM links WHERE url = ?", url);
        if (rs != null && rs.next()) {
            JdbcUtils.update(
                    connection,
                    "DELETE FROM subscriptions WHERE link_id = ? AND chat_id = ?",
                    rs.getLong("id"),
                    chatId);
        }
    }

    @Override
    public void clear() {
        JdbcUtils.update(connection, "DELETE FROM links");
        JdbcUtils.update(connection, "DELETE FROM subscriptions");
    }

    @Override
    @SneakyThrows
    public int size() {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT COUNT(*) FROM links");
        if (rs != null && rs.next()) return rs.getInt(1);
        else return -1;
    }

    @Override
    @SneakyThrows
    public boolean isLastEvent(String url, Event event) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT last_event FROM links WHERE url = ?", url);
        if (rs != null && rs.next()) {
            String lastEvent = rs.getString("last_event");
            if (lastEvent == null) return false;
            Event dbEvent = new ObjectMapper().readValue(rs.getString("last_event"), Event.class);
            return event.equals(dbEvent);
        } else {
            return false;
        }
    }

    @Override
    @SneakyThrows
    public boolean isLastEvent(String url, Long postId) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT last_event FROM links WHERE url = ?", url);
        if (rs != null && rs.next()) {
            String lastEvent = rs.getString("last_event");
            if (lastEvent == null) return false;
            Item dbItem = new ObjectMapper().readValue(rs.getString("last_event"), Item.class);
            return postId.equals(dbItem.postId());
        } else {
            return false;
        }
    }

    @Override
    public void setLastEvent(String url, Item item) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JdbcUtils.update(
                    connection, "UPDATE links SET last_event = ? WHERE url = ?", mapper.writeValueAsString(item), url);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLastEvent(String url, Event event) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JdbcUtils.update(
                    connection, "UPDATE links SET last_event = ? WHERE url = ?", mapper.writeValueAsString(event), url);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SneakyThrows
    public Event getLastGitHubEvent(String url) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT last_event FROM links WHERE url = ?", url);
        if (rs != null && rs.next()) {
            return new ObjectMapper().readValue(rs.getString("last_event"), Event.class);
        } else {
            return null;
        }
    }

    @Override
    @SneakyThrows
    public Item getLastStackOverflowEvent(String url) {
        ResultSet rs = JdbcUtils.execute(connection, "SELECT last_event FROM links WHERE url = ?", url);
        if (rs != null && rs.next()) {
            return new ObjectMapper().readValue(rs.getString("last_event"), Item.class);
        } else {
            return null;
        }
    }
}
