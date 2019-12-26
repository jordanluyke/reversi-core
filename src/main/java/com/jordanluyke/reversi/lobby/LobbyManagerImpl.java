package com.jordanluyke.reversi.lobby;

import com.google.inject.Inject;
import com.jordanluyke.reversi.lobby.dto.CreateLobbyRequest;
import com.jordanluyke.reversi.lobby.dto.UpdateLobbyRequest;
import com.jordanluyke.reversi.lobby.model.Lobby;
import com.jordanluyke.reversi.web.api.SocketManager;
import com.jordanluyke.reversi.web.api.model.SocketChannel;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class LobbyManagerImpl implements LobbyManager {
    private static final Logger logger = LogManager.getLogger(LobbyManager.class);

    private LobbyDAO lobbyDAO;
    private SocketManager socketManager;

    @Override
    public Single<Lobby> getLobbyById(String lobbyId) {
        return lobbyDAO.getLobbyById(lobbyId);
    }

    @Override
    public Single<Lobby> createLobby(CreateLobbyRequest createLobbyRequest) {
        return lobbyDAO.createLobby(createLobbyRequest);
    }

    @Override
    public Single<Lobby> updateLobby(String lobbyId, UpdateLobbyRequest updateLobbyRequest) {
        return lobbyDAO.updateLobby(lobbyId, updateLobbyRequest)
                .doOnSuccess(Void -> socketManager.send(SocketChannel.Lobby, lobbyId));
    }

    @Override
    public Observable<Lobby> getLobbies() {
        return lobbyDAO.getLobbies();
    }

    @Override
    public Single<Lobby> closeLobby(String lobbyId) {
        return lobbyDAO.closeLobby(lobbyId);
    }

    @Override
    public Single<Lobby> ready(String lobbyId, String playerId) {
        return getLobbyById(lobbyId)
                .flatMap(lobby -> {
                    UpdateLobbyRequest req = new UpdateLobbyRequest();
                    if(lobby.getPlayerIdDark().equals(playerId))
                        req.setPlayerDarkReady(Optional.of(true));
                    else if(lobby.getPlayerIdLight().isPresent() && lobby.getPlayerIdLight().get().equals(playerId))
                        req.setPlayerLightReady(Optional.of(true));
                    else
                        return Single.error(new WebException(HttpResponseStatus.FORBIDDEN));
                    return updateLobby(lobbyId, req);
                });
    }
}
