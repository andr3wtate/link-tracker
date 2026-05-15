package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.commondto.dto.AddLink;
import backend.academy.linktracker.commondto.dto.Link;
import backend.academy.linktracker.commondto.dto.RemoveLink;
import backend.academy.linktracker.scrapper.dto.LinkForMonitorService;
import java.net.URI;
import java.time.Instant;
import java.util.List;

public interface ScrapperRepository {
    boolean isChatRegistered(long chatId);

    boolean chatContainsLink(long chatId, URI url);

    void registerChat(long chatId);

    void deleteChat(long chatId);

    List<Link> getLinksByChatId(long chatId);

    List<URI> getAllLinks();

    void deleteLink(long linkId);

    List<LinkForMonitorService> getLinksBatch(long lastLinkId, int batchSize);

    void updateLastCheckTime(long linkId, Instant time);

    List<Long> getTrackingTgChatIds(URI link);

    Link addLinkByChatId(long chatId, AddLink newLink);

    Link deleteLinkByChatId(long chatId, RemoveLink link);
}
