package com.jordanluyke.reversi.lobby;

import com.google.inject.Inject;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.lobby.dto.CreateLobbyRequest;
import com.jordanluyke.reversi.lobby.model.Lobby;
import com.jordanluyke.reversi.util.RandomUtil;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import static org.jooq.sources.Tables.LOBBY;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class LobbyDAO {
    private static final Logger logger = LogManager.getLogger(LobbyDAO.class);

    private DbManager dbManager;

    public Single<Lobby> getLobbyById(String accountId) {
        return Single.just(dbManager.getDsl().selectFrom(LOBBY)
                .where(LOBBY.ID.eq(accountId))
                .fetchAny())
                .map(Lobby::fromRecord);
    }

    public Single<Lobby> createLobby(CreateLobbyRequest createLobbyRequest) {
        String id = RandomUtil.generateId();
        return Single.just(dbManager.getDsl().insertInto(LOBBY, LOBBY.ID, LOBBY.NAME, LOBBY.PLAYER1)
                .values(id, createLobbyRequest.getName().orElse("Lobby"), createLobbyRequest.getAccountId())
                .execute())
                .flatMap(Void -> getLobbyById(id));
    }

    public Observable<Lobby> getLobbies() {
        return Observable.fromIterable(dbManager.getDsl().selectFrom(LOBBY).fetch())
                .map(Lobby::fromRecord);
    }
}
