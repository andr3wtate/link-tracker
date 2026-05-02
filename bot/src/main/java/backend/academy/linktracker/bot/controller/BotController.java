package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.commondto.dto.ApiError;
import backend.academy.linktracker.commondto.dto.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BotController {
    private final TelegramBot telegramBot;
    private final BotRepository repository;

    @PostMapping("/updates")
    public ResponseEntity<?> processUpdates(@RequestBody LinkUpdate update) {
        if (update.tgChatIds().stream().anyMatch(id -> !repository.isPresent(id))) {
            return ResponseEntity.badRequest().body(new ApiError("", "", "", "", List.of()));
        }
        for (long chatId : update.tgChatIds()) {
            telegramBot.execute(new SendMessage(
                    chatId, String.format("Изменение в ссылке %s: %n%s", update.url(), update.description())));
        }
        return ResponseEntity.ok().build();
    }
}
