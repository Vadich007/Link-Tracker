package backend.academy.bot;

import backend.academy.bot.repository.JdbcUserRepository;
import backend.academy.bot.repository.OrmUserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        private OrmUserRepository ormUserRepository;

        @Autowired(required = false)
        private JdbcUserRepository jdbcUserRepository;

        @Test
        void ormTest() {
            assertNotNull(ormUserRepository);
            assertNull(jdbcUserRepository);
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
        private OrmUserRepository ormUserRepository;

        @Autowired(required = false)
        private JdbcUserRepository jdbcUserRepository;

        @Test
        void jdbcTest() {
            assertNull(ormUserRepository);
            assertNotNull(jdbcUserRepository);
        }
    }
}
