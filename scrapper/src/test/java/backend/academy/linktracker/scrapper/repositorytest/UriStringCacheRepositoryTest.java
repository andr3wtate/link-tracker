package backend.academy.linktracker.scrapper.repositorytest;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.repository.CacheRepository;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class UriStringCacheRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CacheRepository<URI, String> uriStringCacheRepository;

    @Test
    void testSetAndGet() {
        URI key = URI.create("https://example.com");
        String value = "test-value";
        uriStringCacheRepository.set(key, value);
        Optional<String> retrieved = uriStringCacheRepository.get(key);
        assertThat(retrieved).isPresent().contains(value);
    }

    @Test
    void testOverwrite() {
        URI key = URI.create("https://example.com");
        uriStringCacheRepository.set(key, "first");
        uriStringCacheRepository.set(key, "second");
        Optional<String> retrieved = uriStringCacheRepository.get(key);
        assertThat(retrieved).contains("second");
    }

    @Test
    void testGetNonExistent() {
        URI key = URI.create("https://notfound.com");
        Optional<String> retrieved = uriStringCacheRepository.get(key);
        assertThat(retrieved).isEmpty();
    }
}
