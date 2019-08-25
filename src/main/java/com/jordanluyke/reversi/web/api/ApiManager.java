package com.jordanluyke.reversi.web.api;

import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.HttpServerResponse;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface ApiManager {

    Single<HttpServerResponse> handleRequest(HttpServerRequest request);

    Maybe<WebSocketServerResponse> handleRequest(WebSocketServerRequest request);
}
