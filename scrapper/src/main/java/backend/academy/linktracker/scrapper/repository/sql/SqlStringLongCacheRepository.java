package backend.academy.linktracker.scrapper.repository.sql;

import backend.academy.linktracker.scrapper.repository.CacheRepository;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class SqlStringLongCacheRepository implements CacheRepository<String, Long> {
    private final JdbcTemplate jdbcTemplate;

    public SqlStringLongCacheRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Long> get(String key) {
        String sql = "SELECT value FROM string_long_cache WHERE key = ?";
        try {
            Long value = jdbcTemplate.queryForObject(sql, Long.class, key);
            return Optional.ofNullable(value);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void set(String key, Long value) {
        String sql = """
            INSERT INTO string_long_cache (key, value)
            VALUES (?, ?)
            ON CONFLICT (key)
            DO UPDATE SET value = EXCLUDED.value, updated_at = CURRENT_TIMESTAMP
            """;
        jdbcTemplate.update(sql, key, value);
    }
}
