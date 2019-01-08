package com.jordanluyke.reversi.match;

import com.jordanluyke.reversi.match.model.Match;
import com.jordanluyke.reversi.match.model.Position;
import com.jordanluyke.reversi.session.model.Session;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface MatchManager {

    Observable<Match> createMatch(String accountId);

    Observable<Match> getMatch(String matchId);

    Observable<Match> placePiece(String matchId, String accountId, Position position);

    Observable<Match> join(String matchId, String accountId);

    Observable<Match> findMatch(String accountId);
}
