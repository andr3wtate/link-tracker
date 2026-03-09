package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.commondto.LinkUpdate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface ScrapperClient {

    @PostExchange("/updates")
    void sendUpdates(@RequestBody LinkUpdate update);
}
