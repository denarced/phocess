package com.denarced.phocess;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.denarced.phocess.count.Count;
import com.denarced.phocess.count.CountService;
import com.denarced.phocess.count.NonexistentGameException;
import com.denarced.phocess.register.GameExistsException;
import com.denarced.phocess.register.RegisterGameRequest;
import com.denarced.phocess.register.RegisterService;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class MainServiceFetchBareGamesTest {

    public static final LocalDate GAME_DATE = LocalDate.of(2025, Month.JULY, 22);
    private final MainService mainService;
    private final RegisterService registerService;
    private final CountService countService;

    @Autowired
    MainServiceFetchBareGamesTest(MainService mainService, RegisterService registerService, CountService countService) {
        this.mainService = mainService;
        this.registerService = registerService;
        this.countService = countService;
    }

    @Test
    void testWithGames() throws GameExistsException, NonexistentGameException, InvalidRequestException {
        countService.updateCount(registerService.registerGame(createRegisterRequest("a vs b")).getId(), Count.TOTAL, 1);

        String gameName = "holy vs sane";
        registerService.registerGame(createRegisterRequest(gameName));
        assertEquals(gameName, mainService.fetchBareGames().getFirst().getName());
    }

    @Test
    void testWithoutGames() {
        assertEquals(0, mainService.fetchBareGames().size());
    }

    private static RegisterGameRequest createRegisterRequest(String name) {
        return new RegisterGameRequest(GAME_DATE, name);
    }
}
