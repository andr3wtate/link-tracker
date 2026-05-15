package backend.academy.linktracker.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.commondto.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.LinkForMonitorService;
import backend.academy.linktracker.scrapper.messaging.MessageSender;
import backend.academy.linktracker.scrapper.properties.MonitorServiceProperties;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import backend.academy.linktracker.scrapper.service.linkhandler.GitHubLinkHandler;
import backend.academy.linktracker.scrapper.service.linkhandler.StackOverflowLinkHandler;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonitorServiceTest {
    @Mock
    private MonitorServiceProperties properties;

    @Mock
    private ScrapperRepository scrapperRepository;

    @Mock
    private MessageSender messageSender;

    @Mock
    private GitHubLinkHandler gitHubLinkHandler;

    @Mock
    private StackOverflowLinkHandler stackOverflowLinkHandler;

    private MonitorService monitorService;

    @BeforeEach
    void setUp() {
        when(properties.getThreads()).thenReturn(2);
        when(properties.getBatchSize()).thenReturn(10);
        monitorService = new MonitorService(
                properties, scrapperRepository, messageSender, gitHubLinkHandler, stackOverflowLinkHandler);
        monitorService.init();
    }

    @AfterEach
    void tearDown() {
        monitorService.shutdown();
    }

    @Test
    void checkChangesProcessesGithubAndStackOverflowLinks() {
        LinkForMonitorService githubLink = new LinkForMonitorService(
                1L, URI.create("https://github.com/octocat/hello-world"), Instant.parse("2026-05-15T10:00:00Z"));
        LinkForMonitorService stackOverflowLink = new LinkForMonitorService(
                2L,
                URI.create("https://stackoverflow.com/questions/123/test-question"),
                Instant.parse("2026-05-15T10:00:00Z"));

        when(scrapperRepository.getLinksBatch(0, 10)).thenReturn(List.of(githubLink, stackOverflowLink));
        when(scrapperRepository.getLinksBatch(2, 10)).thenReturn(List.of());

        monitorService.checkChanges();

        verify(gitHubLinkHandler).processLink(githubLink);
        verify(stackOverflowLinkHandler).processLink(stackOverflowLink);
        verify(scrapperRepository).updateLastCheckTime(eq(1L), any(Instant.class));
        verify(scrapperRepository).updateLastCheckTime(eq(2L), any(Instant.class));
        verifyNoInteractions(messageSender);
    }

    @Test
    void checkChangesIsolatesErrorsAndContinuesBatchProcessing() {
        LinkForMonitorService brokenGithubLink = new LinkForMonitorService(
                1L, URI.create("https://github.com/octocat/hello-world"), Instant.parse("2026-05-15T10:00:00Z"));
        LinkForMonitorService stackOverflowLink = new LinkForMonitorService(
                2L,
                URI.create("https://stackoverflow.com/questions/123/test-question"),
                Instant.parse("2026-05-15T10:00:00Z"));

        when(scrapperRepository.getLinksBatch(0, 10)).thenReturn(List.of(brokenGithubLink, stackOverflowLink));
        when(scrapperRepository.getLinksBatch(2, 10)).thenReturn(List.of());
        when(scrapperRepository.getTrackingTgChatIds(brokenGithubLink.url())).thenReturn(List.of(10L));
        doThrow(new RuntimeException("GitHub is unavailable"))
                .when(gitHubLinkHandler)
                .processLink(brokenGithubLink);

        monitorService.checkChanges();

        verify(gitHubLinkHandler).processLink(brokenGithubLink);
        verify(stackOverflowLinkHandler).processLink(stackOverflowLink);
        verify(messageSender).sendUpdate(any(LinkUpdate.class));
        verify(scrapperRepository, never()).updateLastCheckTime(eq(1L), any(Instant.class));
        verify(scrapperRepository).updateLastCheckTime(eq(2L), any(Instant.class));
    }
}
