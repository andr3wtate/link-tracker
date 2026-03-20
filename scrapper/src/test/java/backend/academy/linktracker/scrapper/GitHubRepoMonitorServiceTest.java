package backend.academy.linktracker.scrapper;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.commondto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.ScrapperClient;
import backend.academy.linktracker.scrapper.repository.CacheRepository;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import backend.academy.linktracker.scrapper.service.GitHubRepoMonitorService;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class GitHubRepoMonitorServiceTest {

    @Mock
    private ScrapperRepository scrapperRepository;

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private CacheRepository<URI, String> eTagRepository;

    @InjectMocks
    private GitHubRepoMonitorService service;

    @Test
    void whenGitHubClientThrowsNotFound_shouldNotCrash() {
        URI link = URI.create("https://github.com/user/repo");
        when(scrapperRepository.getAllLinks()).thenReturn(List.of(link));
        when(eTagRepository.get(link)).thenReturn(Optional.empty()); // первый раз, ETag нет
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
                .when(gitHubClient)
                .checkChanges("user", "repo", null);

        service.checkChanges();

        verify(scrapperClient, never()).sendUpdates(any(LinkUpdate.class));
        verify(eTagRepository, never()).set(eq(link), anyString());
    }

    @Test
    void whenLinkIsNotGitHub_shouldSkipProcessing() {
        URI link = URI.create("https://stackoverflow.com/questions/123");
        when(scrapperRepository.getAllLinks()).thenReturn(List.of(link));

        service.checkChanges();

        verifyNoInteractions(gitHubClient, scrapperClient, eTagRepository);
    }

    @Test
    void whenGitHubClientThrowsServerError_shouldNotCrash() {
        URI link = URI.create("https://github.com/user/repo");
        when(scrapperRepository.getAllLinks()).thenReturn(List.of(link));
        when(eTagRepository.get(link)).thenReturn(Optional.of("some-etag"));
        doThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                .when(gitHubClient)
                .checkChanges("user", "repo", "some-etag");

        service.checkChanges();

        verify(scrapperClient, never()).sendUpdates(any());
        verify(eTagRepository, never()).set(eq(link), anyString());
    }
}
