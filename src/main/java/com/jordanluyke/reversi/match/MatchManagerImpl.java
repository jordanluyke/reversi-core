package com.jordanluyke.reversi.match;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.AccountManager;
import com.jordanluyke.reversi.match.model.Match;
import com.jordanluyke.reversi.match.model.Position;
import com.jordanluyke.reversi.match.model.Side;
import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.util.WebSocketUtil;
import com.jordanluyke.reversi.web.WebManager;
import com.jordanluyke.reversi.web.api.events.OutgoingEvents;
import com.jordanluyke.reversi.web.model.WebException;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import com.jordanluyke.reversi.web.netty.AggregateWebSocketChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class MatchManagerImpl implements MatchManager {
    private static final Logger logger = LogManager.getLogger(MatchManager.class);

    private AccountManager accountManager;
    private WebManager webManager;

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
                    List<AggregateWebSocketChannelHandlerContext> connections = webManager.getConnections(match.getPlayerDarkId().get());
                    connections.addAll(webManager.getConnections(match.getPlayerLightId().get()));
                    connections.forEach(connection -> WebSocketUtil.writeResponse(connection.getCtx(), new WebSocketServerResponse(OutgoingEvents.Match)));

                    if(match.getCompletedAt().isPresent() && match.getPlayerDarkId().isPresent() && match.getPlayerLightId().isPresent()) {
                        Observable.from(Arrays.asList(match.getPlayerDarkId().get(), match.getPlayerLightId().get()))
                                .flatMap(id -> accountManager.getPlayerStats(id))
                                .flatMap(playerStats -> {
                                    playerStats.setMatches(playerStats.getMatches() + 1);
                                    return accountManager.updatePlayerStats(playerStats);
                                })
                                .subscribe(new ErrorHandlingSubscriber<>());
                        // then remove from list
                    }
                });
    }

    @Override
    public Observable<Match> join(String matchId, String accountId) {
        return getMatch(matchId)
                .flatMap(match -> match.join(accountId));
    }
}
