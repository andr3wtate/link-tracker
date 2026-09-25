package backend.academy.linktracker.scrapper.messaging.kafka;

import backend.academy.linktracker.commondto.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.messaging.MessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging-type", havingValue = "kafka", matchIfMissing = true)
public class KafkaMessageSender implements MessageSender {
    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    @Override
    public void sendUpdate(LinkUpdate update) {
        kafkaTemplate.send(topic, update.url(), update);
    }
}
