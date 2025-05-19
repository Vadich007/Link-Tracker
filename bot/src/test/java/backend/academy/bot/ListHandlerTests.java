package backend.academy.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import backend.academy.bot.service.commandhandlers.CommandHandler;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@TestPropertySource(properties = "app.message-transport=http")
class ListHandlerTests {
    @Autowired
    CommandHandler listHandler;

    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withExposedPorts(5432)
            .withDatabaseName("local")
            .withUsername("postgres")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("app.access-type", () -> "orm");
    }

    @BeforeAll
    static void beforeAll() {
        postgresContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgresContainer.stop();
    }

    @Test
    void handleTest1() {
        final long chatId = 1;
        WireMockServer wireMock = new WireMockServer(options().port(8081));
        wireMock.stubFor(get(urlEqualTo("/api/v1/links"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Tg-Chat-Id", String.valueOf(chatId))
                        .withStatus(200)
                        .withBody("{ \"links\": [ {" + "    \"id\": 1,"
                                + "    \"url\": \"https://github.com/Vadich007/test\","
                                + "    \"tags\": null,"
                                + "    \"filters\": null"
                                + "}],"
                                + "\"size\": 1"
                                + "}")));
        wireMock.start();
        String result = listHandler.handle(chatId, " ");
        Assertions.assertEquals(
                result,
                """
                1. https://github.com/Vadich007/test
                Теги: нет
                Фильтры: нет
                """);
        wireMock.stop();
    }

    @Test
    void handleTest2() {
        final long chatId = 1;
        WireMockServer wireMock = new WireMockServer(options().port(8081));
        wireMock.stubFor(get(urlEqualTo("/api/v1/links"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Tg-Chat-Id", String.valueOf(chatId))
                        .withStatus(200)
                        .withBody("{ \"links\": [ {" + "    \"id\": 1,"
                                + "    \"url\": \"https://github.com/Vadich007/test\","
                                + "    \"tags\": [\"a\"],"
                                + "    \"filters\": [\"a\"]"
                                + "}],"
                                + "\"size\": 1"
                                + "}")));
        wireMock.start();
        String result = listHandler.handle(chatId, " ");
        Assertions.assertEquals(
                result,
                """
                1. https://github.com/Vadich007/test
                Теги: a
                Фильтры: a
                """);
        wireMock.stop();
    }

    @Test
    void handleTest3() {
        final long chatId = 1;
        WireMockServer wireMock = new WireMockServer(options().port(8081));
        wireMock.stubFor(get(urlEqualTo("/api/v1/links"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Tg-Chat-Id", String.valueOf(chatId))
                        .withStatus(200)
                        .withBody("{ \"links\": [ {" + "    \"id\": 1,"
                                + "    \"url\": \"https://github.com/Vadich007/test\","
                                + "    \"tags\": null,"
                                + "    \"filters\": [\"a\"]"
                                + "}],"
                                + "\"size\": 1"
                                + "}")));
        wireMock.start();
        String result = listHandler.handle(chatId, " ");
        Assertions.assertEquals(
                result,
                """
                1. https://github.com/Vadich007/test
                Теги: нет
                Фильтры: a
                """);
        wireMock.stop();
    }

    @Test
    void handleTest4() {
        final long chatId = 1;
        WireMockServer wireMock = new WireMockServer(options().port(8081));
        wireMock.stubFor(get(urlEqualTo("/api/v1/links"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Tg-Chat-Id", String.valueOf(chatId))
                        .withStatus(200)
                        .withBody("{ \"links\": [ {" + "    \"id\": 1,"
                                + "    \"url\": \"https://github.com/Vadich007/test\","
                                + "    \"tags\": [\"a\"],"
                                + "    \"filters\": null"
                                + "}],"
                                + "\"size\": 1"
                                + "}")));
        wireMock.start();
        String result = listHandler.handle(chatId, " ");
        Assertions.assertEquals(
                result,
                """
                1. https://github.com/Vadich007/test
                Теги: a
                Фильтры: нет
                """);
        wireMock.stop();
    }
}
