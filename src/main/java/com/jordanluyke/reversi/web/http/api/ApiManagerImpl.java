package com.jordanluyke.reversi.web.http.api;

import com.google.inject.Inject;
import com.jordanluyke.reversi.web.http.server.model.ServerRequest;
import com.jordanluyke.reversi.web.http.server.model.ServerResponse;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class ApiManagerImpl implements ApiManager {

    RouteMatcher routeMatcher;

    @Inject
    public ApiManagerImpl(RouteMatcher routeMatcher) {
        this.routeMatcher = routeMatcher;
    }

    @Override
    public Observable<ServerResponse> handleRequest(ServerRequest request) {
        return routeMatcher.handle(request);
    }
}
