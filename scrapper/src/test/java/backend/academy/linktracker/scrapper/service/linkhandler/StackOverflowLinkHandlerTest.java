package backend.academy.linktracker.scrapper.service.linkhandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.commondto.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.LinkForMonitorService;
import backend.academy.linktracker.scrapper.dto.StackOverflowAnswer;
import backend.academy.linktracker.scrapper.dto.StackOverflowComment;
import backend.academy.linktracker.scrapper.messaging.MessageSender;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import java.net.URI;
import java.time.Instant;
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
class StackOverflowLinkHandlerTest {
    @Mock
    private ScrapperRepository scrapperRepository;

    @Mock
    private StackOverflowClient stackOverflowClient;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private StackOverflowLinkHandler stackOverflowLinkHandler;

    @Test
    void processLinkWhenNewAnswerExistsSendsUpdateWithPreview() {
        LinkForMonitorService link = new LinkForMonitorService(
                1L,
                URI.create("https://stackoverflow.com/questions/123/test-question"),
                Instant.parse("2026-05-15T10:00:00Z"));
        String longBody = "b".repeat(250);
        StackOverflowAnswer.Answer answer = new StackOverflowAnswer.Answer(
                new StackOverflowAnswer.Answer.Owner("Ivan"),
                Instant.parse("2026-05-15T10:05:00Z").getEpochSecond(),
                "https://stackoverflow.com/a/1",
                "How to test?",
                longBody);

        when(stackOverflowClient.getNewAnswers(
                        eq("123"),
                        eq(link.lastCheck().getEpochSecond()),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()))
                .thenReturn(ResponseEntity.ok(new StackOverflowAnswer(List.of(answer))));
        when(stackOverflowClient.getNewComments(
                        eq("123"),
                        eq(link.lastCheck().getEpochSecond()),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()))
                .thenReturn(ResponseEntity.ok(new StackOverflowComment(List.of())));
        when(scrapperRepository.getTrackingTgChatIds(link.url())).thenReturn(List.of(10L));

        stackOverflowLinkHandler.processLink(link);

        ArgumentCaptor<LinkUpdate> updateCaptor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(messageSender).sendUpdate(updateCaptor.capture());
        assertThat(updateCaptor.getValue().toString())
                .contains("Ivan", "How to test?", "Новый ответ")
                .doesNotContain("b".repeat(201));
    }

    @Test
    void processLinkWhenInvalidQuestionLinkDeletesLinkAndSendsUpdate() {
        LinkForMonitorService link = new LinkForMonitorService(
                2L, URI.create("https://stackoverflow.com/questions"), Instant.parse("2026-05-15T10:00:00Z"));
        when(scrapperRepository.getTrackingTgChatIds(link.url())).thenReturn(List.of(10L));

        stackOverflowLinkHandler.processLink(link);

        verify(scrapperRepository).deleteLink(2L);
        verify(messageSender).sendUpdate(any(LinkUpdate.class));
        verifyNoInteractions(stackOverflowClient);
    }

    @Test
    void processLinkWhenStackOverflowReturnsErrorDoesNotSendUpdate() {
        LinkForMonitorService link = new LinkForMonitorService(
                3L,
                URI.create("https://stackoverflow.com/questions/123/test-question"),
                Instant.parse("2026-05-15T10:00:00Z"));

        when(stackOverflowClient.getNewAnswers(
                        eq("123"),
                        eq(link.lastCheck().getEpochSecond()),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()))
                .thenReturn(
                        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new StackOverflowAnswer(List.of())));
        when(stackOverflowClient.getNewComments(
                        eq("123"),
                        eq(link.lastCheck().getEpochSecond()),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()))
                .thenReturn(ResponseEntity.ok(new StackOverflowComment(List.of())));

        stackOverflowLinkHandler.processLink(link);

        verify(messageSender, never()).sendUpdate(any(LinkUpdate.class));
    }
}
