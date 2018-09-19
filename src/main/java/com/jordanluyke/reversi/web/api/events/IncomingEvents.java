package com.jordanluyke.reversi.web.api.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.web.api.model.WebSocketEventHandler;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.netty.AggregateWebSocketChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

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
            return o.flatMap(req -> {
                JsonNode channel = req.getBody().get("channel");
                if(channel == null)
                    return Observable.error(new FieldRequiredException("channel"));

                req.getAggregateContext().addEventSubscription(OutgoingEvents.valueOf(req.getBody().get("event").asText()), channel.asText());

                return Observable.empty();
            });
        }
    }

    public static class Match implements WebSocketEventHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<WebSocketServerRequest> o) {
            return o.flatMap(req -> {
                JsonNode channel = req.getBody().get("channel");
                if(channel == null)
                    return Observable.error(new FieldRequiredException("channel"));

                req.getAggregateContext().addEventSubscription(OutgoingEvents.valueOf(req.getBody().get("event").asText()), channel.asText());

                return Observable.empty();
            });
        }
    }

    public static class KeepAlive implements WebSocketEventHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<WebSocketServerRequest> o) {
            return o.flatMap(req -> Observable.empty());
        }
    }
}
