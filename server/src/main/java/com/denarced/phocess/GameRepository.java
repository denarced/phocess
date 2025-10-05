package com.denarced.phocess;

import com.denarced.phocess.domain.Game;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface GameRepository extends CrudRepository<Game, Long> {
    Game findByName(String name);
    List<Game> findAllByTotalCountIsNullAndFirstCountIsNullAndCountIsNull();
}
