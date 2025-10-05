package com.denarced.phocess.register;

import com.denarced.phocess.InvalidRequestException;
import com.denarced.phocess.domain.ErrorContainer;
import com.denarced.phocess.domain.Game;
import com.denarced.phocess.domain.StandardError;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;

    @PostMapping("/v1/games")
    public RegisterResponse registerGame(@RequestBody RegisterGameRequest request)
        throws GameExistsException, InvalidRequestException {
        Game game = registerService.registerGame(request);
        return new RegisterResponse(game.getId());
    }

    @ExceptionHandler(GameExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorContainer handleGameExists() {
        return new ErrorContainer(new StandardError("game exists"));
    }
}
