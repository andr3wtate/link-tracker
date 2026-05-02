package backend.academy.linktracker.bot.repositorytest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

import backend.academy.linktracker.bot.repository.BotRepository;
import java.nio.file.Paths;
import java.sql.Connection;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BotRepositoryTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("botdb")
            .withUsername("botuser")
            .withPassword("botpass");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private BotRepository botRepository;

    @BeforeAll
    void applyMigrations() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
            var migrationsPath = Paths.get("../migrations").toAbsolutePath().normalize();
            var resourceAccessor = new DirectoryResourceAccessor(migrationsPath);
            var liquibase = new Liquibase("00-initial-schema.sql", resourceAccessor, database);
            liquibase.update(new Contexts());
        }
    }

    @Test
    void addChat_shouldCreateNewUser() {
        long chatId = 1001L;
        assertThat(botRepository.isPresent(chatId)).isFalse();
        botRepository.addChat(chatId);
        assertThat(botRepository.isPresent(chatId)).isTrue();
    }

    @Test
    void isPresent_shouldReturnFalseForNonExistent() {
        long chatId = 1002L;
        assertThat(botRepository.isPresent(chatId)).isFalse();
    }

    @Test
    void addChat_duplicate_shouldNotThrowAndStayPresent() {
        long chatId = 1003L;
        botRepository.addChat(chatId);
        assertThatNoException().isThrownBy(() -> botRepository.addChat(chatId));
        assertThat(botRepository.isPresent(chatId)).isTrue();
    }
}
