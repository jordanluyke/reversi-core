package com.jordanluyke.reversi.web.http.api.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.web.http.server.model.ServerRequest;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface RouteHandler {

    Observable<ObjectNode> handle(Observable<ServerRequest> o);
}
