package backend.academy.linktracker.scrapper.repositorytest;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.repository.CacheRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class StringLongCacheRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CacheRepository<String, Long> stringLongCacheRepository;

    @Test
    void testSetAndGet() {
        String key = "some-key";
        Long value = 12345L;
        stringLongCacheRepository.set(key, value);
        Optional<Long> retrieved = stringLongCacheRepository.get(key);
        assertThat(retrieved).isPresent().contains(value);
    }

    @Test
    void testOverwrite() {
        String key = "overwrite-key";
        stringLongCacheRepository.set(key, 100L);
        stringLongCacheRepository.set(key, 200L);
        Optional<Long> retrieved = stringLongCacheRepository.get(key);
        assertThat(retrieved).contains(200L);
    }

    @Test
    void testGetNonExistent() {
        String key = "missing-key";
        Optional<Long> retrieved = stringLongCacheRepository.get(key);
        assertThat(retrieved).isEmpty();
    }
}
