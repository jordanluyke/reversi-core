package com.jordanluyke.reversi.lobby;

import com.jordanluyke.reversi.lobby.dto.UpdateLobbyRequest;
import com.jordanluyke.reversi.lobby.model.Lobby;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface LobbyManager {
    Single<Lobby> getLobbyById(String id);

    Single<Lobby> createLobby(String accountId);

    Single<Lobby> updateLobby(String lobbyId, UpdateLobbyRequest updateLobbyRequest);

    Observable<Lobby> getLobbies();

    Single<Lobby> join(String lobbyId, String accountId);

    Single<Lobby> leave(String lobbyId, String accountId);

    Single<Lobby> ready(String lobbyId, String accountId);

    Single<Lobby> cancel(String lobbyId, String accountId);
}
