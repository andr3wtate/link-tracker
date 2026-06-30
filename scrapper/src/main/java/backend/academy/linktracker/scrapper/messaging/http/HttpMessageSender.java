package backend.academy.linktracker.scrapper.messaging.http;

import backend.academy.linktracker.commondto.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.ScrapperClient;
import backend.academy.linktracker.scrapper.messaging.MessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging-type", havingValue = "http")
public class HttpMessageSender implements MessageSender {
    private final ScrapperClient scrapperClient;

    @Override
    public void sendUpdate(LinkUpdate update) {
        scrapperClient.sendUpdates(update);
    }
}
