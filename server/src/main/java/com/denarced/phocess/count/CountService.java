package com.denarced.phocess.count;

import com.denarced.phocess.GameRepository;
import com.denarced.phocess.InvalidRequestException;
import com.denarced.phocess.domain.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CountService {

    private final GameRepository gameRepository;

    @Transactional
    public void updateCount(long id, Count countType, long count)
        throws NonexistentGameException, InvalidRequestException {
        if (count < 1) {
            throw new InvalidRequestException("required: count > 0");
        }
        Game game = gameRepository.findById(id).orElseThrow(() -> new NonexistentGameException(id));
        if (
            (countType == Count.FIRST && game.getTotalCount() != null && count > game.getTotalCount()) ||
            (countType == Count.TOTAL && game.getFirstCount() != null && count < game.getFirstCount())
        ) {
            throw new InvalidRequestException("required: first <= total");
        }
        if (
            (countType == Count.FINAL && game.getFirstCount() != null && count > game.getFirstCount()) ||
            (countType == Count.FIRST && game.getCount() != null && game.getCount() > count)
        ) {
            throw new InvalidRequestException("required: final <= first");
        }
        switch (countType) {
            case TOTAL -> game.setTotalCount(count);
            case FIRST -> game.setFirstCount(count);
            case FINAL -> game.setCount(count);
        }
    }
}
