package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.commondto.entity.UriStringCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmUriStringCache extends JpaRepository<UriStringCache, String> {}
