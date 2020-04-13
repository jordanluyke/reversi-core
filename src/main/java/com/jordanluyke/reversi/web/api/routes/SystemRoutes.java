package com.jordanluyke.reversi.web.api.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import com.jordanluyke.reversi.account.AccountManager;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.SocketManager;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class SystemRoutes {
    private static final Logger logger = LogManager.getLogger(SystemRoutes.class);

    public static class GetStatus implements HttpRouteHandler {
        @Inject protected Config config;
        @Override
        public Single<ObjectNode> handle(Single<HttpServerRequest> o) {
            return o.map(req -> {
                ObjectNode body = new ObjectMapper().createObjectNode();
                body.put("branch", config.getBranch());
                body.put("builtAt", config.getBuiltAt());
                body.put("commit", config.getCommit());
                return body;
            });
        }
    }

    public static class GetConfig implements HttpRouteHandler {
        @Inject protected Config config;
        @Override
        public Single<ObjectNode> handle(Single<HttpServerRequest> o) {
            return o.map(req -> {
                ObjectNode body = new ObjectMapper().createObjectNode();
                body.put("pusherKey", config.getPusherKey());
                body.put("pusherCluster", config.getPusherCluster());
                return body;
            });
        }
    }

    public static class PusherAuth implements HttpRouteHandler {
        @Inject protected SessionManager sessionManager;
        @Inject protected AccountManager accountManager;
        @Inject protected SocketManager socketManager;
        @Override
        public Single<JsonNode> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> accountManager.getAccountById(session.getOwnerId()))
                    .flatMap(account -> {
                        if(!req.getBody().isPresent())
                            return Single.error(new WebException(HttpResponseStatus.BAD_REQUEST));
                        Optional<String> socketId = NodeUtil.get("socket_id", req.getBody().get());
                        Optional<String> channelName = NodeUtil.get("channel_name", req.getBody().get());
                        if(!socketId.isPresent() || !channelName.isPresent())
                            return Single.error(new WebException (HttpResponseStatus.BAD_REQUEST));
                        return socketManager.authenticate(socketId.get(), channelName.get(), account.getAccount().getId());
                    }));
        }
    }
}
