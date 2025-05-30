package backend.academy.scrapper;

import backend.academy.scrapper.configs.DbConfig;
import backend.academy.scrapper.db.LiquibaseMigration;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.chat.JdbcChatRepository;
import backend.academy.scrapper.repository.link.JdbcLinkRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.schemas.models.Link;
import backend.academy.scrapper.schemas.requests.AddLinkRequest;
import backend.academy.scrapper.schemas.requests.RemoveLinkRequest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import liquibase.exception.LiquibaseException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
@TestPropertySource(properties = "app.message-transport=kafka")
@SpringBootTest
public class LinksConsumerTests {
    private static KafkaContainer kafkaContainer =
        new KafkaContainer("apache/kafka-native:3.8.1").withExposedPorts(9092);

    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17-alpine")
        .withExposedPorts(5432)
        .withDatabaseName("local")
        .withUsername("postgres")
        .withPassword("test");

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private KafkaTemplate<Long, Object> kafkaTemplate;

    private static final String topic = "links";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("app.access-type", () -> "orm");

        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add(
            "spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.LongSerializer");
        registry.add(
            "spring.kafka.producer.value-serializer",
            () -> "org.springframework.kafka.support.serializer.JsonSerializer");
        registry.add("spring.kafka.consumer.group-id", () -> "scrapper-consumer-group");
        registry.add(
            "spring.kafka.consumer.key-deserializer",
            () -> "org.apache.kafka.common.serialization.LongDeserializer");
        registry.add(
            "spring.kafka.consumer.value-deserializer",
            () -> "org.springframework.kafka.support.serializer.JsonDeserializer");
    }

    @BeforeAll
    static void beforeAll() throws SQLException, LiquibaseException {
        postgresContainer.start();
        kafkaContainer.start();

        Connection connection = DriverManager.getConnection(
            postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword());
        LiquibaseMigration.migration(connection, "db/master.xml");

        String bootstrapServers = kafkaContainer.getBootstrapServers();

        try (AdminClient adminClient = AdminClient.create(Map.of("bootstrap.servers", bootstrapServers))) {
            adminClient
                .createTopics(List.of(new NewTopic(topic, 1, (short) 1)))
                .all()
                .get(10, TimeUnit.SECONDS);

            await().atMost(30, TimeUnit.SECONDS).until(() -> {
                try {
                    return adminClient.listTopics().names().get().contains(topic);
                } catch (Exception e) {
                    return false;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to create topic", e);
        }
    }

    @AfterAll
    static void afterAll() {
        postgresContainer.stop();
        kafkaContainer.stop();
    }

    @BeforeEach
    void setUp() {
        DbConfig config = new DbConfig(
            postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword());
        chatRepository = new JdbcChatRepository(config);
        linkRepository = new JdbcLinkRepository(config);
    }

    @Test
    void addChatListenValidTest() {
        Long chatId = 1L;
        String link = "http://examples.com";

        chatRepository.addChat(chatId);
        AddLinkRequest addLinkRequest = new AddLinkRequest(link, List.of("a"), List.of("a"));

        kafkaTemplate.send(topic, chatId, addLinkRequest);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Assertions.assertEquals(1, linkRepository.size());
            Assertions.assertTrue(linkRepository.containLink(link));
            Assertions.assertEquals(1, linkRepository.getLinks(chatId).size());
            Assertions.assertEquals(link, linkRepository.getLinks(chatId).get(0).url());
        });
    }

    @Test
    void removeChatListenValidTest() {
        Long chatId = 1L;
        String link = "http://examples.com";
        chatRepository.addChat(chatId);
        linkRepository.addLink(link);
        chatRepository.subscribeLink(chatId, new Link(linkRepository.getLinkId(link), link, null, null));
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(link);

        kafkaTemplate.send(topic, chatId, removeLinkRequest);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Assertions.assertEquals(0, linkRepository.size());
            Assertions.assertEquals(0, linkRepository.getLinks(chatId).size());
        });
    }
}
