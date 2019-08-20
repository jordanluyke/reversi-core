package com.jordanluyke.reversi.web.api.model;

import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import io.reactivex.Single;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface WebSocketEventHandler {

    Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o);
}
