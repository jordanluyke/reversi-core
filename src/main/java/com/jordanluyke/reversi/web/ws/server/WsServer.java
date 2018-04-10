package com.jordanluyke.reversi.web.ws.server;

import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface WsServer {

    Observable<Void> start();
}
