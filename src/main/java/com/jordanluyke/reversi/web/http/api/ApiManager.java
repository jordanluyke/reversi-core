package com.jordanluyke.reversi.web.http.api;

import com.jordanluyke.reversi.web.http.server.model.ServerRequest;
import com.jordanluyke.reversi.web.http.server.model.ServerResponse;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface ApiManager {

    Observable<ServerResponse> handleRequest(ServerRequest request);
}
