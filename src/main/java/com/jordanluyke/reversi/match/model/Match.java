package com.jordanluyke.reversi.match.model;

import com.jordanluyke.reversi.util.ErrorHandlingObserver;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    @Builder.Default private Optional<Instant> lastMoveAt = Optional.empty();

    public Single<Match> placePiece(Side side, Position position) {
        return Single.defer(() -> {
            if(completedAt.isPresent())
                return Single.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Game completed"));
            if(side != turn)
                return Single.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Not your turn"));
            if(!playerLightId.isPresent() || !playerDarkId.isPresent())
                return Single.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Two players required"));
            try {
                board.placePiece(side, position);
                if(board.isComplete()) {
                    completedAt = Optional.of(Instant.now());
                    int darkAmount = board.getAmount(Side.DARK);
                    int lightAmount = board.getAmount(Side.LIGHT);
                    if(lightAmount > darkAmount)
                        winnerId = playerLightId;
                    else if(darkAmount > lightAmount)
                        winnerId = playerDarkId;
                } else if(board.canPlacePiece(side.getOpposite())) {
                    turn = turn.getOpposite();
                }
                return Single.just(this);
            } catch(IllegalMoveException e) {
                return Single.error(e);
            }
        })
                .doOnSuccess(Void -> {
                    lastMoveAt = Optional.of(Instant.now());
                    Observable.timer(5, TimeUnit.MINUTES)
                            .doAfterNext(Void1 -> {
                                if(lastMoveAt.isPresent() && lastMoveAt.get().isBefore(Instant.now())) {
                                    completedAt = Optional.of(Instant.now());
                                    winnerId = turn == Side.LIGHT ? playerDarkId : playerLightId;
                                }
                            })
                            .subscribe(new ErrorHandlingObserver<>());
                });
    }
}
