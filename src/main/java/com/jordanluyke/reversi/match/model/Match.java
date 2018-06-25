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
    private String playerLightId;
    private String playerDarkId;
    private Side turn = Side.DARK;
    private Board board = Board.create();
    private Date createdAt = new Date();
    private Optional<Date> completedAt = Optional.empty();

    public Observable<Match> placePiece(Side side, Position position) {
        if(side != turn)
            return Observable.error(new IllegalMoveException());
        return board.placePiece(side, position)
                .flatMap(board1 -> Observable.zip(
                        board1.canPlacePiece(side.getOpposite()),
                        board1.canPlacePiece(side),
                        (opposingSideCanPlacePiece, sameSideCanPlacePiece) -> {
                            if(opposingSideCanPlacePiece)
                                turn = turn.getOpposite();
                            else if(!opposingSideCanPlacePiece && !sameSideCanPlacePiece)
                                completedAt = Optional.of(new Date());
                            return null;
                        })
                )
                .map(Void -> this);
    }
}
