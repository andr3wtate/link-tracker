package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.commondto.entity.LinkEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmLinkRepository extends JpaRepository<LinkEntity, Long> {
    @Modifying
    @Query("DELETE FROM LinkEntity l WHERE l.id NOT IN (SELECT DISTINCT s.link.id FROM Subscription s)")
    void deleteOrphanLinks();

    Optional<LinkEntity> findByUrl(String url);

    @Query("SELECT l FROM LinkEntity l WHERE l.id > :lastId ORDER BY l.id")
    List<LinkEntity> findLinksBatch(@Param("lastId") long lastId, Pageable pageable);

    @Modifying
    @Query("UPDATE LinkEntity l SET l.lastCheck = :time WHERE l.id = :id")
    void updateLastCheckTime(@Param("id") long id, @Param("time") Instant time);
}
