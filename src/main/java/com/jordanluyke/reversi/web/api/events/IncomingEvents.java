package com.jordanluyke.reversi.web.api.events;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.jordanluyke.reversi.match.MatchManager;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.WebSocketEventHandler;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class IncomingEvents {
    private static final Logger logger = LogManager.getLogger(IncomingEvents.class);

    public static class Receipt implements WebSocketEventHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<WebSocketServerRequest> o) {
            return o.flatMap(req -> {
                if(req.getBody().get("id") == null)
                    return Observable.error(new FieldRequiredException("id"));

                req.getAggregateContext().unsubscribeMessageReceipt(req.getBody().get("id").asText());

                ObjectNode body = NodeUtil.mapper.createObjectNode();
                body.put("success", true);
                return Observable.just(body);
            });
        }
    }

    public static class Account implements WebSocketEventHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<WebSocketServerRequest> o) {
            return o.flatMap(IncomingEvents::channelSubscriptionHandler);
        }
    }

    public static class Match implements WebSocketEventHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<WebSocketServerRequest> o) {
            return o.flatMap(IncomingEvents::channelSubscriptionHandler);
        }
    }

    public static class KeepAlive implements WebSocketEventHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<WebSocketServerRequest> o) {
            return o.flatMap(req -> Observable.empty());
        }
    }

    public static class FindMatch implements WebSocketEventHandler {
        @Inject protected MatchManager matchManager;
        @Inject protected SessionManager sessionManager;
        @Override
        public Observable<ObjectNode> handle(Observable<WebSocketServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req))
                    .flatMap(session -> matchManager.findMatch(session.getOwnerId()))
                    .map(match -> {
                        ObjectNode body = NodeUtil.mapper.createObjectNode();
                        body.put("event", OutgoingEvents.FindMatch.toString());
                        body.put("matchId", match.getId());
                        return body;
                    });
        }
    }

    private static Observable<ObjectNode> channelSubscriptionHandler(WebSocketServerRequest req) {
        Optional<String> event = NodeUtil.get(req.getBody(), "event");
        Optional<String> channel = NodeUtil.get(req.getBody(), "channel");
        Optional<Boolean> unsubscribe = NodeUtil.getBoolean(req.getBody(), "unsubscribe");

        if(!event.isPresent())
            return Observable.error(new FieldRequiredException("event"));
        if(channel.isPresent() && !unsubscribe.isPresent())
            req.getAggregateContext().addEventSubscription(OutgoingEvents.valueOf(event.get()), channel.get());
        else if(unsubscribe.isPresent())
            req.getAggregateContext().removeEventSubscription(OutgoingEvents.valueOf(event.get()));
        else
            return Observable.error(new FieldRequiredException("channel"));

        return Observable.empty();
    }
}
