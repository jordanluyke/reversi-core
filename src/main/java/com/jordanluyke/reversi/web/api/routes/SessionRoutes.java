package com.jordanluyke.reversi.web.api.routes;

import com.google.inject.Inject;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.session.dto.SessionResponse;
import com.jordanluyke.reversi.session.model.Session;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class SessionRoutes {
    private static final Logger logger = LogManager.getLogger(SessionRoutes.class);

    public static class CreateSession implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Override
        public Observable<SessionResponse> handle(Observable<HttpServerRequest> o) {
            return o.flatMap(req -> NodeUtil.parseObjectNodeInto(req.getBody(), SessionCreationRequest.class))
                    .doOnNext(req -> logger.info("{}", req))
                    .flatMap(sessionManager::createSession)
                    .doOnNext(session -> logger.info("{}", session))
                    .map(session -> new SessionResponse(session.getOwnerId(), session.getId(), session.getExpiresAt()));
        }
    }
}
