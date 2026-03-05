package backend.academy.linktracker.bot.repository;

import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class BotRepositoryImpl implements BotRepository {
    private final Set<Long> chatIds;

    BotRepositoryImpl() {
        chatIds = new HashSet<>();
    }

    @Override
    public void addChat(long chatId) {
        chatIds.add(chatId);
    }

    @Override
    public boolean isPresent(long chatId) {
        return chatIds.contains(chatId);
    }
}
