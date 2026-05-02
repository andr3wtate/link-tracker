package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.commondto.entity.ScrapperUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmScrapperUserRepository extends JpaRepository<ScrapperUser, Long> {
    boolean existsByChatId(Long chatId);

    Optional<ScrapperUser> findByChatId(Long chatId);
}
