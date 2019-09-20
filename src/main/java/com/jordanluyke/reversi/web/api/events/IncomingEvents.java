package com.jordanluyke.reversi.web.api.events;

import com.google.inject.Inject;
import com.jordanluyke.reversi.match.MatchManager;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.WebSocketEventHandler;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class IncomingEvents {
    private static final Logger logger = LogManager.getLogger(IncomingEvents.class);

    public static class Receipt implements WebSocketEventHandler {
        @Override
        public Maybe<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMapMaybe(req -> {
                Optional<String> id = NodeUtil.get("id", req.getBody());
                if(!id.isPresent())
                    return Maybe.error(new FieldRequiredException("id"));

                req.getConnection().unsubscribeReceipt(id.get());

                return Maybe.empty();
            });
        }
    }

    public static class Account implements WebSocketEventHandler {
        @Override
        public Maybe<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMapCompletable(req -> req.getConnection().handleSubscriptionRequest(req, true))
                    .toMaybe();
        }
    }

    public static class Match implements WebSocketEventHandler {
        @Override
        public Maybe<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMapCompletable(req -> req.getConnection().handleSubscriptionRequest(req, true))
                    .toMaybe();
        }
    }

    public static class KeepAlive implements WebSocketEventHandler {
        @Override
        public Maybe<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMapMaybe(req -> Maybe.empty());
        }
    }

    public static class FindMatch implements WebSocketEventHandler {
        @Inject protected MatchManager matchManager;
        @Override
        public Maybe<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMapCompletable(req -> {
                Optional<String> channel = NodeUtil.get("channel", req.getBody());
                Optional<Single<WebSocketServerResponse>> findMatch = channel.map(c -> matchManager.findMatch(c)
                        .map(match -> WebSocketServerResponse.builder()
                                .event(SocketEvent.FindMatch)
                                .body(NodeUtil.mapper.createObjectNode().put("matchId", match.getId()))
                                .build()));
                return req.getConnection().handleSubscriptionRequest(req, true, findMatch);
            })
                    .toMaybe();
        }
    }
}
