package com.jordanluyke.reversi.web.api.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.model.ServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class SystemRoutes {
    private static final Logger logger = LogManager.getLogger(AccountRoutes.class);

    public static class GetStatus implements HttpRouteHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<ServerRequest> o) {
            return o.map(req -> {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode();
                node.put("time", System.currentTimeMillis());
                node.put("status", "OK");
                return node;
            });
        }
    }
}
