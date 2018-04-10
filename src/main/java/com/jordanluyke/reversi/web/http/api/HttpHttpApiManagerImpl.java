package com.jordanluyke.reversi.web.http.api;

import com.google.inject.Inject;
import com.jordanluyke.reversi.web.http.server.model.ServerRequest;
import com.jordanluyke.reversi.web.http.server.model.ServerResponse;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class HttpHttpApiManagerImpl implements HttpApiManager {

    RouteMatcher routeMatcher;

    @Inject
    public HttpHttpApiManagerImpl(RouteMatcher routeMatcher) {
        this.routeMatcher = routeMatcher;
    }

    @Override
    public Observable<ServerResponse> handleRequest(ServerRequest request) {
        return routeMatcher.handle(request);
    }
}
