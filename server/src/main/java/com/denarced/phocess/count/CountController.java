package com.denarced.phocess.count;

import com.denarced.phocess.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CountController {

    private final CountService countService;

    @PostMapping("/v1/games/{id}/total/{count}")
    public void updateTotal(@PathVariable long id, @PathVariable long count)
        throws NonexistentGameException, InvalidRequestException {
        countService.updateCount(id, Count.TOTAL, count);
    }

    @PostMapping("/v1/games/{id}/first/{count}")
    public void updateFirst(@PathVariable long id, @PathVariable long count)
        throws NonexistentGameException, InvalidRequestException {
        countService.updateCount(id, Count.FIRST, count);
    }

    @PostMapping("/v1/games/{id}/final/{count}")
    public void updateFinal(@PathVariable long id, @PathVariable long count)
        throws NonexistentGameException, InvalidRequestException {
        countService.updateCount(id, Count.FINAL, count);
    }

    @ExceptionHandler(NonexistentGameException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNonexistentGame() {}
}
