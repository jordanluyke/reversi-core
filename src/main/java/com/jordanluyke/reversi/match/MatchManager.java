package com.jordanluyke.reversi.match;

import com.jordanluyke.reversi.match.model.Match;
import com.jordanluyke.reversi.match.model.Position;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface MatchManager {

    Single<Match> createMatch(String accountId);

    Single<Match> getMatch(String matchId);

    Single<Match> placePiece(String matchId, String accountId, Position position);

    Single<Match> join(String matchId, String accountId);

    Single<Match> findMatch(String accountId);
}
