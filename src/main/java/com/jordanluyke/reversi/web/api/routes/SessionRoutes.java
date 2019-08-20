package com.jordanluyke.reversi.web.api.routes;

import com.google.inject.Inject;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.session.model.Session;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import io.reactivex.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class SessionRoutes {
    private static final Logger logger = LogManager.getLogger(SessionRoutes.class);

    public static class CreateSession implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Override
        public Single<Session> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> NodeUtil.parseNodeInto(SessionCreationRequest.class, req.getBody()))
                    .flatMap(sessionManager::createSession);
        }
    }

    public static class DeleteSession implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Override
        public Single<Session> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.logout(req.getQueryParams().get("sessionId")));
        }
    }
}
