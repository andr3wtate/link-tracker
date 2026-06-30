package backend.academy.linktracker.scrapper.messaging;

import backend.academy.linktracker.commondto.dto.LinkUpdate;

public interface MessageSender {
    void sendUpdate(LinkUpdate update);
}
