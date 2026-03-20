package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.commondto.AddLink;
import backend.academy.linktracker.commondto.Link;
import backend.academy.linktracker.commondto.ListLinks;
import backend.academy.linktracker.commondto.RemoveLink;
import backend.academy.linktracker.scrapper.service.ScrapperService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScrapperController {
    private final ScrapperService service;

    @PostMapping("/tg-chat/{id}")
    public void registerChat(@PathVariable("id") long chatId) {
        service.registerChat(chatId);
    }

    @DeleteMapping("/tg-chat/{id}")
    public void deleteChat(@PathVariable("id") long chatId) {
        service.deleteChat(chatId);
    }

    @GetMapping("/links")
    public ListLinks listTrackedLinks(@RequestHeader("Tg-Chat-Id") long chatId) {
        return service.listTrackedLinks(chatId);
    }

    @PostMapping("/links")
    public Link addLinkTracking(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody AddLink newLink) {
        return service.addLinkTracking(chatId, newLink);
    }

    @DeleteMapping("/links")
    public Link removeLinkTracking(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody RemoveLink removeLink) {
        return service.removeLinkTracking(chatId, removeLink);
    }
}
