package com.jordanluyke.reversi.web.api.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class AccountRoutes {
    private static final Logger logger = LogManager.getLogger(AccountRoutes.class);

    public static class GetAccounts implements HttpRouteHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<HttpServerRequest> o) {
            return o.map(req -> {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode();
                node.put("class", this.getClass().getCanonicalName());
                return node;
            });
        }
    }

    public static class CreateAccount implements HttpRouteHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<HttpServerRequest> o) {
            return o.map(req -> {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode();
                node.put("class", this.getClass().getCanonicalName());
                return node;
            });
        }
    }

    public static class GetAccount implements HttpRouteHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<HttpServerRequest> o) {
            return o.map(req -> {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode();
                node.put("class", this.getClass().getCanonicalName());
                return node;
            });
        }
    }

    public static class GetAccountProfile implements HttpRouteHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<HttpServerRequest> o) {
            return o.map(req -> {
                logger.info("params {}", req.getQueryParams());
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode();
                node.put("class", this.getClass().getCanonicalName());
                return node;
            });
        }
    }
}
