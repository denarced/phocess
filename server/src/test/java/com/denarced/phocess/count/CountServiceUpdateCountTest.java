package com.denarced.phocess.count;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.denarced.phocess.GameRepository;
import com.denarced.phocess.IntegrationTest;
import com.denarced.phocess.InvalidRequestException;
import com.denarced.phocess.domain.Game;
import com.denarced.phocess.register.GameExistsException;
import com.denarced.phocess.register.RegisterGameRequest;
import com.denarced.phocess.register.RegisterService;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
public class CountServiceUpdateCountTest {

    public static final LocalDate GAME_DATE = LocalDate.of(2025, Month.JULY, 22);

    private final GameRepository gameRepository;
    private final RegisterService registerService;
    private final CountService countService;

    @Autowired
    CountServiceUpdateCountTest(
        GameRepository gameRepository,
        RegisterService registerService,
        CountService countService
    ) {
        this.gameRepository = gameRepository;
        this.registerService = registerService;
        this.countService = countService;
    }

    @Test
    void testHappyPath() throws GameExistsException, NonexistentGameException, InvalidRequestException {
        long id = registerService.registerGame(createRegisterRequest("moon vs sun")).getId();

        for (CountServiceUpdateCountTest.CountTrip each : List.of(
            new CountServiceUpdateCountTest.CountTrip(() -> fetchGameById(id).getTotalCount(), Count.TOTAL, 10L),
            new CountServiceUpdateCountTest.CountTrip(() -> fetchGameById(id).getFirstCount(), Count.FIRST, 7L),
            new CountServiceUpdateCountTest.CountTrip(() -> fetchGameById(id).getCount(), Count.FINAL, 4L)
        )) {
            countService.updateCount(id, each.countType, each.count);
            assertEquals(each.count, each.supplier.get(), each.countType.name());
        }
    }

    @ParameterizedTest
    @MethodSource
    void testInvalidCount(
        String gameName,
        List<CountServiceUpdateCountTest.CountPair> setupPairs,
        CountServiceUpdateCountTest.CountPair finalPair,
        String expectedMessage
    ) throws GameExistsException, NonexistentGameException, InvalidRequestException {
        long id = registerService.registerGame(createRegisterRequest(gameName)).getId();
        for (CountServiceUpdateCountTest.CountPair each : setupPairs) {
            countService.updateCount(id, each.countType, each.count);
        }

        // EXERCISE
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            countService.updateCount(id, finalPair.countType, finalPair.count())
        );
        assertEquals(expectedMessage, exception.getMessage());
    }

    static Stream<Arguments> testInvalidCount() {
        return Stream.of(
            Arguments.of(
                "dimitri vs andrei",
                List.of(new CountServiceUpdateCountTest.CountPair(Count.TOTAL, 1L)),
                new CountServiceUpdateCountTest.CountPair(Count.TOTAL, 0L),
                "required: count > 0"
            ),
            Arguments.of(
                "mira vs tara",
                List.of(new CountServiceUpdateCountTest.CountPair(Count.TOTAL, 1L)),
                new CountServiceUpdateCountTest.CountPair(Count.TOTAL, -1L),
                "required: count > 0"
            ),
            Arguments.of(
                "dora vs borat",
                List.of(new CountServiceUpdateCountTest.CountPair(Count.TOTAL, 10L)),
                new CountServiceUpdateCountTest.CountPair(Count.FIRST, 11L),
                "required: first <= total"
            ),
            Arguments.of(
                "mike vs yikes",
                List.of(new CountServiceUpdateCountTest.CountPair(Count.FIRST, 20L)),
                new CountServiceUpdateCountTest.CountPair(Count.FINAL, 21L),
                "required: final <= first"
            )
        );
    }

    record CountPair(Count countType, long count) {}

    private @NotNull Game fetchGameById(long id) {
        return gameRepository.findById(id).orElseThrow();
    }

    private static RegisterGameRequest createRegisterRequest(String name) {
        return new RegisterGameRequest(GAME_DATE, name);
    }

    record CountTrip(Supplier<Long> supplier, Count countType, long count) {}
}
