package backend.academy.linktracker.scrapper.repositorytest;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.commondto.dto.AddLink;
import backend.academy.linktracker.commondto.dto.Link;
import backend.academy.linktracker.commondto.dto.RemoveLink;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ScrapperRepositoryTest extends BaseRepositoryTest {
    @Autowired
    protected ScrapperRepository scrapperRepository;

    @Test
    void testRegisterAndIsChatRegistered() {
        long chatId = 12345L;
        assertThat(scrapperRepository.isChatRegistered(chatId)).isFalse();
        scrapperRepository.registerChat(chatId);
        assertThat(scrapperRepository.isChatRegistered(chatId)).isTrue();
    }

    @Test
    void testAddLink() {
        long chatId = 67890L;
        scrapperRepository.registerChat(chatId);
        AddLink addLink = new AddLink(URI.create("https://example.com"), List.of("tag1", "tag2"));
        Link addedLink = scrapperRepository.addLinkByChatId(chatId, addLink);
        assertThat(addedLink.url()).isEqualTo(URI.create("https://example.com"));
        assertThat(addedLink.tags()).containsExactly("tag1", "tag2");

        List<Link> links = scrapperRepository.getLinksByChatId(chatId);
        assertThat(links).hasSize(1);
        assertThat(links.getFirst().url()).isEqualTo(URI.create("https://example.com"));
    }

    @Test
    void testDeleteLink() {
        long chatId = 11111L;
        scrapperRepository.registerChat(chatId);
        AddLink addLink = new AddLink(URI.create("https://delete.com"), List.of());
        scrapperRepository.addLinkByChatId(chatId, addLink);

        RemoveLink removeLink = new RemoveLink(URI.create("https://delete.com"));
        Link deleted = scrapperRepository.deleteLinkByChatId(chatId, removeLink);
        assertThat(deleted.url()).isEqualTo(URI.create("https://delete.com"));

        assertThat(scrapperRepository.getLinksByChatId(chatId)).isEmpty();
    }

    @Test
    void testAddDuplicateLink() {
        long chatId = 99999L;
        scrapperRepository.registerChat(chatId);
        AddLink addLink = new AddLink(URI.create("https://duplicate.com"), List.of());
        scrapperRepository.addLinkByChatId(chatId, addLink);
        assertThat(scrapperRepository.getLinksByChatId(chatId)).hasSize(1);
    }

    @Test
    void testChatContainsLink() {
        long chatId = 55555L;
        scrapperRepository.registerChat(chatId);
        AddLink addLink = new AddLink(URI.create("https://contains.com"), List.of());
        scrapperRepository.addLinkByChatId(chatId, addLink);
        assertThat(scrapperRepository.chatContainsLink(chatId, URI.create("https://contains.com")))
                .isTrue();
    }
}
