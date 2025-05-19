package backend.academy.scrapper;

import backend.academy.scrapper.db.LiquibaseMigration;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.schemas.models.Link;
import backend.academy.scrapper.schemas.requests.AddLinkRequest;
import backend.academy.scrapper.schemas.requests.RemoveLinkRequest;
import backend.academy.scrapper.schemas.responses.bot.ApiErrorResponse;
import backend.academy.scrapper.schemas.responses.bot.LinkResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {"server.port=8086"})
@TestPropertySource(properties = "app.message-transport=http")
public class LinkControllerTests {

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

    String scrapperLinksUrl = "http://localhost:8086/api/v1/links";
    RestTemplate restTemplate = new RestTemplate();

    @Test
    void duplicateLinkTest() {
        chatRepository.addChat(1L);
        linkRepository.addLink("https://github.com/Vadich007/test");
        long id = linkRepository.getLinkId("https://github.com/Vadich007/test");
        Link link = new Link(id, "https://github.com/Vadich007/test", null, null);
        chatRepository.subscribeLink(1L, link);

        AddLinkRequest request = new AddLinkRequest("https://github.com/Vadich007/test", null, null);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(1));
        HttpEntity<AddLinkRequest> requestEntity = new HttpEntity<>(request, headers);

        HttpClientErrorException exception = Assertions.assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(scrapperLinksUrl, HttpMethod.POST, requestEntity, ApiErrorResponse.class);
        });

        ApiErrorResponse apiErrorResponse = exception.getResponseBodyAs(ApiErrorResponse.class);

        Assertions.assertEquals("Ссылка уже отслеживается", apiErrorResponse.description());
        Assertions.assertEquals("400 BAD_REQUEST", apiErrorResponse.code());
    }

    @Test
    void addLinkTest() {
        chatRepository.addChat(1L);
        List<String> tags = new ArrayList<>();
        tags.add("a");
        List<String> filters = new ArrayList<>();
        filters.add("a");
        Link link = new Link(0L, "https://github.com/Vadich007/test", tags, filters);

        LinkResponse expectedResponse = new LinkResponse(link);
        AddLinkRequest request = new AddLinkRequest("https://github.com/Vadich007/test", tags, filters);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(1));
        HttpEntity<AddLinkRequest> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<LinkResponse> response =
                restTemplate.exchange(scrapperLinksUrl, HttpMethod.POST, requestEntity, LinkResponse.class);

        Assertions.assertEquals(expectedResponse.url(), response.getBody().url());
        Assertions.assertEquals(expectedResponse.tags(), response.getBody().tags());
        Assertions.assertEquals(expectedResponse.filters(), response.getBody().filters());

        var links = chatRepository.getLinks(1L);

        Assertions.assertEquals(links.size(), 1);
        Assertions.assertEquals(links.getFirst().url(), response.getBody().url());
        Assertions.assertEquals(links.getFirst().tags(), response.getBody().tags());
        Assertions.assertEquals(links.getFirst().filters(), response.getBody().filters());
        Assertions.assertTrue(linkRepository.containLink("https://github.com/Vadich007/test"));
        Assertions.assertTrue(
                linkRepository.getChats("https://github.com/Vadich007/test").contains(1L));
    }

    @Test
    void deleteLinkTest() {
        chatRepository.addChat(1L);
        List<String> tags = new ArrayList<>();
        tags.add("a");
        List<String> filters = new ArrayList<>();
        filters.add("a");
        linkRepository.addLink("https://github.com/Vadich007/test");
        long id = linkRepository.getLinkId("https://github.com/Vadich007/test");
        Link link = new Link(id, "https://github.com/Vadich007/test", tags, filters);
        chatRepository.subscribeLink(1L, link);

        LinkResponse expectedResponse = new LinkResponse(link);
        RemoveLinkRequest request = new RemoveLinkRequest("https://github.com/Vadich007/test");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(1));
        HttpEntity<RemoveLinkRequest> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<LinkResponse> response =
                restTemplate.exchange(scrapperLinksUrl, HttpMethod.DELETE, requestEntity, LinkResponse.class);

        Assertions.assertEquals(expectedResponse.id(), response.getBody().id());
        Assertions.assertEquals(expectedResponse.url(), response.getBody().url());
        Assertions.assertEquals(expectedResponse.tags(), response.getBody().tags());
        Assertions.assertEquals(expectedResponse.filters(), response.getBody().filters());
        Assertions.assertEquals(chatRepository.getLinks(1L).size(), 0);
        Assertions.assertEquals(linkRepository.size(), 0);
    }

    @Test
    void dontContatinChatRemoveLinkTest() {
        RemoveLinkRequest request = new RemoveLinkRequest("https://github.com/Vadich007/test");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(1));
        HttpEntity<RemoveLinkRequest> requestEntity = new HttpEntity<>(request, headers);

        HttpClientErrorException exception = Assertions.assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(scrapperLinksUrl, HttpMethod.DELETE, requestEntity, ApiErrorResponse.class);
        });

        ApiErrorResponse apiErrorResponse = exception.getResponseBodyAs(ApiErrorResponse.class);

        Assertions.assertEquals(apiErrorResponse.description(), "Отсутствует чат с таким id");
        Assertions.assertEquals("400 BAD_REQUEST", apiErrorResponse.code());
        Assertions.assertEquals(chatRepository.size(), 0);
        Assertions.assertEquals(linkRepository.size(), 0);
    }

    @Test
    void dontContatinChatAddLinkTest() {
        List<String> tags = new ArrayList<>();
        tags.add("a");
        List<String> filters = new ArrayList<>();
        filters.add("a");

        AddLinkRequest request = new AddLinkRequest("https://github.com/Vadich007/test", tags, filters);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(1));
        HttpEntity<AddLinkRequest> requestEntity = new HttpEntity<>(request, headers);

        HttpClientErrorException exception = Assertions.assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(scrapperLinksUrl, HttpMethod.POST, requestEntity, ApiErrorResponse.class);
        });

        ApiErrorResponse apiErrorResponse = exception.getResponseBodyAs(ApiErrorResponse.class);

        Assertions.assertEquals(apiErrorResponse.description(), "Отсутствует чат с таким id");
        Assertions.assertEquals("400 BAD_REQUEST", apiErrorResponse.code());
        Assertions.assertEquals(chatRepository.size(), 0);
        Assertions.assertEquals(linkRepository.size(), 0);
    }

    @Test
    void dontContainChatGetLinkTest() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(1));
        HttpEntity<AddLinkRequest> requestEntity = new HttpEntity<>(headers);

        HttpClientErrorException exception = Assertions.assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(scrapperLinksUrl, HttpMethod.GET, requestEntity, ApiErrorResponse.class);
        });

        ApiErrorResponse apiErrorResponse = exception.getResponseBodyAs(ApiErrorResponse.class);

        Assertions.assertEquals(apiErrorResponse.description(), "Отсутствует чат с таким id");
        Assertions.assertEquals("400 BAD_REQUEST", apiErrorResponse.code());
        Assertions.assertEquals(chatRepository.size(), 0);
        Assertions.assertEquals(linkRepository.size(), 0);
    }

    @Test
    void dontContainLinkRemoveLinkTest() {
        chatRepository.addChat(1L);

        RemoveLinkRequest request = new RemoveLinkRequest("https://github.com/Vadich007/test");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(1));
        HttpEntity<RemoveLinkRequest> requestEntity = new HttpEntity<>(request, headers);

        HttpClientErrorException exception = Assertions.assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(scrapperLinksUrl, HttpMethod.DELETE, requestEntity, ApiErrorResponse.class);
        });

        ApiErrorResponse apiErrorResponse = exception.getResponseBodyAs(ApiErrorResponse.class);

        Assertions.assertEquals(apiErrorResponse.description(), "Ссылка не найдена");
        Assertions.assertEquals("404 NOT_FOUND", apiErrorResponse.code());
    }
}
