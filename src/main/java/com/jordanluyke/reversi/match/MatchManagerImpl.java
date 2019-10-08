package com.jordanluyke.reversi.match;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.AccountManager;
import com.jordanluyke.reversi.match.model.Match;
import com.jordanluyke.reversi.match.model.Position;
import com.jordanluyke.reversi.match.model.Side;
import com.jordanluyke.reversi.util.ErrorHandlingSingleObserver;
import com.jordanluyke.reversi.web.api.SocketManager;
import com.jordanluyke.reversi.web.api.model.SocketEvent;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class MatchManagerImpl implements MatchManager {
    private static final Logger logger = LogManager.getLogger(MatchManager.class);

    private AccountManager accountManager;
    private SocketManager socketManager;

    private final List<Match> matches = new ArrayList<>();

    @Override
    public Single<Match> createMatch(String accountId) {
        Match match = new Match();
        match.setPlayerDarkId(Optional.of(accountId));
        matches.add(match);
        return Single.just(match);
    }

    @Override
    public Single<Match> getMatch(String matchId) {
        return Observable.fromIterable(matches)
                .filter(match -> match.getId().equals(matchId))
                .singleOrError()
                .onErrorResumeNext(e -> Single.error(new WebException(HttpResponseStatus.NOT_FOUND, "Match not found")));
    }

    @Override
    public Single<Match> placePiece(String matchId, String accountId, Position position) {
        return getMatch(matchId)
                .flatMap(match -> {
                    Side side;
                    if(match.getPlayerDarkId().isPresent() && match.getPlayerDarkId().get().equals(accountId))
                        side = Side.DARK;
                    else if(match.getPlayerLightId().isPresent() && match.getPlayerLightId().get().equals(accountId))
                        side = Side.LIGHT;
                    else
                        return Single.error(new WebException(HttpResponseStatus.FORBIDDEN));
                    return match.placePiece(side, position);
                })
                .doOnSuccess(match -> {
                    socketManager.send(SocketEvent.Match, matchId);

                    if(match.getCompletedAt().isPresent() && match.getPlayerDarkId().isPresent() && match.getPlayerLightId().isPresent()) {
                        Observable.fromIterable(Arrays.asList(match.getPlayerDarkId().get(), match.getPlayerLightId().get()))
                                .flatMap(id -> accountManager.getPlayerStats(id).toObservable())
                                .flatMap(playerStats -> {
                                    playerStats.setMatches(playerStats.getMatches() + 1);
                                    return accountManager.updatePlayerStats(playerStats).toObservable();
                                })
                                .toList()
                                .delay(5, TimeUnit.MINUTES)
                                .doOnSuccess(Void -> matches.removeIf(match1 -> match1.getId().equals(matchId)))
                                .subscribe(new ErrorHandlingSingleObserver<>());
                    }
                });
    }

    @Override
    public Single<Match> join(String matchId, String accountId) {
        return getMatch(matchId)
                .flatMap(match -> join(match, accountId));
    }

    @Override
    public Single<Match> findMatch(String accountId) {
        return Observable.fromIterable(matches)
                .filter(match -> !match.isPrivate()
                        && (!match.getPlayerLightId().isPresent() || !match.getPlayerDarkId().isPresent())
                        && (!match.getPlayerLightId().isPresent() || !match.getPlayerLightId().get().equals(accountId))
                        && (!match.getPlayerDarkId().isPresent() || !match.getPlayerDarkId().get().equals(accountId)))
                .singleOrError()
                .retryWhen(errors -> errors.zipWith(Flowable.range(1, 15), (n, i) -> i)
                        .flatMap(retryCount -> Flowable.timer(retryCount, TimeUnit.SECONDS)
                                .doOnNext(Void -> logger.info("Find match retry... {} {}", retryCount, accountId))))
                .onErrorResumeNext(e -> createMatch(accountId))
                .flatMap(match -> join(match, accountId));
    }

    private Single<Match> join(Match match, String accountId) {
        return match.join(accountId)
                .doOnSuccess(Void -> socketManager.send(SocketEvent.Match, match.getId()));
    }
}
