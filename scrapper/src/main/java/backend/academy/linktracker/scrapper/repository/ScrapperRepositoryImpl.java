package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.dto.AddLink;
import backend.academy.linktracker.scrapper.dto.Link;
import backend.academy.linktracker.scrapper.dto.RemoveLink;
import org.springframework.stereotype.Repository;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class ScrapperRepositoryImpl implements ScrapperRepository {
    private final Map<Long, List<Link>> chatToLinks = new HashMap<>();

    // link URI -> chatIds that track it
    // also contains all unique links
    private final Map<URI, Set<Long>> linkToChats = new HashMap<>();

    private long linkNumber = 0;

    @Override
    public boolean isChatRegistered(long chatId) {
        return chatToLinks.containsKey(chatId);
    }

    @Override
    public boolean chatContainsLink(long chatId, URI url) {
        return chatToLinks.get(chatId).stream()
            .anyMatch(l -> l.url().equals(url));
    }

    @Override
    public void registerChat(long chatId) {
        chatToLinks.put(chatId, new ArrayList<>());
    }

    @Override
    public void deleteChat(long chatId) {
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
        return chatToLinks.get(chatId).stream().toList();
    }

    @Override
    public Link addLinkByChatId(long chatId, AddLink newLink) {
        Link link = new Link(linkNumber++, newLink.link(), newLink.tags(), newLink.filters());
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
