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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class LobbyManagerImpl implements LobbyManager {
    private static final Logger logger = LogManager.getLogger(LobbyManager.class);

    private LobbyDAO lobbyDAO;
    private SocketManager socketManager;

    private final List<Lobby> lobbies = new ArrayList<>();

    @Override
    public Single<Lobby> getLobbyById(String lobbyId) {
//        return lobbyDAO.getLobbyById(lobbyId);
        return Observable.fromIterable(lobbies)
                .filter(lobby -> lobby.getId().equals(lobbyId))
                .firstOrError();
    }

    @Override
    public Single<Lobby> createLobby(CreateLobbyRequest createLobbyRequest) {
//        return lobbyDAO.createLobby(createLobbyRequest);
        Lobby lobby = new Lobby();
        lobby.setName(createLobbyRequest.getName());
        lobby.setPlayerIdDark(createLobbyRequest.getAccountId());
        lobbies.add(lobby);
        return Single.just(lobby);
    }

    @Override
    public Single<Lobby> updateLobby(String lobbyId, UpdateLobbyRequest updateLobbyRequest) {
//        return lobbyDAO.updateLobby(lobbyId, updateLobbyRequest)
//                .doOnSuccess(Void -> socketManager.send(SocketChannel.Lobby, lobbyId));
        for(Lobby lobby : lobbies) {
            if(lobby.getId().equals(lobbyId)) {
                updateLobbyRequest.getPlayerIdLight().ifPresent(id -> lobby.setPlayerIdLight(Optional.of(id)));
                updateLobbyRequest.getPlayerDarkReady().ifPresent(lobby::setPlayerDarkReady);
                updateLobbyRequest.getPlayerLightReady().ifPresent(lobby::setPlayerLightReady);
                return Single.just(lobby);
            }
        }
        return Single.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    public Observable<Lobby> getLobbies() {
//        return lobbyDAO.getLobbies();
        return Observable.fromIterable(lobbies);
    }

    @Override
    public Single<Lobby> closeLobby(String lobbyId) {
//        return lobbyDAO.closeLobby(lobbyId);
        for(int i = 0; i < lobbies.size(); i++) {
            if(lobbies.get(i).getId().equals(lobbyId)) {
                Lobby lobby = lobbies.get(i);
                lobbies.remove(i);
                lobby.setClosedAt(Optional.of(Instant.now()));
                return Single.just(lobbies.get(i));
            }
        }
        return Single.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR));
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
