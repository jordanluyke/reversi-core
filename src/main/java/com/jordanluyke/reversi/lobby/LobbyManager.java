package com.jordanluyke.reversi.lobby;

import com.jordanluyke.reversi.lobby.dto.CreateLobbyRequest;
import com.jordanluyke.reversi.lobby.model.Lobby;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface LobbyManager {
    Single<Lobby> getLobbyById(String id);

    Single<Lobby> createLobby(CreateLobbyRequest createLobbyRequest);

    Observable<Lobby> getLobbies();
}
