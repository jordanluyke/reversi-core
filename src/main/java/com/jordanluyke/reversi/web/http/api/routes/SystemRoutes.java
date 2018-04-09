package com.jordanluyke.reversi.web.http.api.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.web.http.api.model.RouteHandler;
import com.jordanluyke.reversi.web.http.server.model.ServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class SystemRoutes {
    private static final Logger logger = LogManager.getLogger(AccountRoutes.class);

    public static class GetStatus implements RouteHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<ServerRequest> o) {
            return o.map(r -> {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode();
                node.put("time", System.currentTimeMillis());
                node.put("status", "OK");
                return node;
            });
        }
    }
}
