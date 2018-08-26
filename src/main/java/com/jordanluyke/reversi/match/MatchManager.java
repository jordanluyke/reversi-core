package com.jordanluyke.reversi.match;

import com.jordanluyke.reversi.match.model.Match;
import com.jordanluyke.reversi.session.model.Session;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface MatchManager {

    Observable<Match> createMatch(String accountId);

    Observable<Match> getMatch(String matchId);

    Observable<Match> placePiece(String matchId, String accountId, String coordinates);
}
