package com.jordanluyke.reversi.web.api.routes;

import com.google.inject.Inject;
import com.jordanluyke.reversi.match.MatchManager;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class MatchRoutes {
    private static final Logger logger = LogManager.getLogger(MatchRoutes.class);

    public static class CreateMatch implements HttpRouteHandler {
        @Inject protected MatchManager matchManager;
        @Override
        public Observable<?> handle(Observable<HttpServerRequest> o) {
            return null;
        }
    }

    public static class GetMatch implements HttpRouteHandler {
        @Inject protected MatchManager matchManager;
        @Override
        public Observable<?> handle(Observable<HttpServerRequest> o) {
            return null;
        }
    }

    public static class Move implements HttpRouteHandler {
        @Inject protected MatchManager matchManager;
        @Override
        public Observable<?> handle(Observable<HttpServerRequest> o) {
            return null;
        }
    }
}
