package com.jordanluyke.reversi.web.api.routes;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.AccountManager;
import com.jordanluyke.reversi.lobby.LobbyManager;
import com.jordanluyke.reversi.lobby.dto.CreateLobbyRequest;
import com.jordanluyke.reversi.lobby.model.Lobby;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.api.model.PagingResponse;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class LobbyRoutes {
    private static final Logger logger = LogManager.getLogger(LobbyRoutes.class);

    public static class GetLobby implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Inject protected LobbyManager lobbyManager;
        @Override
        public Single<Lobby> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> {
                        String id = req.getQueryParams().get("lobbyId");
                        if(!session.getOwnerId().equals(id))
                            return Single.error(new WebException(HttpResponseStatus.FORBIDDEN));
                        return lobbyManager.getLobbyById(id);
                    }));
        }
    }

    public static class CreateLobby implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Inject protected LobbyManager lobbyManager;
        @Override
        public Single<Lobby> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> NodeUtil.parseNodeInto(CreateLobbyRequest.class, req.getBody())
                            .flatMap(createLobbyRequest -> {
//                                if(!createLobbyRequest.getName().isPresent())
//                                    return Single.error(new FieldRequiredException("name"));
                                createLobbyRequest.setAccountId(session.getOwnerId());
                                return lobbyManager.createLobby(createLobbyRequest);
                            })));
        }
    }

    public static class GetLobbies implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Inject protected LobbyManager lobbyManager;
        @Override
        public Single<PagingResponse> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> lobbyManager.getLobbies()
                            .filter(lobby -> !lobby.isPrivate())
                            .toList())
                    .map(lobbies -> new PagingResponse<>(lobbies, 0, lobbies.size())));
        }
    }

    public static class Close implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Inject protected LobbyManager lobbyManager;
        @Override
        public Single<Lobby> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> {
                        String id = req.getQueryParams().get("lobbyId");
                        return lobbyManager.getLobbyById(id)
                            .flatMap(lobby -> {
                                if(!lobby.getPlayerIdDark().equals(session.getOwnerId()))
                                    return Single.error(new WebException(HttpResponseStatus.FORBIDDEN));
                                return lobbyManager.closeLobby(id);
                            });
                    }));
        }
    }

    public static class Ready implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Inject protected LobbyManager lobbyManager;
        @Inject protected AccountManager accountManager;
        @Override
        public Single<Lobby> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> {
                        String lobbyId = req.getQueryParams().get("lobbyId");
                        return lobbyManager.ready(lobbyId, session.getOwnerId());
//                        return lobbyManager.getLobbyById(lobbyId)
//                                .flatMap(lobby -> {
//                                    if(!session.getId().equals(lobby.getPlayerDark()) && !(lobby.getPlayerLight().isPresent() && session.getId().equals(lobby.getPlayerLight().get())))
//                                        return Single.error(new WebException(HttpResponseStatus.FORBIDDEN));
//                                });
                    }));
        }
    }
}
