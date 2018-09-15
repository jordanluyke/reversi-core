package com.jordanluyke.reversi.web.api.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.jordanluyke.reversi.web.WebManager;
import com.jordanluyke.reversi.web.api.WebSocketManager;
import com.jordanluyke.reversi.web.api.model.WebSocketEventHandler;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class IncomingEvents {
    private static final Logger logger = LogManager.getLogger(IncomingEvents.class);

    public static class MessageReceipt implements WebSocketEventHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<WebSocketServerRequest> o) {
            return o.flatMap(req -> {
                if(req.getBody().get("id").isNull())
                    return Observable.error(new FieldRequiredException("id"));

                req.getAggregateContext().unsubscribeMessageReceipt(req.getBody().get("id").asText());

                ObjectNode body = new ObjectMapper().createObjectNode();
                body.put("success", true);
                return Observable.just(body);
            });
        }
    }

    public static class Authenticate implements WebSocketEventHandler {
        @Inject protected WebSocketManager webSocketManager;
        @Override
        public Observable<ObjectNode> handle(Observable<WebSocketServerRequest> o) {
            return o.flatMap(req -> {
                if(req.getBody().get("accountId").isNull())
                    return Observable.error(new FieldRequiredException("accountId"));

                String accountId = req.getBody().get("accountId").asText();
                webSocketManager.addConnection(accountId, req.getAggregateContext());

                return Observable.empty();
            });
        }
    }
}
