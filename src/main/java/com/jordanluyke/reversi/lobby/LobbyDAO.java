package com.jordanluyke.reversi.lobby;

import com.google.inject.Inject;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.lobby.dto.UpdateLobbyRequest;
import com.jordanluyke.reversi.lobby.model.Lobby;
import com.jordanluyke.reversi.util.RandomUtil;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;

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

    public Single<Lobby> createLobby(Lobby lobby) {
        String id = RandomUtil.generateId();
        return Single.just(dbManager.getDsl().insertInto(LOBBY, LOBBY.ID, LOBBY.NAME, LOBBY.PLAYERIDDARK)
                .values(id, lobby.getName().orElse(null), lobby.getPlayerIdDark())
                .execute())
                .flatMap(Void -> getLobbyById(id));
    }

    public Single<Lobby> updateLobby(String lobbyId, UpdateLobbyRequest updateLobbyRequest) {
        return getLobbyById(lobbyId)
                .map(lobby -> dbManager.getDsl().update(LOBBY)
                            .set(LOBBY.PLAYERIDLIGHT, updateLobbyRequest.getPlayerIdLight().orElse(lobby.getPlayerIdLight().orElse(null)))
//                            .set(LOBBY.PLAYERDARKREADY, updateLobbyRequest.getPlayerDarkReady().orElse(lobby.get.getpl().orElse(null)))
//                            .set(LOBBY.PLAYERLIGHTREADY, updateLobbyRequest.getPlayerIdLight().orElse(lobby.getPlayerIdLight().orElse(null)))
                            .where(LOBBY.ID.eq(lobbyId))
                            .execute())
                .flatMap(Void -> getLobbyById(lobbyId));
    }

    public Observable<Lobby> getLobbies() {
        return Observable.fromIterable(dbManager.getDsl().selectFrom(LOBBY).fetch())
                .map(Lobby::fromRecord);
    }

    public Single<Lobby> closeLobby(String id) {
        return Single.just(dbManager.getDsl().update(LOBBY)
                        .set(LOBBY.CLOSEDAT, Instant.now())
                        .where(LOBBY.ID.eq(id))
                        .execute())
                .flatMap(Void -> getLobbyById(id));
    }
}
