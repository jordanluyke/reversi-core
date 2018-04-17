package com.jordanluyke.reversi.web.api.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.web.api.model.WebSocketEventHandler;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
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
                ObjectNode body = new ObjectMapper().createObjectNode();
                body.put("time", System.currentTimeMillis());
                body.put("status", "OK");
                return body;
            });
        }
    }

    public static class MessageReceipt implements WebSocketEventHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<WebSocketServerRequest> o) {
            return o.flatMap(req -> {
                if(req.getBody().get("id") == null)
                    return Observable.error(new FieldRequiredException("id"));

                req.getAggregateContext().onMessageReceiptReceived(req.getBody().get("id").asText());

                ObjectNode body = new ObjectMapper().createObjectNode();
                body.put("success", true);
                return Observable.just(body);
            });
        }
    }

//    public static class TestChat implements WebSocketEventHandler {
//        @Override
//        public Observable<ObjectNode> handle(Observable<WebSocketServerRequest> o) {
//            return o.map(req -> {
//                ObjectNode body = new ObjectMapper().createObjectNode();
//                body.put("username", "bullyhunter_77");
//                body.put("message", "lol");
//                return req.getAggregateContext().markReceiptRequired(body);
//            });
//        }
//    }
}
