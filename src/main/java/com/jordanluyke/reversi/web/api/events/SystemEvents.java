package com.jordanluyke.reversi.web.api.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.web.api.model.WebSocketEventHandler;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class SystemEvents {
        private static final Logger logger = LogManager.getLogger(SystemEvents.class);

        public static class KeepAlive implements WebSocketEventHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<WebSocketServerRequest> o) {
            return o.map(req -> {
                req.getAggregateContext().onKeepAlive.onNext(null);
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode();
                node.put("time", System.currentTimeMillis());
                node.put("status", "OK");
                return node;
            });
        }
    }
}
