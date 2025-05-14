package backend.academy.scrapper;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.configs.DbConfig;
import backend.academy.scrapper.db.LiquibaseMigration;
import backend.academy.scrapper.repository.chat.JdbcChatRepository;
import backend.academy.scrapper.repository.link.JdbcLinkRepository;
import backend.academy.scrapper.schemas.models.Link;
import backend.academy.scrapper.schemas.responses.github.Event;
import backend.academy.scrapper.schemas.responses.github.Payload;
import backend.academy.scrapper.schemas.responses.github.PullRequest;
import backend.academy.scrapper.schemas.responses.github.User;
import backend.academy.scrapper.schemas.responses.stackoverflow.Item;
import backend.academy.scrapper.schemas.responses.stackoverflow.Owner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestPropertySource(properties = "app.access-type=jdbc")
public class JdbcLinkRepositoryTests {
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withExposedPorts(5432)
            .withDatabaseName("local")
            .withUsername("postgres")
            .withPassword("test");

    private static JdbcChatRepository chatRepository;

    private static JdbcLinkRepository linkRepository;

    @BeforeEach
    void setUp() {
        DbConfig config = new DbConfig(
                postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword());
        chatRepository = new JdbcChatRepository(config);
        linkRepository = new JdbcLinkRepository(config);
        chatRepository.clear();
        linkRepository.clear();
    }

    @BeforeAll
    static void beforeAll() throws SQLException, LiquibaseException {
        postgresContainer.start();
        Connection connection = DriverManager.getConnection(
                postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword());
        LiquibaseMigration.migration(connection, "db/master.xml");
    }

    @AfterAll
    static void afterAll() {
        postgresContainer.stop();
    }

    @Test
    void testContainLink() {
        linkRepository.addLink("https://github.com/example");
        assertTrue(linkRepository.containLink("https://github.com/example"));
        assertFalse(linkRepository.containLink("https://nonexistent.url"));
    }

    @Test
    void testGetChats() {
        linkRepository.addLink("https://github.com/example");
        chatRepository.addChat(1L);
        chatRepository.subscribeLink(1L, new Link(1L, "https://github.com/example", null, null));
        Set<Long> chats = linkRepository.getChats("https://github.com/example");

        assertNotNull(chats);
        assertEquals(1, chats.size());
        assertTrue(chats.contains(1L));
    }

    @Test
    void testAddAndDeleteLink() {
        String newUrl = "https://new.url";
        assertFalse(linkRepository.containLink(newUrl));

        linkRepository.addLink(newUrl);
        assertTrue(linkRepository.containLink(newUrl));

        linkRepository.deleteLink(newUrl);
        assertFalse(linkRepository.containLink(newUrl));
    }

    @Test
    void testGetUrls() {
        linkRepository.addLink("https://github.com/example");
        linkRepository.addLink("https://github.com/example1");
        Set<String> urls = linkRepository.getUrls(10, 0);
        assertNotNull(urls);
        assertEquals(2, urls.size());
        assertTrue(urls.contains("https://github.com/example"));
        assertTrue(urls.contains("https://github.com/example1"));
    }

    @Test
    void testDeleteChat() {
        linkRepository.addLink("https://github.com/example");
        chatRepository.addChat(1L);
        chatRepository.subscribeLink(1L, new Link(1L, "https://github.com/example", null, null));

        linkRepository.deleteChat("https://github.com/example", 1L);

        Set<Long> chats = linkRepository.getChats("https://github.com/example");
        assertEquals(0, chats.size());
    }

    @Test
    void testClear() {
        linkRepository.addLink("https://github.com/example");
        linkRepository.clear();
        assertEquals(0, linkRepository.size());
    }

    @Test
    void testSize() {
        assertEquals(linkRepository.size(), 0);
        linkRepository.addLink("https://github.com/example");
        assertEquals(linkRepository.size(), 1);
    }

    @Test
    void testLastEventOperations() {
        String githubUrl = "https://github.com/example";
        String stackUrl = "https://stackoverflow.com/questions/123";

        PullRequest pullRequest = new PullRequest("1", "1", new User("login"));
        Payload payload = new Payload("action", pullRequest, null);
        Event event = new Event("push", "main", payload, "11");
        linkRepository.addLink(githubUrl);
        linkRepository.setLastEvent(githubUrl, event);
        assertTrue(linkRepository.isLastEvent(githubUrl, event));
        assertEquals(event, linkRepository.getLastGitHubEvent(githubUrl));

        Item item = new Item(12L, 123L, "title", new Owner("123"), "123");
        linkRepository.addLink(stackUrl);
        linkRepository.setLastEvent(stackUrl, item);
        assertTrue(linkRepository.isLastEvent(stackUrl, 123L));
        assertEquals(item, linkRepository.getLastStackOverflowEvent(stackUrl));
    }
}
