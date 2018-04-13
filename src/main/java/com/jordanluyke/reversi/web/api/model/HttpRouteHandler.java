package com.jordanluyke.reversi.web.api.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface HttpRouteHandler {

//    Observable<?> handle(Observable<HttpServerRequest> o);
    Observable<ObjectNode> handle(Observable<HttpServerRequest> o);
}
