package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.dto.AddLink;
import backend.academy.linktracker.scrapper.dto.Link;
import backend.academy.linktracker.scrapper.dto.RemoveLink;
import java.net.URI;
import java.util.List;

public interface ScrapperRepository {
    boolean isChatRegistered(long chatId);
    boolean chatContainsLink(long chatId, URI url);

    void registerChat(long chatId);
    void deleteChat(long chatId);
    List<Link> getLinksByChatId(long chatId);
    Link addLinkByChatId(long chatId, AddLink newLink);
    Link deleteLinkByChatId(long chatId, RemoveLink link);
}
