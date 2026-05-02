package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.commondto.entity.LinkEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmLinkRepository extends JpaRepository<LinkEntity, Long> {
    @Modifying
    @Query("DELETE FROM LinkEntity l WHERE l.id NOT IN (SELECT DISTINCT s.link.id FROM Subscription s)")
    void deleteOrphanLinks();

    Optional<LinkEntity> findByUrl(String url);
}
