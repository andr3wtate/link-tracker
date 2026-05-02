package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.commondto.dto.AddLink;
import backend.academy.linktracker.commondto.dto.Link;
import backend.academy.linktracker.commondto.dto.RemoveLink;
import backend.academy.linktracker.commondto.entity.LinkEntity;
import backend.academy.linktracker.commondto.entity.ScrapperUser;
import backend.academy.linktracker.commondto.entity.Subscription;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
@RequiredArgsConstructor
public class OrmScrapperRepository implements ScrapperRepository {
    private final OrmLinkRepository linkRepository;
    private final OrmScrapperUserRepository scrapperUserRepository;
    private final OrmSubscriptionRepository subscriptionRepository;

    @Override
    public boolean isChatRegistered(long chatId) {
        return scrapperUserRepository.existsByChatId(chatId);
    }

    @Override
    public boolean chatContainsLink(long chatId, URI url) {
        return subscriptionRepository.existsByScrapperUser_ChatIdAndLink_Url(chatId, url.toString());
    }

    @Override
    @Transactional
    public void registerChat(long chatId) {
        scrapperUserRepository.findByChatId(chatId).orElseGet(() -> {
            ScrapperUser newChat = new ScrapperUser();
            newChat.setChatId(chatId);
            return scrapperUserRepository.save(newChat);
        });
    }

    @Override
    @Transactional
    public void deleteChat(long chatId) {
        scrapperUserRepository.findByChatId(chatId).ifPresent(chat -> {
            scrapperUserRepository.deleteById(chat.getId());
            linkRepository.deleteOrphanLinks();
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Link> getLinksByChatId(long chatId) {
        return subscriptionRepository.findLinksByChatId(chatId).stream()
                .map(data -> new Link((Long) data[0], (String) data[1], (List<String>) data[2]))
                .toList();
    }

    @Override
    public List<URI> getAllLinks() {
        return linkRepository.findAll().stream()
                .map(l -> URI.create(l.getUrl()))
                .toList();
    }

    @Override
    public List<Long> getTrackingTgChatIds(URI link) {
        return subscriptionRepository.findTrackingTgChatIds(link.toString());
    }

    @Override
    @Transactional
    public Link addLinkByChatId(long chatId, AddLink newLink) {
        ScrapperUser user = scrapperUserRepository
                .findByChatId(chatId)
                .orElseThrow(() -> new IllegalArgumentException("User is not registered"));

        LinkEntity linkEntity = linkRepository
                .findByUrl(newLink.link().toString())
                .orElseGet(() -> {
                    LinkEntity newLinkEntity = new LinkEntity();
                    newLinkEntity.setUrl(newLink.link().toString());
                    return linkRepository.save(newLinkEntity);
                });

        Subscription subscription = new Subscription(user, linkEntity, newLink.tags());
        subscriptionRepository.save(subscription);

        return new Link(linkEntity.getId(), URI.create(linkEntity.getUrl()), newLink.tags());
    }

    @Override
    @Transactional
    public Link deleteLinkByChatId(long chatId, RemoveLink removeLink) {
        ScrapperUser user = scrapperUserRepository
                .findByChatId(chatId)
                .orElseThrow(() -> new IllegalArgumentException("User is not registered"));

        LinkEntity linkEntity = linkRepository
                .findByUrl(removeLink.link().toString())
                .orElseThrow(() -> new IllegalArgumentException("Link is not tracked by anyone"));

        subscriptionRepository.deleteByScrapperUser_IdAndLinkId(user.getId(), linkEntity.getId());
        if (subscriptionRepository.countByLink_Id(linkEntity.getId()) == 0) {
            linkRepository.deleteById(linkEntity.getId());
        }

        return new Link(linkEntity.getId(), removeLink.link(), List.of());
    }
}
