package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.commondto.entity.StringLongCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmStringLongCache extends JpaRepository<StringLongCache, String> {}
