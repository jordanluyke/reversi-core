package com.jordanluyke.reversi.web.api;

import com.google.inject.Inject;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.HttpServerResponse;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import io.reactivex.Single;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class ApiManagerImpl implements ApiManager {
    private static final Logger logger = LogManager.getLogger(ApiManager.class);

    private RouteMatcher routeMatcher;

    @Override
    public Single<HttpServerResponse> handleRequest(HttpServerRequest request) {
        return routeMatcher.handle(request);
    }

    @Override
    public Single<WebSocketServerResponse> handleRequest(WebSocketServerRequest request) {
        return routeMatcher.handle(request);
    }
}
