package backend.academy.linktracker.scrapper.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.commondto.dto.LinkUpdate;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaMessageSenderTest {
    @Test
    @SuppressWarnings("unchecked")
    void publishesTheHttpDtoAsJsonWithUrlKey() {
        KafkaTemplate<String, LinkUpdate> template = org.mockito.Mockito.mock(KafkaTemplate.class);
        KafkaMessageSender sender = new KafkaMessageSender(template);
        ReflectionTestUtils.setField(sender, "topic", "link-updates");
        LinkUpdate update = new LinkUpdate("https://github.com/owner/repo", "New issue", List.of(1L, 2L));

        sender.sendUpdate(update);

        verify(template).send("link-updates", update.url(), update);
        byte[] json = new JsonSerializer<LinkUpdate>().serialize("link-updates", update);
        String payload = new String(json, StandardCharsets.UTF_8);
        assertThat(payload).contains("\"url\":\"https://github.com/owner/repo\"");
        assertThat(payload).contains("\"description\":\"New issue\"");
        assertThat(payload).contains("\"tgChatIds\":[1,2]");
    }
}
