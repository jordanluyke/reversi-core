package com.jordanluyke.reversi.web.api;

import com.google.inject.Inject;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.HttpServerResponse;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import com.jordanluyke.reversi.web.netty.AggregateWebSocketChannelHandlerContext;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class ApiManagerImpl implements ApiManager {
    private static final Logger logger = LogManager.getLogger(ApiManager.class);

    private RouteMatcher routeMatcher;
    private Map<String, AggregateWebSocketChannelHandlerContext> aggregateContexts = new HashMap<>();

    @Inject
    public ApiManagerImpl(RouteMatcher routeMatcher) {
        this.routeMatcher = routeMatcher;
    }

    @Override
    public Observable<HttpServerResponse> handleRequest(HttpServerRequest request) {
        return routeMatcher.handle(request);
    }

    @Override
    public Observable<WebSocketServerResponse> handleRequest(WebSocketServerRequest request) {
        return routeMatcher.handle(request);
    }
}
