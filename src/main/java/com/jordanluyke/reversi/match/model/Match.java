package com.jordanluyke.reversi.match.model;

import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
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

    @Builder.Default private String id = RandomUtil.generateId();
    @Builder.Default private Optional<String> playerDarkId = Optional.empty();
    @Builder.Default private Optional<String> playerLightId = Optional.empty();
    @Builder.Default private Side turn = Side.DARK;
    @Builder.Default private Board board = Board.create();
    @Builder.Default private Instant createdAt = Instant.now();
    @Builder.Default private Optional<Instant> completedAt = Optional.empty();
    @Builder.Default private Optional<Instant> disabledAt = Optional.empty();
    @Builder.Default private Optional<String> winnerId = Optional.empty();
    @Builder.Default private boolean isPrivate = false;

    public Single<Match> placePiece(Side side, Position position) {
        if(completedAt.isPresent())
            return Single.error(new WebException("Game completed", HttpResponseStatus.INTERNAL_SERVER_ERROR));
        if(side != turn)
            return Single.error(new WebException("Not your turn", HttpResponseStatus.INTERNAL_SERVER_ERROR));
        if(!playerLightId.isPresent() || !playerDarkId.isPresent())
            return Single.error(new WebException("Two players required", HttpResponseStatus.INTERNAL_SERVER_ERROR));
        return board.placePiece(side, position)
                .flatMap(board1 -> board1.canPlacePiece(side.getOpposite())
                        .flatMap(opposingSideCanPlacePiece -> {
                            if(opposingSideCanPlacePiece) {
                                turn = turn.getOpposite();
                                return Single.just(this);
                            }
                            return board1.canPlacePiece(side)
                                    .flatMap(sameSideCanPlacePiece -> {
                                        if(sameSideCanPlacePiece)
                                            return Single.just(this);
                                        completedAt = Optional.of(Instant.now());
                                        return Single.zip(
                                                board1.getAmount(Side.LIGHT),
                                                board1.getAmount(Side.DARK),
                                                (lightAmount, darkAmount) -> {
                                                    if(lightAmount > darkAmount)
                                                        winnerId = playerLightId;
                                                    else if(darkAmount > lightAmount)
                                                        winnerId = playerDarkId;
                                                    return this;
                                                }
                                        );
                                    });
                        }));
    }

    public Single<Match> join(String accountId) {
        if(!playerDarkId.isPresent())
            playerDarkId = Optional.of(accountId);
        else if(!playerLightId.isPresent())
            playerLightId = Optional.of(accountId);
        else
            return Single.error(new WebException("Too many players", HttpResponseStatus.INTERNAL_SERVER_ERROR));
        return Single.just(this);
    }
}
