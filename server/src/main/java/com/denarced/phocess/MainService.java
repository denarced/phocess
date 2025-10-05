package com.denarced.phocess;

import com.denarced.phocess.domain.Game;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MainService {

    private final GameRepository gameRepository;

    @Transactional(readOnly = true)
    public List<Game> fetchBareGames() {
        return gameRepository.findAllByTotalCountIsNullAndFirstCountIsNullAndCountIsNull();
    }
}
