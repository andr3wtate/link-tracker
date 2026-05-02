package backend.academy.linktracker.bot.repository.orm;

import backend.academy.linktracker.commondto.entity.BotUserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmBotUserSessionRepository extends JpaRepository<BotUserSession, Long> {}
