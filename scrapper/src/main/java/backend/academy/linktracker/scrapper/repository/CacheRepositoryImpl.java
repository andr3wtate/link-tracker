package backend.academy.linktracker.scrapper.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class CacheRepositoryImpl<K, V> implements CacheRepository<K, V> {
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
