package com.jordanluyke.reversi.web.api.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface WebSocketEventHandler {

    Observable<ObjectNode> handle(Observable<WebSocketServerRequest> o);
}
