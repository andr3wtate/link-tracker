package backend.academy.linktracker.scrapper.service.linkhandler;

import backend.academy.linktracker.commondto.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.dto.GitHubIssue;
import backend.academy.linktracker.scrapper.dto.LinkForMonitorService;
import backend.academy.linktracker.scrapper.messaging.MessageSender;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitHubLinkHandler implements LinkHandler {
    private final ScrapperRepository scrapperRepository;
    private final GitHubClient gitHubClient;
    private final MessageSender messageSender;

    @Override
    public void processLink(LinkForMonitorService link) {
        String[] ownerAndRepo = getOwnerAndRepo(link.url());
        if (ownerAndRepo == null) {
            sendUpdate(
                    link,
                    "Некорректная ссылка github, должен быть указан owner и repo, она будет удалена из отслеживания");
            scrapperRepository.deleteLink(link.id());
            return;
        }
        String queryForIssue = String.format(
                "is:issue repo:%s/%s created:>%s",
                ownerAndRepo[0], ownerAndRepo[1], link.lastCheck().toString());
        String queryForPr = String.format(
                "is:pull-request repo:%s/%s created:>%s",
                ownerAndRepo[0], ownerAndRepo[1], link.lastCheck().toString());
        ResponseEntity<GitHubIssue> responseForIssue = gitHubClient.getChanges(queryForIssue);
        ResponseEntity<GitHubIssue> responseForPr = gitHubClient.getChanges(queryForPr);
        if (!responseForIssue.getStatusCode().is2xxSuccessful()
                || !responseForPr.getStatusCode().is2xxSuccessful()) {
            log.atWarn().addKeyValue("Link", link).log("GitHub response is {}", responseForPr.getStatusCode());
            return;
        }
        GitHubIssue issues = responseForIssue.getBody();
        GitHubIssue pr = responseForPr.getBody();
        List<GitHubIssue.Issue> both = new ArrayList<>();
        if (issues != null && issues.totalCount() != 0) {
            both.addAll(issues.items());
        }
        if (pr != null && pr.totalCount() != 0) {
            both.addAll(pr.items());
        }
        if (!both.isEmpty()) {
            String description = formDescription(both);
            sendUpdate(link, description);
        }
    }

    private void sendUpdate(LinkForMonitorService link, String description) {
        messageSender.sendUpdate(new LinkUpdate(
                link.url().toString(), description, scrapperRepository.getTrackingTgChatIds(link.url())));
    }

    private String formDescription(List<GitHubIssue.Issue> issues) {
        StringBuilder description = new StringBuilder("Изменения в репозиториях: \n");
        int cnt = 1;
        for (GitHubIssue.Issue issue : issues) {
            description
                    .append(cnt)
                    .append(". ")
                    .append("Новый ")
                    .append(issue.pullRequest() == null ? "Issue" : "Pull Request")
                    .append(" в репозитории ")
                    .append(issue.url())
                    .append(".\n Заголовок - ")
                    .append(issue.title())
                    .append("\n Пользователь - ")
                    .append(issue.user() != null ? issue.user().login() : "")
                    .append("\n Время создания - ")
                    .append(issue.createdAt())
                    .append("\nПревью описания - ")
                    .append(
                            issue.body() != null
                                    ? issue.body()
                                            .substring(
                                                    0,
                                                    Math.min(200, issue.body().length()))
                                    : "")
                    .append("\n");
            cnt++;
        }
        return description.toString();
    }

    private String[] getOwnerAndRepo(URI link) {
        String path = link.getPath();
        List<String> parts =
                Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toList();
        if (parts.size() < 2) {
            return null;
        }
        return new String[] {parts.get(0), parts.get(1)};
    }
}
