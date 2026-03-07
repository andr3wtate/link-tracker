package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.commondto.AddLink;
import backend.academy.linktracker.commondto.Link;
import backend.academy.linktracker.commondto.ListLinks;
import backend.academy.linktracker.commondto.RemoveLink;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public interface BotClient {
    @PostExchange("/tg-chat/{id}")
    void registerChat(@PathVariable("id") long chatId);

    @GetExchange("/links")
    ListLinks getTrackedLinks(@RequestHeader("Tg-Chat-Id") long chatId);

    @PostExchange("/links")
    Link addLinkTracking(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody AddLink addLink);

    @DeleteExchange
    Link removeLinkTracking(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody RemoveLink removeLink);
}
