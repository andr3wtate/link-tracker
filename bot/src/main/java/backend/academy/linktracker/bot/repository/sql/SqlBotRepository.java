package backend.academy.linktracker.bot.repository.sql;

import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.bot.repository.BotState;
import java.sql.Array;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class SqlBotRepository implements BotRepository {

    private final JdbcTemplate jdbcTemplate;

    public SqlBotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addChat(long chatId) {
        String sql = "INSERT INTO bot_users (chat_id) VALUES (?) ON CONFLICT (chat_id) DO NOTHING";
        jdbcTemplate.update(sql, chatId);
    }

    @Override
    public boolean isPresent(long chatId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM bot_users WHERE chat_id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, chatId));
    }

    @Override
    public BotState getState(long chatId) {
        String sql = "SELECT chat_state FROM bot_users_sessions WHERE chat_id = ?";
        try {
            String state = jdbcTemplate.queryForObject(sql, String.class, chatId);
            return BotState.valueOf(state);
        } catch (EmptyResultDataAccessException e) {
            return BotState.AWAITING_COMMAND;
        }
    }

    @Override
    public void setState(long chatId, BotState state) {
        String sql = """
            INSERT INTO bot_users_sessions (chat_id, chat_state)
            VALUES (?, ?)
            ON CONFLICT (chat_id)
            DO UPDATE SET chat_state = EXCLUDED.chat_state, updated_at = CURRENT_TIMESTAMP
            """;
        jdbcTemplate.update(sql, chatId, state.name());
    }

    @Override
    public List<String> getArgs(long chatId) {
        String sql = "SELECT arguments FROM bot_users_sessions WHERE chat_id = ?";
        try {
            String[] args = jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> {
                        Array array = rs.getArray("arguments");
                        if (array == null) {
                            return new String[0];
                        }
                        return (String[]) array.getArray();
                    },
                    chatId);
            return args == null ? List.of() : Arrays.asList(args);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }

    @Override
    public void setArgs(long chatId, List<String> args) {
        String sql = """
            UPDATE bot_users_sessions SET arguments = ?, updated_at = CURRENT_TIMESTAMP WHERE chat_id = ?
            """;
        jdbcTemplate.update(sql, args.toArray(new String[0]), chatId);
    }

    @Override
    public List<String> getTags(long chatId) {
        String sql = "SELECT tags FROM bot_users_sessions WHERE chat_id = ?";
        try {
            String[] tags = jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> {
                        Array array = rs.getArray("tags");
                        if (array == null) {
                            return new String[0];
                        }
                        return (String[]) array.getArray();
                    },
                    chatId);
            return tags == null ? List.of() : Arrays.asList(tags);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }

    @Override
    public void setTags(long chatId, List<String> tags) {
        String sql = """
            UPDATE bot_users_sessions SET tags = ?, updated_at = CURRENT_TIMESTAMP WHERE chat_id = ?
            """;
        jdbcTemplate.update(sql, tags.toArray(new String[0]), chatId);
    }
}
