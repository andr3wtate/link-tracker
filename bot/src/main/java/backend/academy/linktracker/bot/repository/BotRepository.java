package backend.academy.linktracker.bot.repository;

import java.util.List;

public interface BotRepository {
    void addChat(long chatId);

    boolean isPresent(long chatId);

    BotState getState(long chatId);

    void setState(long chatId, BotState state);

    List<String> getArgs(long chatId);

    void setArgs(long chatId, List<String> args);

    List<String> getTags(long chatId);

    void setTags(long chatId, List<String> tags);
}
