package com.jordanluyke.reversi.match.model;

import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Match {
    private static final Logger logger = LogManager.getLogger(Match.class);

    private String id = RandomUtil.generateId();
    private Optional<String> playerDarkId = Optional.empty();
    private Optional<String> playerLightId = Optional.empty();
    private Side turn = Side.DARK;
    private Board board = Board.create();
    private Instant createdAt = Instant.now();
    private Optional<Instant> completedAt = Optional.empty();
    private Optional<Instant> disabledAt = Optional.empty();
    private Optional<String> winnerId = Optional.empty();
    private boolean isPrivate = false;

    public Observable<Match> placePiece(Side side, Position position) {
        if(completedAt.isPresent())
            return Observable.error(new WebException("Game completed", HttpResponseStatus.INTERNAL_SERVER_ERROR));
        if(side != turn)
            return Observable.error(new WebException("Not your turn", HttpResponseStatus.INTERNAL_SERVER_ERROR));
        if(!playerLightId.isPresent() || !playerDarkId.isPresent())
            return Observable.error(new WebException("Two players required", HttpResponseStatus.INTERNAL_SERVER_ERROR));
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
                                        completedAt = Optional.of(Instant.now());
                                        return Observable.zip(
                                                board1.getAmount(Side.LIGHT),
                                                board1.getAmount(Side.DARK),
                                                (lightAmount, darkAmount) -> {
                                                    if(lightAmount > darkAmount)
                                                        winnerId = playerLightId;
                                                    else if(darkAmount > lightAmount)
                                                        winnerId = playerDarkId;
                                                    return null;
                                                }
                                        );
                                    });
                        }))
                .defaultIfEmpty(null)
                .map(Void -> this);
    }

    public Observable<Match> join(String accountId) {
        if(!playerDarkId.isPresent())
            playerDarkId = Optional.of(accountId);
        else if(!playerLightId.isPresent())
            playerLightId = Optional.of(accountId);
        else
            return Observable.error(new WebException("Too many players", HttpResponseStatus.INTERNAL_SERVER_ERROR));
        return Observable.just(this);
    }
}
