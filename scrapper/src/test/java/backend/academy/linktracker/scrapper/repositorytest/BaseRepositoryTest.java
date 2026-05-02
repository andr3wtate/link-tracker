package backend.academy.linktracker.scrapper.repositorytest;

import backend.academy.linktracker.scrapper.service.GitHubRepoMonitorService;
import backend.academy.linktracker.scrapper.service.StackOverflowMonitorService;
import java.nio.file.Paths;
import java.sql.Connection;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseRepositoryTest {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("botdb")
            .withUsername("botuser")
            .withPassword("botpass");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.stackoverflow.key", () -> "test-key");
    }

    @Autowired
    protected DataSource dataSource;

    @MockitoBean
    private GitHubRepoMonitorService gitHubRepoMonitorService;

    @MockitoBean
    private StackOverflowMonitorService stackOverflowMonitorService;

    @BeforeAll
    void applyMigrations() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            var database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            var migrationsPath = Paths.get("../migrations").toAbsolutePath().normalize();
            var resourceAccessor = new DirectoryResourceAccessor(migrationsPath);
            var liquibase = new Liquibase("00-initial-schema.sql", resourceAccessor, database);
            liquibase.update(new Contexts());
        }
    }
}
