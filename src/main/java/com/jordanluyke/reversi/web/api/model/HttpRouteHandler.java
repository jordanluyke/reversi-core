package com.jordanluyke.reversi.web.api.model;

import com.jordanluyke.reversi.web.model.HttpServerRequest;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface HttpRouteHandler {

    Observable<?> handle(Observable<HttpServerRequest> o);
}
