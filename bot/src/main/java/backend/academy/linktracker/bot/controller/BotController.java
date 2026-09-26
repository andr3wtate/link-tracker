package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.service.UpdateService;
import backend.academy.linktracker.commondto.dto.ApiError;
import backend.academy.linktracker.commondto.dto.LinkUpdate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BotController {
    private final UpdateService updateService;

    @PostMapping("/updates")
    public ResponseEntity<?> processUpdates(@RequestBody LinkUpdate update) {
        if (!updateService.sendUpdate(update)) {
            return ResponseEntity.badRequest().body(new ApiError("", "", "", "", List.of()));
        }
        return ResponseEntity.ok().build();
    }
}
