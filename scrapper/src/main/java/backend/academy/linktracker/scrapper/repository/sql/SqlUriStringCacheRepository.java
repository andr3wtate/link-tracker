package backend.academy.linktracker.scrapper.repository.sql;

import backend.academy.linktracker.scrapper.repository.CacheRepository;
import java.net.URI;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class SqlUriStringCacheRepository implements CacheRepository<URI, String> {
    private final JdbcTemplate jdbcTemplate;

    public SqlUriStringCacheRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<String> get(URI key) {
        String sql = "SELECT value FROM uri_string_cache WHERE uri = ?";
        try {
            String value = jdbcTemplate.queryForObject(sql, String.class, key.toString());
            return Optional.ofNullable(value);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void set(URI key, String value) {
        String sql = """
            INSERT INTO uri_string_cache (uri, value)
            VALUES (?, ?)
            ON CONFLICT (uri)
            DO UPDATE SET value = EXCLUDED.value, updated_at = CURRENT_TIMESTAMP
            """;
        jdbcTemplate.update(sql, key.toString(), value);
    }
}
