package com.jordanluyke.reversi.web.api;

import com.google.inject.Inject;
import com.jordanluyke.reversi.web.model.ServerRequest;
import com.jordanluyke.reversi.web.model.ServerResponse;
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
    public Observable<ServerResponse> handleHttpRequest(ServerRequest request) {
        return routeMatcher.handle(request);
    }
}
