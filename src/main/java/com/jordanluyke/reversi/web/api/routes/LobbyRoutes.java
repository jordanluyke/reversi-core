package com.jordanluyke.reversi.web.api.routes;

import com.google.inject.Inject;
import com.jordanluyke.reversi.lobby.LobbyManager;
import com.jordanluyke.reversi.lobby.dto.CreateLobbyRequest;
import com.jordanluyke.reversi.lobby.dto.GetLobbiesResponse;
import com.jordanluyke.reversi.lobby.model.Lobby;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
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
                            .flatMap(creationRequest -> {
                                if(!creationRequest.getName().isPresent())
                                    return Single.error(new FieldRequiredException("name"));
                                creationRequest.setAccountId(session.getOwnerId());
                                return lobbyManager.createLobby(creationRequest);
                            })));
        }
    }

    public static class GetLobbies implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Inject protected LobbyManager lobbyManager;
        @Override
        public Single<GetLobbiesResponse> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> lobbyManager.getLobbies()
                            .filter(lobby -> !lobby.isPrivate())
                            .toList())
                    .map(GetLobbiesResponse::new));
        }
    }
}
