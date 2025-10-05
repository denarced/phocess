package com.denarced.phocess;

import java.util.List;

public record GameListResponse(List<Game> games) {
    public record Game(long id, String name) {}
}
