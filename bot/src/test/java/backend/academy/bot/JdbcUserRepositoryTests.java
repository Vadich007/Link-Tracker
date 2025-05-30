package backend.academy.bot;

import backend.academy.bot.configs.DbConfig;
import backend.academy.bot.db.LiquibaseMigration;
import backend.academy.bot.repository.JdbcUserRepository;
import backend.academy.bot.schemas.models.User;
import backend.academy.bot.schemas.models.UserStates;
import backend.academy.bot.schemas.requests.AddLinkRequest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@TestPropertySource(properties = "app.access-type=jdbc")
public class JdbcUserRepositoryTests {

    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17-alpine")
        .withExposedPorts(5432)
        .withDatabaseName("local")
        .withUsername("postgres")
        .withPassword("test");

    private JdbcUserRepository userRepository;

    @BeforeEach
    void setUp() throws SQLException, LiquibaseException {
        postgresContainer.start();
        Connection connection = DriverManager.getConnection(
            postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword());
        LiquibaseMigration.migration(connection, "db/master.xml");
        DbConfig config = new DbConfig(
            postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword());
        userRepository = new JdbcUserRepository(config);
    }

    @AfterEach
    void afterEach() {
        postgresContainer.stop();
    }

    @Test
    void shouldAddAndCheckUser() {
        long chatId = 12345L;
        assertFalse(userRepository.contain(chatId));
        userRepository.addUser(chatId);
        assertTrue(userRepository.contain(chatId));
    }

    @Test
    void shouldGetUserWithState() {
        long chatId = 54321L;
        userRepository.addUser(chatId);

        User user = userRepository.getUser(chatId);
        assertNotNull(user);
        assertEquals(chatId, user.chatId());
        assertEquals(UserStates.FREE, user.state());
        assertNull(user.addLinkRequest());
    }

    @Test
    void shouldUpdateUserState() {
        long chatId = 98765L;
        userRepository.addUser(chatId);

        userRepository.updateState(chatId, UserStates.WAITING_FOR_LINK);
        User user = userRepository.getUser(chatId);
        assertEquals(UserStates.WAITING_FOR_LINK, user.state());
    }

    @Test
    void shouldHandleAddLinkRequest() {
        long chatId = 11111L;
        userRepository.addUser(chatId);

        String testUrl = "https://example.com";
        userRepository.addUrl(chatId, testUrl);

        AddLinkRequest request = userRepository.getAddLinkRequest(chatId);
        assertNotNull(request);
        assertEquals(testUrl, request.link());
        assertNull(request.tags());
        assertNull(request.filters());

        List<String> tags = List.of("tag1", "tag2");
        userRepository.addTags(chatId, tags);
        request = userRepository.getAddLinkRequest(chatId);
        assertEquals(tags, request.tags());

        List<String> filters = List.of("filter1");
        userRepository.addFilters(chatId, filters);
        request = userRepository.getAddLinkRequest(chatId);
        assertEquals(filters, request.filters());

        userRepository.deleteAddLinkRequest(chatId);
        assertNull(userRepository.getAddLinkRequest(chatId));
    }
}
