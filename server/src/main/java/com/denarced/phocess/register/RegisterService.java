package com.denarced.phocess.register;

import com.denarced.phocess.GameRepository;
import com.denarced.phocess.InvalidRequestException;
import com.denarced.phocess.domain.Game;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@RequiredArgsConstructor
@Service
public class RegisterService {

    private final GameRepository gameRepository;

    @Transactional
    public Game registerGame(RegisterGameRequest request) throws GameExistsException, InvalidRequestException {
        Game game = new Game();
        checkGameName(request.name());
        game.setName(request.name());
        // TODO Validate date.
        game.setDate(request.date());
        try {
            gameRepository.save(game);
        } catch (DataIntegrityViolationException e) {
            throw new GameExistsException(e);
        }
        return game;
    }

    private static void checkGameName(String name) throws InvalidRequestException {
        name = StringUtils.trimToNull(name);
        if (name == null) {
            throw new InvalidRequestException("game name can't be blank");
        }
        String[] pieces = name.split("\\s+");
        InvalidRequestException exception = new InvalidRequestException("game name must be of form \"teamA vs teamB\"");
        if (pieces.length < 3) {
            throw exception;
        }
        Arrays.stream(pieces)
            .filter("vs"::equals)
            .findFirst()
            .orElseThrow(() -> exception);
    }
}
