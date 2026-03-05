package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.dto.AddLink;
import backend.academy.linktracker.scrapper.dto.ApiError;
import backend.academy.linktracker.scrapper.dto.Link;
import backend.academy.linktracker.scrapper.dto.ListLinks;
import backend.academy.linktracker.scrapper.dto.RemoveLink;
import backend.academy.linktracker.scrapper.exception.AbstractException;
import backend.academy.linktracker.scrapper.service.ScrapperService;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequiredArgsConstructor
public class ScrapperController {
    private final ScrapperService service;

    @PostMapping("/tg-chat/{id}")
    public void registerChat(@PathVariable("id") long chatId) {
        service.registerChar(chatId);
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
