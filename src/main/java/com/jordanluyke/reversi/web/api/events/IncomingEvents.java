package com.jordanluyke.reversi.web.api.events;

import com.google.inject.Inject;
import com.jordanluyke.reversi.match.MatchManager;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.WebSocketEventHandler;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import io.reactivex.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class IncomingEvents {
    private static final Logger logger = LogManager.getLogger(IncomingEvents.class);

    public static class Receipt implements WebSocketEventHandler {
        @Override
        public Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMap(req -> {
                Optional<String> id = NodeUtil.get("id", req.getBody());
                if(!id.isPresent())
                    return Single.error(new FieldRequiredException("id"));
                req.getConnection().unsubscribeMessageReceipt(id.get());

                return Single.just(WebSocketServerResponse.builder()
                                .event(SocketEvent.Receipt)
                                .body(NodeUtil.mapper.createObjectNode().put("success", true))
                                .build());
            });
        }
    }

    public static class Account implements WebSocketEventHandler {
        @Override
        public Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMapCompletable(req -> req.getConnection().handleSubscriptionRequest(req, true))
                    .toSingle(() -> WebSocketServerResponse.builder()
                                .event(SocketEvent.Account)
                                .body(NodeUtil.mapper.createObjectNode().put("success", true))
                                .build());
        }
    }

    public static class Match implements WebSocketEventHandler {
        @Override
        public Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMapCompletable(req -> req.getConnection().handleSubscriptionRequest(req, true))
                    .toSingle(() -> WebSocketServerResponse.builder()
                                .event(SocketEvent.Match)
                                .body(NodeUtil.mapper.createObjectNode().put("success", true))
                                .build());
        }
    }

    public static class KeepAlive implements WebSocketEventHandler {
        @Override
        public Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.map(req -> WebSocketServerResponse.builder()
                    .event(SocketEvent.KeepAlive)
                    .body(NodeUtil.mapper.createObjectNode()
                            .put("time", Instant.now().toEpochMilli()))
                    .build());
        }
    }

    public static class FindMatch implements WebSocketEventHandler {
        @Inject protected MatchManager matchManager;
        @Inject protected SessionManager sessionManager;
        @Override
        public Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMapCompletable(session -> {
                        Single<WebSocketServerResponse> findMatch = matchManager.findMatch(session.getOwnerId())
                                .map(match -> WebSocketServerResponse.builder()
                                        .event(SocketEvent.FindMatch)
                                        .body(NodeUtil.mapper.createObjectNode().put("matchId", match.getId()))
                                        .build());
                        return req.getConnection().handleSubscriptionRequest(req, true, Optional.of(findMatch));
                    })
                    .toSingle(() -> WebSocketServerResponse.builder()
                                        .event(SocketEvent.FindMatch)
                                        .body(NodeUtil.mapper.createObjectNode().put("success", true))
                                        .build()));
        }
    }
}
