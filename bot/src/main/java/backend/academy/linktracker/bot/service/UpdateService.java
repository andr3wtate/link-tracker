package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.commondto.dto.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateService {
    private final TelegramBot telegramBot;
    private final BotRepository repository;

    public boolean sendUpdate(LinkUpdate update) {
        if (update == null || update.tgChatIds() == null
                || update.tgChatIds().stream().anyMatch(id -> id == null || !repository.isPresent(id))) {
            return false;
        }
        for (long chatId : update.tgChatIds()) {
            var response = telegramBot.execute(new SendMessage(
                    chatId, String.format("Уведомление по ссылке %s: %n%s", update.url(), update.description())));
            if (response != null && !response.isOk()) {
                throw new IllegalStateException("Telegram rejected update for chat " + chatId);
            }
        }
        return true;
    }
}
