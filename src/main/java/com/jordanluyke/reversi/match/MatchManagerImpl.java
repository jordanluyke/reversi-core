package com.jordanluyke.reversi.match;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.AccountManager;
import com.jordanluyke.reversi.match.model.Match;
import com.jordanluyke.reversi.match.model.Position;
import com.jordanluyke.reversi.match.model.Side;
import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.util.WebSocketUtil;
import com.jordanluyke.reversi.web.api.ApiManager;
import com.jordanluyke.reversi.web.api.SocketManager;
import com.jordanluyke.reversi.web.api.events.OutgoingEvents;
import com.jordanluyke.reversi.web.model.WebException;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public Observable<Match> createMatch(String accountId) {
        Match match = new Match();
        match.setPlayerDarkId(Optional.of(accountId));
        matches.add(match);
        return Observable.just(match);
    }

    @Override
    public Observable<Match> getMatch(String matchId) {
        return Observable.from(matches)
                .filter(match -> match.getId().equals(matchId))
                .flatMap(match -> {
                    if(match == null)
                        return Observable.error(new WebException("Match not found", HttpResponseStatus.NOT_FOUND));
                    return Observable.just(match);
                });
    }

    @Override
    public Observable<Match> placePiece(String matchId, String accountId, Position position) {
        return getMatch(matchId)
                .flatMap(match -> {
                    Side side;
                    if(match.getPlayerDarkId().isPresent() && match.getPlayerDarkId().get().equals(accountId))
                        side = Side.DARK;
                    else if(match.getPlayerLightId().isPresent() && match.getPlayerLightId().get().equals(accountId))
                        side = Side.LIGHT;
                    else
                        return Observable.error(new WebException(HttpResponseStatus.FORBIDDEN));
                    return match.placePiece(side, position);
                })
                .doOnNext(match -> {
                    socketManager.sendUpdateEvent(OutgoingEvents.Match, matchId);

                    if(match.getCompletedAt().isPresent() && match.getPlayerDarkId().isPresent() && match.getPlayerLightId().isPresent()) {
                        Observable.from(Arrays.asList(match.getPlayerDarkId().get(), match.getPlayerLightId().get()))
                                .flatMap(id -> accountManager.getPlayerStats(id))
                                .flatMap(playerStats -> {
                                    playerStats.setMatches(playerStats.getMatches() + 1);
                                    return accountManager.updatePlayerStats(playerStats);
                                })
                                .toList()
                                .delay(5, TimeUnit.MINUTES)
                                .doOnNext(Void -> matches.removeIf(match1 -> match1.getId().equals(matchId)))
                                .subscribe(new ErrorHandlingSubscriber<>());
                    }
                });
    }

    @Override
    public Observable<Match> join(String matchId, String accountId) {
        return getMatch(matchId)
                .flatMap(match -> join(match, accountId));
    }

    @Override
    public Observable<Match> findMatch(String accountId) {
        return Observable.from(matches)
                .filter(match -> !match.isPrivate() && (!match.getPlayerLightId().isPresent() || !match.getPlayerDarkId().isPresent()))
                .first()
                .retryWhen(errors -> errors.zipWith(Observable.range(1, 15), (n, i) -> i)
                        .flatMap(retryCount -> Observable.timer(retryCount, TimeUnit.SECONDS)
                                .doOnNext(Void -> logger.info("Find match retry... {} {}", retryCount, accountId))
                        ))
                .defaultIfEmpty(null)
                .flatMap(match -> {
                    if(match == null)
                        return createMatch(accountId);
                    return join(match, accountId);
                });
    }

    private Observable<Match> join(Match match, String accountId) {
        return match.join(accountId)
                .doOnNext(Void -> socketManager.sendUpdateEvent(OutgoingEvents.Match, match.getId()));
    }
}
