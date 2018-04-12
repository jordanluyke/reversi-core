package com.jordanluyke.reversi.web.api.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.web.model.ServerRequest;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface HttpRouteHandler {

//    Observable<?> handle(Observable<ServerRequest> o);
    Observable<ObjectNode> handle(Observable<ServerRequest> o);
}
