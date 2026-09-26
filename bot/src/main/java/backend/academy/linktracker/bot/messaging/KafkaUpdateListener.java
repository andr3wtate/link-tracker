package backend.academy.linktracker.bot.messaging;

import backend.academy.linktracker.bot.service.UpdateService;
import backend.academy.linktracker.commondto.dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaUpdateListener {
    private final UpdateService updateService;

    @KafkaListener(topics = "${app.kafka.topic}")
    public void receive(LinkUpdate update) {
        updateService.sendUpdate(update);
    }
}
