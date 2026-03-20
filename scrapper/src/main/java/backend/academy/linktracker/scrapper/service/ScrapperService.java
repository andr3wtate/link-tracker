package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.commondto.AddLink;
import backend.academy.linktracker.commondto.Link;
import backend.academy.linktracker.commondto.ListLinks;
import backend.academy.linktracker.commondto.RemoveLink;
import backend.academy.linktracker.scrapper.exception.InvalidRequestParametersException;
import backend.academy.linktracker.scrapper.exception.ItemAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ItemNotFoundException;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScrapperService {
    private final ScrapperRepository repository;

    public void registerChat(long chatId) {
        checkId(chatId);
        if (repository.isChatRegistered(chatId)) {
            throw new ItemAlreadyExistsException("Chat already exists: " + chatId);
        }
        repository.registerChat(chatId);
    }

    public void deleteChat(long chatId) {
        checkId(chatId);
        checkIfChatRegistered(chatId);
        repository.deleteChat(chatId);
    }

    private void checkId(long id) {
        if (id < 0) {
            throw new InvalidRequestParametersException("Negative id");
        }
    }

    private void checkIfChatRegistered(long chatId) {
        if (!repository.isChatRegistered(chatId)) {
            throw new ItemNotFoundException("Chat doesn't exists: " + chatId);
        }
    }

    public ListLinks listTrackedLinks(long chatId) {
        checkId(chatId);
        checkIfChatRegistered(chatId);
        List<Link> links = repository.getLinksByChatId(chatId);
        return new ListLinks(links, links.size());
    }

    public Link addLinkTracking(long chatId, AddLink newLink) {
        checkId(chatId);
        checkIfChatRegistered(chatId);
        if (repository.chatContainsLink(chatId, newLink.link())) {
            throw new ItemAlreadyExistsException(
                    String.format("Link %s is already tracked by chat %s", newLink.link(), chatId));
        }
        return repository.addLinkByChatId(chatId, newLink);
    }

    public Link removeLinkTracking(long chatId, RemoveLink removeLink) {
        checkId(chatId);
        checkIfChatRegistered(chatId);
        if (!repository.chatContainsLink(chatId, removeLink.link())) {
            throw new ItemNotFoundException(String.format("Chat %s doesn't track link %s", chatId, removeLink.link()));
        }
        return repository.deleteLinkByChatId(chatId, removeLink);
    }
}
