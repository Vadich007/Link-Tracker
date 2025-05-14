package backend.academy.scrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.configs.DbConfig;
import backend.academy.scrapper.db.LiquibaseMigration;
import backend.academy.scrapper.repository.chat.JdbcChatRepository;
import backend.academy.scrapper.repository.link.JdbcLinkRepository;
import backend.academy.scrapper.schemas.models.Link;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
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
public class JdbcChatRepositoryTests {

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
    void containChatTest() {
        assertFalse(chatRepository.containChat(1L));
        chatRepository.addChat(1L);
        assertTrue(chatRepository.containChat(1L));
    }

    @Test
    void deleteChatTest() {
        chatRepository.addChat(1L);
        assertTrue(chatRepository.containChat(1L));
        chatRepository.deleteChat(1L);
        assertFalse(chatRepository.containChat(1L));
    }

    @Test
    void sizeTest() {
        chatRepository.addChat(1L);
        assertEquals(1, chatRepository.size());
    }

    @Test
    void testGetLinks() {
        chatRepository.addChat(1L);
        linkRepository.addLink("https://github.com/");

        chatRepository.subscribeLink(
                1L,
                new Link(
                        linkRepository.getLinkId("https://github.com/"),
                        "https://github.com",
                        List.of("tag1"),
                        List.of("filter1")));

        List<Link> links = chatRepository.getLinks(1L);

        assertNotNull(links);
        assertEquals(1, links.size());

        Link firstLink = links.get(0);
        assertEquals(1L, firstLink.id());
        assertEquals("https://github.com/", firstLink.url());
        assertEquals(List.of("tag1"), firstLink.tags());
        assertEquals(List.of("filter1"), firstLink.filters());
    }

    @Test
    void testSubscribeLink() throws Exception {
        chatRepository.addChat(1L);
        linkRepository.addLink("https://github.com/");

        Link link = new Link(
                linkRepository.getLinkId("https://github.com/"),
                "https://github.com/",
                List.of("java"),
                List.of("open-source"));
        chatRepository.subscribeLink(1L, link);

        List<Link> links = chatRepository.getLinks(1L);

        assertEquals(1, links.size());
        assertEquals(link.id(), links.get(0).id());
        assertEquals(link.url(), links.get(0).url());
    }

    @Test
    void testClear() {
        chatRepository.addChat(1L);
        chatRepository.addChat(2L);

        chatRepository.clear();

        assertEquals(0, chatRepository.size());
    }
}
