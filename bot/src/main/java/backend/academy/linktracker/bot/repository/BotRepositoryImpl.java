package backend.academy.linktracker.bot.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class BotRepositoryImpl implements BotRepository {
    private final Map<Long, BotState> getStates;
    private final Map<Long, List<String>> getArgs;
    private final Map<Long, List<String>> getTags;

    BotRepositoryImpl() {
        getStates = new ConcurrentHashMap<>();
        getArgs = new ConcurrentHashMap<>();
        getTags = new ConcurrentHashMap<>();
    }

    @Override
    public void addChat(long chatId) {
        getStates.put(chatId, BotState.AWAITING_COMMAND);
    }

    @Override
    public boolean isPresent(long chatId) {
        return getStates.containsKey(chatId);
    }

    @Override
    public BotState getState(long chatId) {
        return getStates.get(chatId);
    }

    @Override
    public void setState(long chatId, BotState state) {
        getStates.put(chatId, state);
    }

    @Override
    public List<String> getArgs(long chatId) {
        return getArgs.get(chatId);
    }

    @Override
    public void setArgs(long chatId, List<String> args) {
        getArgs.put(chatId, args);
    }

    @Override
    public List<String> getTags(long chatId) {
        return getTags.get(chatId);
    }

    @Override
    public void setTags(long chatId, List<String> tags) {
        getTags.put(chatId, tags);
    }
}
