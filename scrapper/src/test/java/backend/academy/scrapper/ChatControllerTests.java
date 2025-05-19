package backend.academy.scrapper;

import backend.academy.scrapper.db.LiquibaseMigration;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.schemas.responses.bot.ApiErrorResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {"server.port=8081"})
@TestPropertySource(properties = "app.message-transport=http")
public class ChatControllerTests {

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
    void setUp() {
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

    String scrapperTgUrl = "http://localhost:8081/api/v1/tg-chat/{id}";
    RestTemplate restTemplate = new RestTemplate();

    @Test
    void chatAlreadyContain() {
        chatRepository.addChat(1L);

        HttpClientErrorException exception = Assertions.assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(scrapperTgUrl.replace("{id}", "1"), HttpMethod.POST, null, ApiErrorResponse.class);
        });

        ApiErrorResponse apiErrorResponse = exception.getResponseBodyAs(ApiErrorResponse.class);

        Assertions.assertEquals("Чат с таким id уже зарегистрирован", apiErrorResponse.description());
        Assertions.assertEquals("400 BAD_REQUEST", apiErrorResponse.code());
    }

    @Test
    void chatDontExist() {
        HttpClientErrorException exception = Assertions.assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(scrapperTgUrl.replace("{id}", "1"), HttpMethod.DELETE, null, ApiErrorResponse.class);
        });

        ApiErrorResponse apiErrorResponse = exception.getResponseBodyAs(ApiErrorResponse.class);

        Assertions.assertEquals("Чат не существует", apiErrorResponse.description());
        Assertions.assertEquals("404 NOT_FOUND", apiErrorResponse.code());
    }
}
