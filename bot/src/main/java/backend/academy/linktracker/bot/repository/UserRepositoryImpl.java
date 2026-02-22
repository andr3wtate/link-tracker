package backend.academy.linktracker.bot.repository;

import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Set<Long> users;
    // потом будем хранить для юзера его подписки

    UserRepositoryImpl() {
        users = new HashSet<>();
    }

    @Override
    public void addUser(long userId) {
        users.add(userId);
    }

    @Override
    public boolean isPresent(long userId) {
        return users.contains(userId);
    }
}
