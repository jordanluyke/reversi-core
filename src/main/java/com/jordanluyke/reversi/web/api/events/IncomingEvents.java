package com.jordanluyke.reversi.web.api.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

                ObjectNode body = new ObjectMapper().createObjectNode();
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

    private static Observable<ObjectNode> channelSubscriptionHandler(WebSocketServerRequest req) {
        Optional<String> event = NodeUtil.getText(req.getBody(), "event");
        Optional<String> channel = NodeUtil.getText(req.getBody(), "channel");
        Optional<Boolean> unsubscribe = NodeUtil.getBoolean(req.getBody(), "unsubscribe");

        if(!event.isPresent())
            return Observable.error(new FieldRequiredException("event"));
        if(channel.isPresent() && !unsubscribe.isPresent())
            req.getAggregateContext().addEventSubscription(OutgoingEvents.valueOf(event.get()), channel.get());
        else if(!channel.isPresent() && unsubscribe.isPresent())
            req.getAggregateContext().removeEventSubscription(OutgoingEvents.valueOf(event.get()));
        else
            return Observable.error(new FieldRequiredException("channel"));

        return Observable.empty();
    }
}
