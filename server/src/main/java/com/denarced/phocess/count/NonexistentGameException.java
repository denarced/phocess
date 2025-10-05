package com.denarced.phocess.count;

import lombok.Getter;

@Getter
public class NonexistentGameException extends Exception {

    private final long gameId;

    public NonexistentGameException(long gameId) {
        super("No such game: " + gameId);
        this.gameId = gameId;
    }
}
