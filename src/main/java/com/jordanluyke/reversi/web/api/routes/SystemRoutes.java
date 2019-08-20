package com.jordanluyke.reversi.web.api.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import io.reactivex.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class SystemRoutes {
    private static final Logger logger = LogManager.getLogger(SystemRoutes.class);

    public static class GetStatus implements HttpRouteHandler {
        @Override
        public Single<ObjectNode> handle(Single<HttpServerRequest> o) {
            return o.map(req -> {
                ObjectNode body = new ObjectMapper().createObjectNode();
                body.put("time", System.currentTimeMillis());
                body.put("status", "OK");
                return body;
            });
        }
    }
}
