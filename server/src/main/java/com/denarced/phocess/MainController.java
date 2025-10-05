package com.denarced.phocess;

import com.denarced.phocess.domain.Game;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MainController {

    private final MainService mainService;

    @GetMapping("/v1/games")
    public GameListResponse fetchGames() {
        List<Game> games = mainService.fetchBareGames();
        return new GameListResponse(
            games
                .stream()
                .map(e -> new GameListResponse.Game(e.getId(), e.getName()))
                .toList()
        );
    }
}
