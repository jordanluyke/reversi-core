package com.jordanluyke.reversi.match;

import com.jordanluyke.reversi.match.model.Match;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface MatchManager {

    Observable<Match> createMatch();

    Observable<Match> getMatch(String matchId);

    Observable<Match> placePiece(String matchId, String accountId, String coordinates);
}
