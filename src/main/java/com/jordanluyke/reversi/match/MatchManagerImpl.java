package com.jordanluyke.reversi.match;

import com.jordanluyke.reversi.match.model.Match;
import com.jordanluyke.reversi.match.model.Position;
import com.jordanluyke.reversi.match.model.Side;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class MatchManagerImpl implements MatchManager {
    private static final Logger logger = LogManager.getLogger(MatchManager.class);

    private List<Match> matches = new ArrayList<>();

    @Override
    public Observable<Match> createMatch() {
        Match match = new Match();
        matches.add(match);
        return Observable.just(match);
    }

    @Override
    public Observable<Match> getMatch(String matchId) {
        return Observable.from(matches)
                .filter(match -> match.getId().equals(matchId))
                .take(1)
                .defaultIfEmpty(null)
                .flatMap(match -> {
                    if(match == null)
                        return Observable.error(new WebException("Match ID not found", HttpResponseStatus.NOT_FOUND));
                    return Observable.just(match);
                });
    }

    @Override
    public Observable<Match> placePiece(String matchId, String accountId, String coordinates) {
        return getMatch(matchId)
                .flatMap(match -> {
                    Side side;
                    if(match.getPlayerLightId().equals(accountId))
                        side = Side.LIGHT;
                    else if(match.getPlayerDarkId().equals(accountId))
                        side = Side.DARK;
                    else
                        return Observable.error(new WebException(HttpResponseStatus.FORBIDDEN));
                    return match.placePiece(side, Position.fromCoordinates(coordinates));
                });
    }
}
