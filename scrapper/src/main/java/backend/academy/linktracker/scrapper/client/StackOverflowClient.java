package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.StackOverflowAnswer;
import backend.academy.linktracker.scrapper.dto.StackOverflowComment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface StackOverflowClient {
    @GetExchange("/questions/{ids}/answers")
    ResponseEntity<StackOverflowAnswer> getNewAnswers(
            @PathVariable("ids") String ids, // up to 100 semicolon delimited ids
            @RequestParam("fromdate") Long fromDate,
            @RequestParam("order") String order,
            @RequestParam("sort") String sort,
            @RequestParam("site") String site,
            @RequestParam("filter") String filter);

    @GetExchange("/questions/{ids}/comments")
    ResponseEntity<StackOverflowComment> getNewComments(
            @PathVariable("ids") String ids, // up to 100 semicolon delimited ids
            @RequestParam("fromdate") Long fromDate,
            @RequestParam("order") String order,
            @RequestParam("sort") String sort,
            @RequestParam("site") String site,
            @RequestParam("filter") String filter);
}
