package backend.academy.linktracker.scrapper.repository;

public interface CacheRepository<K, V> {
    V get(K key);

    void set(K key, V value);
}
