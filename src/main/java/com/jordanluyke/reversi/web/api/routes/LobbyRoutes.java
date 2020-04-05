package com.jordanluyke.reversi.web.api.routes;

import com.google.inject.Inject;
import com.jordanluyke.reversi.lobby.LobbyManager;
import com.jordanluyke.reversi.lobby.model.Lobby;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.api.model.PagingResponse;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
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
                    .flatMap(session -> lobbyManager.createLobby(session.getOwnerId())));
        }
    }

    public static class GetLobbies implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Inject protected LobbyManager lobbyManager;
        @Override
        public Single<PagingResponse<Lobby>> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> lobbyManager.getLobbies()
                            .filter(lobby -> !lobby.isPrivate() && !lobby.getClosedAt().isPresent() && !lobby.getStartingAt().isPresent() && (!lobby.getPlayerIdLight().isPresent() || (lobby.getPlayerIdDark().equals(session.getOwnerId()) || (lobby.getPlayerIdLight().isPresent() && lobby.getPlayerIdLight().get().equals(session.getOwnerId())))))
                            .toList())
                    .map(lobbies -> new PagingResponse<>(lobbies, 0, lobbies.size())));
        }
    }

    public static class Join implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Inject protected LobbyManager lobbyManager;
        @Override
        public Single<Lobby> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> {
                        String lobbyId = req.getQueryParams().get("lobbyId");
                        return lobbyManager.join(lobbyId, session.getOwnerId());
                    }));
        }
    }

    public static class Leave implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Inject protected LobbyManager lobbyManager;
        @Override
        public Single<Lobby> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> {
                        String lobbyId = req.getQueryParams().get("lobbyId");
                        return lobbyManager.leave(lobbyId, session.getOwnerId());
                    }));
        }
    }

    public static class Ready implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Inject protected LobbyManager lobbyManager;
        @Override
        public Single<Lobby> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> {
                        String lobbyId = req.getQueryParams().get("lobbyId");
                        return lobbyManager.ready(lobbyId, session.getOwnerId());
                    }));
        }
    }

    public static class Cancel implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Inject protected LobbyManager lobbyManager;
        @Override
        public Single<Lobby> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> {
                        String lobbyId = req.getQueryParams().get("lobbyId");
                        return lobbyManager.cancel(lobbyId, session.getOwnerId());
                    }));
        }
    }
}
