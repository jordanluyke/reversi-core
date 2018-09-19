package com.jordanluyke.reversi.web.api;

import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.HttpServerResponse;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface ApiManager {

    Observable<HttpServerResponse> handleRequest(HttpServerRequest request);

    Observable<WebSocketServerResponse> handleRequest(WebSocketServerRequest request);
}
