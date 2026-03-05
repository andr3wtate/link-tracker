package backend.academy.linktracker.bot.repository;

public interface BotRepository {
    void addChat(long chatId);

    boolean isPresent(long chatId);
}
