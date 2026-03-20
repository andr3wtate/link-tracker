package backend.academy.linktracker.scrapper.repository;

import java.util.Optional;

public interface CacheRepository<K, V> {
    Optional<V> get(K key);

    void set(K key, V value);
}
