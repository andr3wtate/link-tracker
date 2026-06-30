package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.commondto.entity.UriStringCache;
import backend.academy.linktracker.scrapper.repository.CacheRepository;
import java.net.URI;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
@RequiredArgsConstructor
public class OrmUriStringCacheRepository implements CacheRepository<URI, String> {
    private final OrmUriStringCache repo;

    @Override
    @Transactional(readOnly = true)
    public Optional<String> get(URI key) {
        return repo.findById(key.toString()).map(UriStringCache::getValue);
    }

    @Override
    @Transactional
    public void set(URI key, String value) {
        UriStringCache row = repo.findById(key.toString()).orElseGet(() -> new UriStringCache(key.toString(), value));
        row.setValue(value);
        repo.save(row);
    }
}
