package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.commondto.entity.StringLongCache;
import backend.academy.linktracker.scrapper.repository.CacheRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
@RequiredArgsConstructor
public class OrmStringLongCacheRepository implements CacheRepository<String, Long> {
    private final OrmStringLongCache repo;

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> get(String key) {
        return repo.findById(key).map(StringLongCache::getValue);
    }

    @Override
    @Transactional
    public void set(String key, Long value) {
        StringLongCache row = repo.findById(key).orElseGet(() -> new StringLongCache(key, value));
        row.setValue(value);
        repo.save(row);
    }
}
