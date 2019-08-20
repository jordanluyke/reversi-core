package com.jordanluyke.reversi.web.api.events;

import com.google.inject.Inject;
import com.jordanluyke.reversi.match.MatchManager;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.WebSocketEventHandler;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import io.reactivex.Completable;
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
                if(req.getBody().get("id") == null)
                    return Single.error(new FieldRequiredException("id"));

                req.getAggregateContext().unsubscribeMessageReceipt(req.getBody().get("id").asText());

                return Single.just(WebSocketServerResponse.builder()
                                .event(OutgoingEvents.Receipt)
                                .body(NodeUtil.mapper.createObjectNode().put("success", true))
                                .build());
            });
        }
    }

    public static class Account implements WebSocketEventHandler {
        @Override
        public Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMapCompletable(IncomingEvents::channelSubscriptionHandler)
                    .toSingle(() -> WebSocketServerResponse.builder()
                                .event(OutgoingEvents.Account)
                                .body(NodeUtil.mapper.createObjectNode().put("success", true))
                                .build());
        }
    }

    public static class Match implements WebSocketEventHandler {
        @Override
        public Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMapCompletable(IncomingEvents::channelSubscriptionHandler)
                    .toSingle(() -> WebSocketServerResponse.builder()
                                .event(OutgoingEvents.Match)
                                .body(NodeUtil.mapper.createObjectNode().put("success", true))
                                .build());
        }
    }

    public static class KeepAlive implements WebSocketEventHandler {
        @Override
        public Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.map(req -> WebSocketServerResponse.builder()
                    .event(OutgoingEvents.KeepAlive)
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
            return o.flatMap(req -> sessionManager.validate(req))
                    .flatMap(session -> matchManager.findMatch(session.getOwnerId()))
                    .map(match -> WebSocketServerResponse.builder()
                                .event(OutgoingEvents.FindMatch)
                                .body(NodeUtil.mapper.createObjectNode().put("matchId", match.getId()))
                                .build());
        }
    }

    private static Completable channelSubscriptionHandler(WebSocketServerRequest req) {
        Optional<String> event = NodeUtil.get("event", req.getBody());
        Optional<String> channel = NodeUtil.get("channel", req.getBody());
        Optional<Boolean> unsubscribe = NodeUtil.getBoolean("unsubscribe", req.getBody());

        if(!event.isPresent())
            return Completable.error(new FieldRequiredException("event"));
        else if(!channel.isPresent())
            return Completable.error(new FieldRequiredException("channel"));
        else {
            if(unsubscribe.isPresent())
                req.getAggregateContext().removeEventSubscription(OutgoingEvents.valueOf(event.get()));
            else
                req.getAggregateContext().addEventSubscription(OutgoingEvents.valueOf(event.get()), channel.get());
            return Completable.complete();
        }
    }
}
