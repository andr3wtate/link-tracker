package backend.academy.linktracker.scrapper.service.linkhandler;

import backend.academy.linktracker.commondto.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.LinkForMonitorService;
import backend.academy.linktracker.scrapper.dto.StackOverflowAnswer;
import backend.academy.linktracker.scrapper.dto.StackOverflowComment;
import backend.academy.linktracker.scrapper.messaging.MessageSender;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StackOverflowLinkHandler implements LinkHandler {
    private final ScrapperRepository scrapperRepository;
    private final StackOverflowClient stackOverflowClient;
    private final MessageSender messageSender;
    private final String FILTER_FOR_ANSWER = "!)4iDr6u_2od4qbw5MzOKiVWZbL1P";
    private final String FILTER_FOR_COMMENT = "!.FdE*krbNsJ(gZAHh7KHv-DaWSAz8";

    @Override
    public void processLink(LinkForMonitorService link) {
        String id = getId(link.url());
        if (id == null) {
            sendUpdate(
                    link,
                    "Некорректная ссылка на вопрос stackoverflow (нет id вопроса), она будет удалена из отслеживания");
            scrapperRepository.deleteLink(link.id());
            return;
        }
        ResponseEntity<StackOverflowAnswer> newAnswers = stackOverflowClient.getNewAnswers(
                id, link.lastCheck().getEpochSecond(), "desc", "votes", "stackoverflow", FILTER_FOR_ANSWER);
        ResponseEntity<StackOverflowComment> newComments = stackOverflowClient.getNewComments(
                id, link.lastCheck().getEpochSecond(), "desc", "votes", "stackoverflow", FILTER_FOR_COMMENT);
        if (!newAnswers.getStatusCode().is2xxSuccessful()
                || !newComments.getStatusCode().is2xxSuccessful()) {
            log.atWarn().addKeyValue("Link", link).log("Github response is {}", newAnswers.getStatusCode());
            return;
        }
        String description = "";
        if (newAnswers.getBody() != null && !newAnswers.getBody().items().isEmpty()) {
            description = formAnswersDescription(newAnswers.getBody().items());
        }
        if (newComments.getBody() != null && !newComments.getBody().items().isEmpty()) {
            description =
                    description + formCommentsDescription(newComments.getBody().items());
        }
        if (!description.isEmpty()) {
            sendUpdate(link, description);
        }
    }

    private void sendUpdate(LinkForMonitorService link, String description) {
        messageSender.sendUpdate(new LinkUpdate(
                link.url().toString(), description, scrapperRepository.getTrackingTgChatIds(link.url())));
    }

    private String formAnswersDescription(List<StackOverflowAnswer.Answer> answers) {
        StringBuilder description = new StringBuilder("Изменения в ответах на вопросы вопросах\n");
        int cnt = 1;
        for (StackOverflowAnswer.Answer answer : answers) {
            description
                    .append(cnt)
                    .append(". Новый ответ на вопрос по ссылке ")
                    .append(answer.link())
                    .append(".\nТема вопроса - ")
                    .append(answer.title())
                    .append(".\nИмя пользователя - ")
                    .append(answer.owner().displayName())
                    .append(".\nВремя создания - ")
                    .append(Instant.ofEpochSecond(answer.creationDate()))
                    .append(".\nПревью ответа - ")
                    .append(
                            answer.body() == null
                                    ? ""
                                    : answer.body()
                                            .substring(0, Math.min(answer.body().length(), 200)));
        }
        return description.toString();
    }

    private String formCommentsDescription(List<StackOverflowComment.Comment> comments) {
        StringBuilder description = new StringBuilder("Изменения в комментариях к вопросам\n");
        int cnt = 1;
        for (StackOverflowComment.Comment comment : comments) {
            description
                    .append(cnt)
                    .append(". Новый комментарий на вопрос по ссылке ")
                    .append(comment.link())
                    .append(".\nИмя пользователя - ")
                    .append(comment.owner().displayName())
                    .append(".\nВремя создания - ")
                    .append(Instant.ofEpochSecond(comment.creationDate()))
                    .append(".\nПревью ответа - ")
                    .append(
                            comment.body() == null
                                    ? ""
                                    : comment.body()
                                            .substring(
                                                    0, Math.min(comment.body().length(), 200)));
        }
        return description.toString();
    }

    private String getId(URI link) {
        String path = link.getPath();
        List<String> parts =
                Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toList();
        if (parts.size() < 2) {
            return null;
        }
        return parts.get(1);
    }
}
