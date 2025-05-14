package backend.academy.scrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import backend.academy.scrapper.repository.chat.JdbcChatRepository;
import backend.academy.scrapper.repository.chat.OrmChatRepository;
import backend.academy.scrapper.repository.link.JdbcLinkRepository;
import backend.academy.scrapper.repository.link.OrmLinkRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public class DbChooseTests {

    @Nested
    @SpringBootTest
    class OrmTests {

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

        @Autowired(required = false)
        private OrmChatRepository ormChatRepository;

        @Autowired(required = false)
        private OrmLinkRepository ormLinkRepository;

        @Autowired(required = false)
        private JdbcChatRepository jdbcChatRepository;

        @Autowired(required = false)
        private JdbcLinkRepository jdbcLinkRepository;

        @Test
        void ormTest() {
            assertNotNull(ormChatRepository);
            assertNotNull(ormLinkRepository);
            assertNull(jdbcChatRepository);
            assertNull(jdbcLinkRepository);
        }
    }

    @Nested
    @SpringBootTest
    class JdbcTests {

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
            registry.add("app.access-type", () -> "jdbc");
        }

        @BeforeAll
        static void beforeAll() {
            postgresContainer.start();
        }

        @AfterAll
        static void afterAll() {
            postgresContainer.stop();
        }

        @Autowired(required = false)
        private OrmChatRepository ormChatRepository;

        @Autowired(required = false)
        private OrmLinkRepository ormLinkRepository;

        @Autowired(required = false)
        private JdbcChatRepository jdbcChatRepository;

        @Autowired(required = false)
        private JdbcLinkRepository jdbcLinkRepository;

        @Test
        void jdbcTest() {
            assertNull(ormChatRepository);
            assertNull(ormLinkRepository);
            assertNotNull(jdbcChatRepository);
            assertNotNull(jdbcLinkRepository);
        }
    }
}
