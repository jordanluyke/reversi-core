package com.jordanluyke.reversi.web.http.server;

import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface HttpServer {

    Observable<Void> start();
}
