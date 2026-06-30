package backend.academy.linktracker.scrapper.repository.sql;

import backend.academy.linktracker.commondto.dto.AddLink;
import backend.academy.linktracker.commondto.dto.Link;
import backend.academy.linktracker.commondto.dto.RemoveLink;
import backend.academy.linktracker.scrapper.dto.LinkForMonitorService;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import java.net.URI;
import java.sql.Array;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class SqlScrapperRepository implements ScrapperRepository {
    private final JdbcTemplate jdbcTemplate;

    public SqlScrapperRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isChatRegistered(long chatId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM scrapper_users WHERE chat_id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, chatId));
    }

    @Override
    public boolean chatContainsLink(long chatId, URI url) {
        String sql = """
            SELECT EXISTS(
                SELECT 1 FROM subscriptions s
                JOIN scrapper_users u ON u.id = s.user_id
                JOIN links l ON l.id = s.link_id
                WHERE u.chat_id = ? AND l.url = ?
            )
            """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, chatId, url.toString()));
    }

    @Override
    public void registerChat(long chatId) {
        String sql = "INSERT INTO scrapper_users (chat_id) VALUES (?) ON CONFLICT (chat_id) DO NOTHING";
        jdbcTemplate.update(sql, chatId);
    }

    @Override
    @Transactional
    public void deleteChat(long chatId) {
        Long userId =
                jdbcTemplate.queryForObject("SELECT id FROM scrapper_users WHERE chat_id = ?", Long.class, chatId);
        if (userId == null) {
            return;
        }

        jdbcTemplate.update("DELETE FROM scrapper_users WHERE id = ?", userId);

        String sql = """
            DELETE FROM links
            WHERE id NOT IN (SELECT DISTINCT link_id FROM subscriptions)
            """;
        jdbcTemplate.update(sql);
    }

    @Override
    public List<Link> getLinksByChatId(long chatId) {
        String sql = """
            SELECT l.id, l.url, s.tags FROM subscriptions s
            JOIN scrapper_users u ON u.id = s.user_id
            JOIN links l ON l.id = s.link_id
            WHERE u.chat_id = ?
            """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Array arr = rs.getArray("tags");
                    return new Link(
                            rs.getLong("id"),
                            URI.create(rs.getString("url")),
                            arr == null ? List.of() : Arrays.asList((String[]) arr.getArray()));
                },
                chatId);
    }

    @Override
    public List<URI> getAllLinks() {
        String sql = "SELECT url FROM links";
        return jdbcTemplate.query(sql, (rs, rowNum) -> URI.create(rs.getString("url")));
    }

    @Override
    public void deleteLink(long linkId) {
        String sql = """
            DELETE FROM links l
            WHERE l.id = ?
            """;
        jdbcTemplate.update(sql, linkId);
    }

    @Override
    public List<LinkForMonitorService> getLinksBatch(long lastLinkId, int batchSize) {
        String sql = """
            SELECT l.id, l.url, l.last_check FROM links l
            WHERE l.id > ?
            ORDER BY id
            LIMIT ?
            """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new LinkForMonitorService(
                        rs.getLong("id"),
                        URI.create(rs.getString("url")),
                        rs.getTimestamp("last_check").toInstant()),
                lastLinkId,
                batchSize);
    }

    @Override
    public void updateLastCheckTime(long linkId, Instant time) {
        String sql = """
            UPDATE links SET last_check = ?
            WHERE id = ?
            """;
        jdbcTemplate.update(sql, Timestamp.from(time), linkId);
    }

    @Override
    public List<Long> getTrackingTgChatIds(URI link) {
        String sql = """
            SELECT u.chat_id FROM subscriptions s
            JOIN scrapper_users u ON u.id = s.user_id
            JOIN links l ON l.id = s.link_id
            WHERE l.url = ?
            """;
        return jdbcTemplate.queryForList(sql, Long.class, link.toString());
    }

    @Override
    @Transactional
    public Link addLinkByChatId(long chatId, AddLink newLink) {
        Long userId =
                jdbcTemplate.queryForObject("SELECT id FROM scrapper_users WHERE chat_id = ?", Long.class, chatId);
        if (userId == null) { // unreachable (должно быть проверено сервисом)
            throw new IllegalArgumentException("User is not registered");
        }

        String insertLink = """
            INSERT INTO links (url) VALUES (?)
            ON CONFLICT (url) DO UPDATE SET url = EXCLUDED.url
            RETURNING id
            """;
        Long linkId = jdbcTemplate.queryForObject(
                insertLink, Long.class, newLink.link().toString());

        String insertSubscription = """
            INSERT INTO subscriptions (user_id, link_id, tags)
            VALUES (?, ?, ?)
            """;
        jdbcTemplate.update(insertSubscription, userId, linkId, newLink.tags().toArray(new String[0]));
        return new Link(linkId, newLink.link(), newLink.tags());
    }

    @Override
    @Transactional
    public Link deleteLinkByChatId(long chatId, RemoveLink link) {
        Long userId =
                jdbcTemplate.queryForObject("SELECT id FROM scrapper_users WHERE chat_id = ?", Long.class, chatId);
        if (userId == null) { // unreachable (должно быть проверено сервисом)
            throw new IllegalArgumentException("User is not registered");
        }

        Long linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM links WHERE url = ?", Long.class, link.link().toString());
        if (linkId == null) { // unreachable (должно быть проверено сервисом)
            throw new IllegalArgumentException("Link is not tracked by anyone");
        }

        String deleteSubscription = """
            DELETE FROM subscriptions
            WHERE user_id = ? AND link_id = ?
            """;
        jdbcTemplate.update(deleteSubscription, userId, linkId);

        Integer linksLast = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM subscriptions WHERE link_id = ? ", Integer.class, linkId);
        if (linksLast != null && linksLast == 0) {
            jdbcTemplate.update("DELETE FROM links WHERE id = ?", linkId);
        }

        return new Link(linkId, link.link(), List.of());
    }
}
