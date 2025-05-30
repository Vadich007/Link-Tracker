package backend.academy.bot;

import backend.academy.bot.configs.ApiConfig;
import backend.academy.bot.configs.DbConfig;
import backend.academy.bot.db.LiquibaseMigration;
import backend.academy.bot.repository.JdbcUserRepository;
import backend.academy.bot.schemas.requests.AddLinkRequest;
import backend.academy.bot.schemas.requests.RemoveLinkRequest;
import backend.academy.bot.service.scrapper.ScrapperHttpClient;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@EnableRetry
public class RetryTests {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    private ScrapperHttpClient scrapperHttpClient = new ScrapperHttpClient(new RestTemplate(),
        new ApiConfig(
            new ApiConfig.ScrapperApi(
                "http://localhost:" + wireMock.getPort() + "/api/v1/links",
                "http://localhost:" + wireMock.getPort() + "/api/v1/tg-chat/{id}"
            )
        ),
        new RedisTemplate<>()
    );

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
    void postRegistrationChatTest() {
        String scenario = "postRegistrationChatTest";
        String firstState = "Fail";
        String secondState = "OK";
        String url = "/api/v1/tg-chat/1";

        wireMock.stubFor(post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse()
                .withStatus(503))
            .willSetStateTo(firstState));

        wireMock.stubFor(post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(firstState)
            .willReturn(aResponse()
                .withStatus(503))
            .willSetStateTo(secondState));

        wireMock.stubFor(post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(secondState)
            .willReturn(aResponse()
                .withStatus(200)));

        Assertions.assertDoesNotThrow(() -> scrapperHttpClient.registrationChat(1L));

        wireMock.verify(3, postRequestedFor(urlEqualTo(url)));

        Assertions.assertEquals(userRepository.size(), 1);
        Assertions.assertNotNull(userRepository.getUser(1L));
    }


    @Test
    void postRegistrationChatDontRetryTest() {
        String scenario = "postRegistrationChatTest";
        String url = "/api/v1/tg-chat/1";

        wireMock.stubFor(post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse()
                .withStatus(404)));

        Assertions.assertThrows(Exception.class, () -> scrapperHttpClient.registrationChat(1L));

        wireMock.verify(1, postRequestedFor(urlEqualTo(url)));

        Assertions.assertEquals(userRepository.size(), 0);
        Assertions.assertNull(userRepository.getUser(1L));
    }

    @Test
    void postTrackLinkTest() {
        String scenario = "postRegistrationChatTest";
        String firstState = "Fail";
        String secondState = "OK";
        String url = "/api/v1/links";

        wireMock.stubFor(post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse()
                .withStatus(503))
            .willSetStateTo(firstState));

        wireMock.stubFor(post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(firstState)
            .willReturn(aResponse()
                .withStatus(503))
            .willSetStateTo(secondState));

        wireMock.stubFor(post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(secondState)
            .willReturn(aResponse()
                .withStatus(200)));

        Assertions.assertDoesNotThrow(() -> scrapperHttpClient.trackLink(1L, new AddLinkRequest("http", null, null)));

        wireMock.verify(3, postRequestedFor(urlEqualTo(url)));
    }

    @Test
    void deleteUntrackLinkTest() {
        String scenario = "deleteUntrackLinkTest";
        String firstState = "Fail";
        String secondState = "OK";
        String url = "/api/v1/links";

        wireMock.stubFor(delete(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse()
                .withStatus(503))
            .willSetStateTo(firstState));

        wireMock.stubFor(delete(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(firstState)
            .willReturn(aResponse()
                .withStatus(503))
            .willSetStateTo(secondState));

        wireMock.stubFor(delete(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(secondState)
            .willReturn(aResponse()
                .withStatus(200)));

        Assertions.assertDoesNotThrow(() -> scrapperHttpClient.untrackLink(1L, new RemoveLinkRequest("http")));

        wireMock.verify(3, deleteRequestedFor(urlEqualTo(url)));
    }

    @Test
    void getListLinkTest() {
        String scenario = "getListLinkTest";
        String firstState = "Fail";
        String secondState = "OK";
        String url = "/api/v1/tg-chat/1";

        wireMock.stubFor(get(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse()
                .withStatus(503))
            .willSetStateTo(firstState));

        wireMock.stubFor(get(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(firstState)
            .willReturn(aResponse()
                .withStatus(503))
            .willSetStateTo(secondState));

        wireMock.stubFor(get(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(secondState)
            .willReturn(aResponse()
                .withStatus(200)));

        Assertions.assertDoesNotThrow(() -> scrapperHttpClient.listLink(1L));

        wireMock.verify(3, getRequestedFor(urlEqualTo(url)));
    }
}
