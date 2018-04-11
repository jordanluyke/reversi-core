package com.jordanluyke.reversi.web.api.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.util.WebsocketUtil;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.model.ServerResponse;
import com.jordanluyke.reversi.web.model.exceptions.HttpException;
import com.jordanluyke.reversi.web.model.ServerRequest;
import com.jordanluyke.reversi.web.model.exceptions.NotFoundException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class SystemRoutes {
    private static final Logger logger = LogManager.getLogger(AccountRoutes.class);

    public static class GetRoot implements HttpRouteHandler {
        @Override
        public Observable<ServerResponse> handle(Observable<ServerRequest> o) {
            return o.map(r -> {
                logger.info("GetRoot");
                if(!WebsocketUtil.isHandshakeRequest(r))
                    throw new NotFoundException();
                logger.info("req headers {}", r.getHeaders());
                ServerResponse res = new ServerResponse();
                Map<String, String> headers = new HashMap<>();
                headers.put("Sec-WebSocket-Accept", WebsocketUtil.getAcceptValue(r.getHeaders().get("sec-websocket-key")));
                headers.put("Upgrade", "websocket");
                headers.put("Connection", "Upgrade");
                res.setHeaders(headers);
                res.setStatus(HttpResponseStatus.SWITCHING_PROTOCOLS);
                return res;
            });
        }
    }

    public static class GetStatus implements HttpRouteHandler {
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
