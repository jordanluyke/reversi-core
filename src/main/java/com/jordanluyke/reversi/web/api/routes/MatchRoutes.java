package com.jordanluyke.reversi.web.api.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.jordanluyke.reversi.match.MatchManager;
import com.jordanluyke.reversi.match.model.Match;
import com.jordanluyke.reversi.match.model.Position;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class MatchRoutes {
    private static final Logger logger = LogManager.getLogger(MatchRoutes.class);

    public static class CreateMatch implements HttpRouteHandler {
        @Inject protected MatchManager matchManager;
        @Inject protected SessionManager sessionManager;
        @Override
        public Observable<Match> handle(Observable<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req))
                    .flatMap(session -> matchManager.createMatch(session.getOwnerId()));
        }
    }

    public static class GetMatch implements HttpRouteHandler {
        @Inject protected MatchManager matchManager;
        @Override
        public Observable<Match> handle(Observable<HttpServerRequest> o) {
            return o.flatMap(req -> {
                String matchId = req.getQueryParams().get("matchId");
                return matchManager.getMatch(matchId);
//                if(match.isPrivate())
            });
        }
    }

    public static class Move implements HttpRouteHandler {
        @Inject protected MatchManager matchManager;
        @Inject protected SessionManager sessionManager;
        @Override
        public Observable<Match> handle(Observable<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> {
                        String matchId = req.getQueryParams().get("matchId");
                        if(!req.getBody().isPresent())
                            return Observable.error(new WebException(HttpResponseStatus.BAD_REQUEST));
                        JsonNode body = req.getBody().get();
                        Optional<Integer> index = NodeUtil.getInteger(body, "index");
                        Optional<String> coordinates = NodeUtil.getText(body, "coordinates");

                        Position position;
                        if(index.isPresent())
                            position = Position.fromIndex(index.get());
                        else if(coordinates.isPresent())
                            position = Position.fromCoordinates(coordinates.get());
                        else
                            return Observable.error(new FieldRequiredException("coordinates or index"));

                        return matchManager.placePiece(matchId, session.getOwnerId(), position);
                    }));
        }
    }

    public static class Join implements HttpRouteHandler {
        @Inject protected MatchManager matchManager;
        @Inject protected SessionManager sessionManager;
        @Override
        public Observable<Match> handle(Observable<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> {
                        String matchId = req.getQueryParams().get("matchId");
                        return matchManager.join(matchId, session.getOwnerId());
                    }));
        }
    }
}
