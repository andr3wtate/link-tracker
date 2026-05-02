package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.commondto.dto.AddLink;
import backend.academy.linktracker.commondto.dto.Link;
import backend.academy.linktracker.commondto.dto.RemoveLink;
import java.net.URI;
import java.util.List;

public interface ScrapperRepository {
    boolean isChatRegistered(long chatId);

    boolean chatContainsLink(long chatId, URI url);

    void registerChat(long chatId);

    void deleteChat(long chatId);

    List<Link> getLinksByChatId(long chatId);

    List<URI> getAllLinks();

    List<Long> getTrackingTgChatIds(URI link);

    Link addLinkByChatId(long chatId, AddLink newLink);

    Link deleteLinkByChatId(long chatId, RemoveLink link);
}
