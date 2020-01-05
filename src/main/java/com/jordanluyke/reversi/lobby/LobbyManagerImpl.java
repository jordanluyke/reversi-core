package com.jordanluyke.reversi.lobby;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.AccountManager;
import com.jordanluyke.reversi.lobby.dto.UpdateLobbyRequest;
import com.jordanluyke.reversi.lobby.model.Lobby;
import com.jordanluyke.reversi.match.MatchManager;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.api.SocketManager;
import com.jordanluyke.reversi.web.api.model.SocketChannel;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private AccountManager accountManager;
    private MatchManager matchManager;

    private final List<Lobby> lobbies = new ArrayList<>();

    @Override
    public Single<Lobby> getLobbyById(String lobbyId) {
//        return lobbyDAO.getLobbyById(lobbyId);
        return Observable.fromIterable(lobbies)
                .filter(lobby -> lobby.getId().equals(lobbyId))
                .firstOrError()
                .onErrorResumeNext(e -> Single.error(new WebException(HttpResponseStatus.NOT_FOUND, "Lobby not found")));
    }

    @Override
    public Single<Lobby> createLobby(String accountId) {
//        return lobbyDAO.createLobby(createLobbyRequest);
        return accountManager.getAccountById(accountId)
                .flatMap(account -> {
                    Lobby lobby = new Lobby();
                    lobby.setPlayerIdDark(accountId);
                    lobby.setName(Optional.of(account.getAccount().getName() + "'s game"));
                    lobby.setId(RandomUtil.generateId());
                    lobby.setCreatedAt(Instant.now());
                    lobby.setUpdatedAt(lobby.getCreatedAt());
                    lobbies.add(lobby);
                    return Single.just(lobby);
                });
    }

    @Override
    public Single<Lobby> updateLobby(String lobbyId, UpdateLobbyRequest updateLobbyRequest) {
//        return lobbyDAO.updateLobby(lobbyId, updateLobbyRequest)
//                .doOnSuccess(Void -> socketManager.send(SocketChannel.Lobby, lobbyId));
        return Single.defer(() -> {
            for(Lobby lobby : lobbies) {
                if(lobby.getId().equals(lobbyId)) {
                    updateLobbyRequest.getPlayerIdLight().ifPresent(id -> lobby.setPlayerIdLight(Optional.of(id)));
                    updateLobbyRequest.getPlayerDarkReady().ifPresent(lobby::setPlayerReadyDark);
                    updateLobbyRequest.getPlayerLightReady().ifPresent(lobby::setPlayerReadyLight);
                    return updateLobby(lobby);
                }
            }
            return Single.error(new WebException(HttpResponseStatus.NOT_FOUND));
        });
    }

    @Override
    public Observable<Lobby> getLobbies() {
//        return lobbyDAO.getLobbies();
        return Observable.fromIterable(lobbies)
                .filter(lobby -> !lobby.getClosedAt().isPresent());
    }

    @Override
    public Single<Lobby> join(String lobbyId, String accountId) {
        return getLobbyById(lobbyId)
                .flatMap(lobby -> {
                    if(!lobby.getPlayerIdDark().equals(accountId) && !lobby.getPlayerIdLight().isPresent()) {
                        lobby.setPlayerIdLight(Optional.of(accountId));

                        for(Lobby l : lobbies) {
                            if(!lobby.getId().equals(l.getId()) && (l.getPlayerIdDark().equals(accountId) || (l.getPlayerIdLight().isPresent() && l.getPlayerIdLight().get().equals(accountId)))) {
                                return leave(l.getId(), accountId)
                                        .flatMap(this::updateLobby)
                                        .flatMap(Void -> updateLobby(lobby));
                            }
                        }

                        return updateLobby(lobby);
                    }
                    return Single.error(new WebException(HttpResponseStatus.FORBIDDEN));
                });
    }

    @Override
    public Single<Lobby> leave(String lobbyId, String accountId) {
        return getLobbyById(lobbyId)
                .flatMap(lobby -> {
                    if(lobby.getPlayerIdDark().equals(accountId) || (lobby.getPlayerIdLight().isPresent() && lobby.getPlayerIdLight().get().equals(accountId))) {
                        if(lobby.getPlayerIdDark().equals(accountId)) {
                            lobby.setClosedAt(Optional.of(Instant.now()));
                        } else {
                            lobby.setPlayerIdLight(Optional.empty());
                            lobby.setPlayerReadyLight(false);
                        }
                        return updateLobby(lobby);
                    }
                    return Single.error(new WebException(HttpResponseStatus.FORBIDDEN));
                });
    }

    @Override
    public Single<Lobby> ready(String lobbyId, String accountId) {
        return getLobbyById(lobbyId)
                .flatMap(lobby -> {
                    if(lobby.getPlayerIdDark().equals(accountId))
                        lobby.setPlayerReadyDark(true);
                    else if(lobby.getPlayerIdLight().isPresent() && lobby.getPlayerIdLight().get().equals(accountId))
                        lobby.setPlayerReadyLight(true);
                    else
                        return Single.error(new WebException(HttpResponseStatus.FORBIDDEN));
                    if(lobby.getPlayerIdDark() != null && lobby.isPlayerReadyDark() &&
                            lobby.getPlayerIdLight().isPresent() && lobby.isPlayerReadyLight()) {
                        return matchManager.createMatch(lobby.getPlayerIdDark(), lobby.getPlayerIdLight().get())
                                .flatMap(match -> {
                                    lobby.setMatchId(Optional.of(match.getId()));
                                    lobby.setStartingAt(Optional.of(Instant.now()));
                                    return Single.just(lobby);
                                });
                    }
                    return Single.just(lobby);
                })
                .flatMap(this::updateLobby);
    }

    @Override
    public Single<Lobby> cancel(String lobbyId, String accountId) {
        return getLobbyById(lobbyId)
                .flatMap(lobby -> {
                    if(lobby.getPlayerIdDark().equals(accountId))
                        lobby.setPlayerReadyDark(false);
                    else if(lobby.getPlayerIdLight().isPresent() && lobby.getPlayerIdLight().get().equals(accountId))
                        lobby.setPlayerReadyLight(false);
                    else
                        return Single.error(new WebException(HttpResponseStatus.FORBIDDEN));
                    return updateLobby(lobby);
                });
    }

    private Single<Lobby> updateLobby(Lobby lobby) {
        return Single.defer(() -> {
            lobby.setUpdatedAt(Instant.now());
            return Single.just(lobby);
        })
                .doOnSuccess(Void -> socketManager.send(SocketChannel.Lobby, lobby.getId()));
    }
}
