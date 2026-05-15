package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.commondto.dto.AddLink;
import backend.academy.linktracker.commondto.dto.Link;
import backend.academy.linktracker.commondto.dto.RemoveLink;
import backend.academy.linktracker.scrapper.dto.LinkForMonitorService;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "in-memory")
public class InMemoryScrapperRepository implements ScrapperRepository {
    private final Map<Long, List<Link>> chatToLinks = new ConcurrentHashMap<>();

    // link URI -> chatIds that track it
    // also contains all unique links
    private final Map<URI, Set<Long>> linkToChats = new ConcurrentHashMap<>();

    private long linkNumber = 0;

    @Override
    public boolean isChatRegistered(long chatId) {
        return chatToLinks.containsKey(chatId);
    }

    @Override
    public boolean chatContainsLink(long chatId, URI url) {
        return chatToLinks.get(chatId).stream().anyMatch(l -> l.url().equals(url));
    }

    @Override
    public void registerChat(long chatId) {
        if (!chatToLinks.containsKey(chatId)) {
            chatToLinks.put(chatId, new ArrayList<>());
        }
    }

    @Override
    public void deleteChat(long chatId) {
        if (chatToLinks.get(chatId) == null) {
            return;
        }
        for (Link link : chatToLinks.get(chatId)) {
            URI url = link.url();
            linkToChats.get(url).remove(chatId);
            if (linkToChats.get(url).isEmpty()) {
                linkToChats.remove(url);
            }
        }
        chatToLinks.remove(chatId);
    }

    @Override
    public List<Link> getLinksByChatId(long chatId) {
        if (chatToLinks.get(chatId) == null) {
            return List.of();
        }
        return chatToLinks.get(chatId).stream().toList();
    }

    @Override
    public List<URI> getAllLinks() {
        return linkToChats.keySet().stream().toList();
    }

    @Override
    public void deleteLink(long linkId) {
        throw new IllegalStateException("Not implemented in in-memory repo");
    }

    @Override
    public List<LinkForMonitorService> getLinksBatch(long lastLinkId, int batchSize) {
        throw new IllegalStateException("Not implemented in in-memory repo");
    }

    @Override
    public void updateLastCheckTime(long linkId, Instant time) {
        throw new IllegalStateException("Not implemented in in-memory repo");
    }

    @Override
    public List<Long> getTrackingTgChatIds(URI link) {
        return linkToChats.get(link).stream().toList();
    }

    @Override
    public Link addLinkByChatId(long chatId, AddLink newLink) {
        Link link = new Link(linkNumber++, newLink.link(), newLink.tags());
        chatToLinks.get(chatId).add(link);
        if (!linkToChats.containsKey(link.url())) {
            linkToChats.put(link.url(), new HashSet<>());
        }
        linkToChats.get(link.url()).add(chatId);
        return link;
    }

    @Override
    public Link deleteLinkByChatId(long chatId, RemoveLink link) {
        Link del = chatToLinks.get(chatId).stream()
                .filter(l -> l.url().equals(link.link()))
                .findAny()
                .orElseThrow(NullPointerException::new);
        URI url = del.url();
        linkToChats.get(url).remove(chatId);
        if (linkToChats.get(url).isEmpty()) {
            linkToChats.remove(url);
        }
        chatToLinks.get(chatId).remove(del);
        return del;
    }
}
