package com.jordanluyke.reversi.match.model;

import com.jordanluyke.reversi.util.RandomUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.Date;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Match {
    private static final Logger logger = LogManager.getLogger(Match.class);

    private String id = RandomUtil.generateRandom(12);
    private Optional<String> playerLightId = Optional.empty();
    private Optional<String> playerDarkId = Optional.empty();
    private Side turn = Side.DARK;
    private Board board = Board.create();
    private Date createdAt = new Date();
    private Optional<Date> completedAt = Optional.empty();
    private Optional<Date> disabledAt = Optional.empty();
    private Optional<String> winnerId = Optional.empty();
    private boolean isPrivate = false;

    public Observable<Match> placePiece(Side side, Position position) {
        if(side != turn || !playerLightId.isPresent() || !playerDarkId.isPresent())
            return Observable.error(new IllegalMoveException());
        return board.placePiece(side, position)
                .flatMap(board1 -> board1.canPlacePiece(side.getOpposite())
                        .flatMap(opposingSideCanPlacePiece -> {
                            if(opposingSideCanPlacePiece) {
                                turn = turn.getOpposite();
                                return Observable.empty();
                            }
                            return board1.canPlacePiece(side)
                                    .flatMap(sameSideCanPlacePiece -> {
                                        if(sameSideCanPlacePiece)
                                            return Observable.empty();
                                        completedAt = Optional.of(new Date());
                                        return Observable.zip(
                                                board1.getAmount(Side.LIGHT),
                                                board1.getAmount(Side.DARK),
                                                (lightAmount, darkAmount) -> {
                                                    if(lightAmount > darkAmount)
                                                        winnerId = playerLightId;
                                                    else if(darkAmount > lightAmount)
                                                        winnerId = playerDarkId;
                                                    // +1 win amount on account
                                                    return null;
                                                }
                                        );
                                    });
                        }))
                .defaultIfEmpty(null)
                .map(Void -> this);
    }

    // join
}
