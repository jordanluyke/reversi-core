package com.jordanluyke.reversi.web.api;

import com.jordanluyke.reversi.web.model.ServerRequest;
import com.jordanluyke.reversi.web.model.ServerResponse;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface ApiManager {

    Observable<ServerResponse> handleHttpRequest(ServerRequest request);
}
