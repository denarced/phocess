package com.denarced.phocess.register;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.denarced.phocess.GameRepository;
import com.denarced.phocess.IntegrationTest;
import com.denarced.phocess.InvalidRequestException;
import com.denarced.phocess.domain.Game;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
public class RegisterServiceRegisterGameTest {

    public static final LocalDate GAME_DATE = LocalDate.of(2025, Month.JULY, 22);

    private final RegisterService registerService;
    private final GameRepository gameRepository;

    @Autowired
    RegisterServiceRegisterGameTest(RegisterService registerService, GameRepository gameRepository) {
        this.registerService = registerService;
        this.gameRepository = gameRepository;
    }

    @Test
    void testRegisterGame() throws GameExistsException, InvalidRequestException {
        String gameName = "konky vs donky";
        Game game = registerService.registerGame(createRegisterRequest(gameName));
        Game expected = createGameWithName(game.getId(), gameName);
        assertEquals(expected, game);
    }

    @Test
    void testAutoIncrement() throws GameExistsException, InvalidRequestException {
        registerService.registerGame(createRegisterRequest("daughter vs mother"));
        registerService.registerGame(createRegisterRequest("son vs father"));
        List<Long> ids = StreamSupport.stream(gameRepository.findAll().spliterator(), false)
            .map(Game::getId)
            .sorted()
            .toList();
        long first = ids.getFirst();
        for (Long each : ids.subList(1, ids.size())) {
            assertEquals(first + 1, each);
            first = each;
        }
    }

    /**
     * Verify that same game name isn't allowed when the dates are the same as well.
     */
    @Test
    void testSameNameSameDate() throws GameExistsException, InvalidRequestException {
        String name = "black vs white";
        registerService.registerGame(createRegisterRequest(name));
        assertThrows(GameExistsException.class, () -> registerService.registerGame(createRegisterRequest(name)));
    }

    /**
     * Verify that same names are allowed when dates are different.
     */
    @Test
    void testSameNameDifferentDates() throws InvalidRequestException, GameExistsException {
        String gameName = "dave vs danger";
        RegisterGameRequest firstRequest = createRegisterRequest(gameName);
        registerService.registerGame(firstRequest);
        registerService.registerGame(createRegisterRequest(gameName, firstRequest.date().plusDays(1L)));
    }

    private static Game createGameWithName(long id, String name) {
        Game expected = new Game();
        expected.setId(id);
        expected.setName(name);
        expected.setDate(GAME_DATE);
        return expected;
    }

    private static RegisterGameRequest createRegisterRequest(String name) {
        return createRegisterRequest(name, GAME_DATE);
    }

    private static RegisterGameRequest createRegisterRequest(String name, LocalDate date) {
        return new RegisterGameRequest(date, name);
    }
}
