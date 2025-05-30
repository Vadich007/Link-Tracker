package backend.academy.scrapper;

import backend.academy.scrapper.db.LiquibaseMigration;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.schemas.models.Link;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
public class OrmChatRepositoryTests {

    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17-alpine")
        .withExposedPorts(5432)
        .withDatabaseName("local")
        .withUsername("postgres")
        .withPassword("test");

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private LinkRepository linkRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("app.access-type", () -> "orm");
    }

    @BeforeEach
    void setUp() throws SQLException, LiquibaseException {
        postgresContainer.start();
        Connection connection = DriverManager.getConnection(
            postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword());
        LiquibaseMigration.migration(connection, "db/master.xml");
    }

    @AfterEach
    void afterEach() {
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
    void getLinksTest() {
        chatRepository.addChat(1L);
        linkRepository.addLink("https://github.com/");

        chatRepository.subscribeLink(
            1L,
            new Link(
                linkRepository.getLinkId("https://github.com/"),
                "https://github.com",
                List.of("tag1"),
                List.of("filter1")));

        List<Link> links = linkRepository.getLinks(1L);

        assertNotNull(links);
        assertEquals(1, links.size());

        Link firstLink = links.get(0);
        assertEquals(1L, firstLink.id());
        assertEquals("https://github.com/", firstLink.url());
        assertEquals(List.of("tag1"), firstLink.tags());
        assertEquals(List.of("filter1"), firstLink.filters());
    }

    @Test
    void subscribeLinkTest() throws Exception {
        chatRepository.addChat(1L);
        linkRepository.addLink("https://github.com/");

        Link link = new Link(
            linkRepository.getLinkId("https://github.com/"),
            "https://github.com/",
            List.of("java"),
            List.of("open-source"));
        chatRepository.subscribeLink(1L, link);

        List<Link> links = linkRepository.getLinks(1L);

        assertEquals(1, links.size());
        assertEquals(link.id(), links.get(0).id());
        assertEquals(link.url(), links.get(0).url());
    }
}
