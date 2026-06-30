package backend.academy.linktracker.bot.repository.orm;

import backend.academy.linktracker.commondto.entity.BotUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmBotUserRepository extends JpaRepository<BotUser, Long> {
    Optional<BotUser> findByChatId(Long chatId);

    boolean existsByChatId(Long chatId);
}
