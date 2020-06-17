package com.jordanluyke.reversi.lobby;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.AccountManager;
import com.jordanluyke.reversi.lobby.dto.UpdateLobbyRequest;
import com.jordanluyke.reversi.lobby.model.Lobby;
import com.jordanluyke.reversi.match.MatchManager;
import com.jordanluyke.reversi.util.*;
import com.jordanluyke.reversi.web.api.SocketManager;
import com.jordanluyke.reversi.web.api.model.PusherChannel;
import com.jordanluyke.reversi.web.api.model.UserStatus;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.*;

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
    private final Map<String, List<Disposable>> lobbySubscriptions = new HashMap<>();

    @Override
    public Single<Lobby> getLobbyById(String lobbyId) {
//        return lobbyDAO.getLobbyById(lobbyId);
        return Observable.fromIterable(lobbies)
                .filter(lobby -> lobby.getId().equals(lobbyId) && !lobby.getClosedAt().isPresent())
                .firstOrError()
                .onErrorResumeNext(e -> Single.error(new WebException(HttpResponseStatus.NOT_FOUND, "Lobby not found")));
    }

    @Override
    public Single<Lobby> createLobby(String accountId) {
//        return lobbyDAO.createLobby(createLobbyRequest);
        return accountManager.getAccountById(accountId)
                .map(account -> {
                    Lobby lobby = new Lobby();
                    lobby.setPlayerIdDark(accountId);
                    lobby.setName(Optional.of(account.getAccount().getName() + "'s game"));
                    lobby.setId(RandomUtil.generateId());
                    lobby.setCreatedAt(Instant.now());
                    lobby.setUpdatedAt(lobby.getCreatedAt());
                    lobbies.add(lobby);
                    return lobby;
                })
                .doOnSuccess(lobby -> {
                    socketManager.getUserStatus(accountId).ifPresent(userStatus -> {
                        Disposable offline = userStatus.getOnChange()
                                .filter(status -> status == UserStatus.Status.OFFLINE)
                                .flatMap(status -> leave(lobby.getId(), accountId).toObservable())
                                .subscribe(o -> {}, e -> logger.error("Error: {}", e.getMessage()));

                        lobbySubscriptions.put(lobby.getId(), Arrays.asList(offline));
                    });

                    socketManager.send(PusherChannel.Lobbies);
                });
    }

    @Override
    public Single<Lobby> updateLobby(String lobbyId, UpdateLobbyRequest updateLobbyRequest) {
//        return lobbyDAO.updateLobby(lobbyId, updateLobbyRequest)
//                .doOnSuccess(Void -> socketManager.send(SocketChannel.Lobby, lobbyId));
        return Observable.fromIterable(lobbies)
                .filter(lobby -> lobby.getId().equals(lobbyId))
                .firstOrError()
                .onErrorResumeNext(e -> Single.error(new WebException(HttpResponseStatus.NOT_FOUND)))
                .flatMap(lobby -> {
                    updateLobbyRequest.getPlayerIdLight().ifPresent(id -> lobby.setPlayerIdLight(Optional.of(id)));
                    updateLobbyRequest.getPlayerDarkReady().ifPresent(lobby::setPlayerReadyDark);
                    updateLobbyRequest.getPlayerLightReady().ifPresent(lobby::setPlayerReadyLight);
                    return updateLobby(lobby);
                });
    }

    @Override
    public Observable<Lobby> getLobbies() {
//        return lobbyDAO.getLobbies();
        return Observable.fromIterable(lobbies);
    }

    @Override
    public Single<Lobby> join(String lobbyId, String accountId) {
        return getLobbyById(lobbyId)
                .flatMap(lobby -> {
                    if(!lobby.getPlayerIdDark().equals(accountId) && !lobby.getPlayerIdLight().isPresent()) {
                        lobby.setPlayerIdLight(Optional.of(accountId));
                        return updateLobby(lobby);
                    }
                    return Single.error(new WebException(HttpResponseStatus.FORBIDDEN));
                })
                .doOnSuccess(lobby -> {
                    Observable.fromIterable(lobbies)
                            .filter(l -> !lobby.getId().equals(l.getId()) && (l.getPlayerIdDark().equals(accountId) || (l.getPlayerIdLight().isPresent() && l.getPlayerIdLight().get().equals(accountId))))
                            .flatMapSingle(l -> leave(l.getId(), accountId)
                                    .flatMap(this::updateLobby))
                            .subscribe(new ErrorHandlingObserver<>());

                    socketManager.send(PusherChannel.Lobbies);
                });
    }

    @Override
    public Single<Lobby> leave(String lobbyId, String accountId) {
        return getLobbyById(lobbyId)
                .flatMap(lobby -> {
                    if(!lobby.getMatchId().isPresent() && (lobby.getPlayerIdDark().equals(accountId) || (lobby.getPlayerIdLight().isPresent() && lobby.getPlayerIdLight().get().equals(accountId)))) {
                        if(lobby.getPlayerIdDark().equals(accountId)) {
                            lobby.setClosedAt(Optional.of(Instant.now()));
                        } else {
                            lobby.setPlayerIdLight(Optional.empty());
                            lobby.setPlayerReadyLight(false);
                        }
                        lobby.setStartingAt(Optional.empty());
                        return updateLobby(lobby);
                    }
                    return Single.error(new WebException(HttpResponseStatus.FORBIDDEN));
                })
                .doOnSuccess(lobby -> {
                    socketManager.send(PusherChannel.Lobbies);
                    removeSubscriptions(lobbyId);
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
                                .map(match -> {
                                    lobby.setMatchId(Optional.of(match.getId()));
                                    lobby.setStartingAt(Optional.of(Instant.now()));
                                    return lobby;
                                })
                                .doOnSuccess(Void -> removeSubscriptions(lobbyId));
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
                .doOnSuccess(Void -> socketManager.send(PusherChannel.Lobby, lobby.getId()));
    }

    private void removeSubscriptions(String lobbyId) {

        List<Disposable> disposables = lobbySubscriptions.get(lobbyId);
        if(disposables != null)
            disposables.forEach(Disposable::dispose);
        lobbySubscriptions.remove(lobbyId);
    }
}
