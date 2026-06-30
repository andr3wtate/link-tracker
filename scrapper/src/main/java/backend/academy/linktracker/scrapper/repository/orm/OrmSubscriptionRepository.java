package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.commondto.entity.Subscription;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmSubscriptionRepository extends JpaRepository<Subscription, Long> {
    boolean existsByScrapperUser_ChatIdAndLink_Url(long chatId, String url);

    @Query("SELECT s.link.id, s.link.url, s.tags FROM Subscription s WHERE s.scrapperUser.chatId = :chatId")
    List<Object[]> findLinksByChatId(@Param("chatId") Long chatId);

    @Query("SELECT s.scrapperUser.chatId FROM Subscription s WHERE s.link.url = :url")
    List<Long> findTrackingTgChatIds(@Param("url") String url);

    void deleteByScrapperUser_IdAndLinkId(Long userId, Long linkId);

    long countByLink_Id(Long linkId);
}
