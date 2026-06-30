package backend.academy.linktracker.scrapper.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "in-memory")
public class InMemoryCacheRepository<K, V> implements CacheRepository<K, V> {
    private final Map<K, V> map = new ConcurrentHashMap<>();

    @Override
    public Optional<V> get(K key) {
        return Optional.ofNullable(map.get(key));
    }

    @Override
    public void set(K key, V value) {
        map.put(key, value);
    }
}
