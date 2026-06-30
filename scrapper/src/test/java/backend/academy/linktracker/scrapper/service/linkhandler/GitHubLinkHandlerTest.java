package backend.academy.linktracker.scrapper.service.linkhandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.commondto.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.dto.GitHubIssue;
import backend.academy.linktracker.scrapper.dto.LinkForMonitorService;
import backend.academy.linktracker.scrapper.messaging.MessageSender;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class GitHubLinkHandlerTest {
    @Mock
    private ScrapperRepository scrapperRepository;

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private GitHubLinkHandler gitHubLinkHandler;

    @Test
    void processLinkWhenNewIssueExistsSendsUpdateWithPreview() {
        LinkForMonitorService link = new LinkForMonitorService(
                1L, URI.create("https://github.com/octocat/hello-world"), Instant.parse("2026-05-15T10:00:00Z"));
        String longBody = "a".repeat(250);
        GitHubIssue.Issue issue = new GitHubIssue.Issue(
                "https://github.com/octocat/hello-world/issues/1",
                "Bug title",
                new GitHubIssue.Issue.User("octocat"),
                OffsetDateTime.parse("2026-05-15T10:05:00Z"),
                longBody,
                null);

        when(gitHubClient.getChanges(contains("is:issue repo:octocat/hello-world")))
                .thenReturn(ResponseEntity.ok(new GitHubIssue(1, List.of(issue))));
        when(gitHubClient.getChanges(contains("is:pull-request repo:octocat/hello-world")))
                .thenReturn(ResponseEntity.ok(new GitHubIssue(0, List.of())));
        when(scrapperRepository.getTrackingTgChatIds(link.url())).thenReturn(List.of(10L, 20L));

        gitHubLinkHandler.processLink(link);

        ArgumentCaptor<LinkUpdate> updateCaptor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(messageSender).sendUpdate(updateCaptor.capture());
        assertThat(updateCaptor.getValue().toString())
                .contains("Bug title", "octocat", "Issue")
                .doesNotContain("a".repeat(201));
    }

    @Test
    void processLinkWhenInvalidGitHubLinkDeletesLinkAndSendsUpdate() {
        LinkForMonitorService link = new LinkForMonitorService(
                2L, URI.create("https://github.com/octocat"), Instant.parse("2026-05-15T10:00:00Z"));
        when(scrapperRepository.getTrackingTgChatIds(link.url())).thenReturn(List.of(10L));

        gitHubLinkHandler.processLink(link);

        verify(scrapperRepository).deleteLink(2L);
        verify(messageSender).sendUpdate(any(LinkUpdate.class));
        verifyNoInteractions(gitHubClient);
    }

    @Test
    void processLinkWhenGitHubReturnsErrorDoesNotSendUpdate() {
        LinkForMonitorService link = new LinkForMonitorService(
                3L, URI.create("https://github.com/octocat/hello-world"), Instant.parse("2026-05-15T10:00:00Z"));

        when(gitHubClient.getChanges(contains("is:issue repo:octocat/hello-world")))
                .thenReturn(
                        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new GitHubIssue(0, List.of())));
        when(gitHubClient.getChanges(contains("is:pull-request repo:octocat/hello-world")))
                .thenReturn(ResponseEntity.ok(new GitHubIssue(0, List.of())));

        gitHubLinkHandler.processLink(link);

        verify(messageSender, never()).sendUpdate(any(LinkUpdate.class));
    }
}
