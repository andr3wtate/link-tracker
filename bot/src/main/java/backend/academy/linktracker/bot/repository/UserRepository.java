package backend.academy.linktracker.bot.repository;

public interface UserRepository {
    void addUser(long userId);

    boolean isPresent(long userId);
}
