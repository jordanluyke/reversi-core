package com.jordanluyke.reversi.lobby;

import com.google.inject.Inject;
import com.jordanluyke.reversi.lobby.dto.CreateLobbyRequest;
import com.jordanluyke.reversi.lobby.model.Lobby;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class LobbyManagerImpl implements LobbyManager {
    private static final Logger logger = LogManager.getLogger(LobbyManager.class);

    private LobbyDAO lobbyDAO;

    @Override
    public Single<Lobby> getLobbyById(String id) {
        return lobbyDAO.getLobbyById(id);
    }

    @Override
    public Single<Lobby> createLobby(CreateLobbyRequest createLobbyRequest) {
        return lobbyDAO.createLobby(createLobbyRequest);
    }

    @Override
    public Observable<Lobby> getLobbies() {
        return lobbyDAO.getLobbies();
    }
}
