package com.jordanluyke.reversi.web.api;

import com.google.inject.Inject;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.HttpServerResponse;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class ApiManagerImpl implements ApiManager {

    private RouteMatcher routeMatcher;

    @Inject
    public ApiManagerImpl(RouteMatcher routeMatcher) {
        this.routeMatcher = routeMatcher;
    }

    @Override
    public Observable<HttpServerResponse> handleHttpRequest(HttpServerRequest request) {
        return routeMatcher.handle(request);
    }

    @Override
    public Observable<WebSocketServerResponse> handleWebSocketRequest(WebSocketServerRequest request) {
        return routeMatcher.handle(request);
    }
}
