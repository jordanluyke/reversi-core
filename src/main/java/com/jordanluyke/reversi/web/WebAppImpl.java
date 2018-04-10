package com.jordanluyke.reversi.web;

import com.google.inject.Inject;
import com.jordanluyke.reversi.web.http.server.HttpServer;
import com.jordanluyke.reversi.web.ws.server.WsServer;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebAppImpl implements WebApp {

    private HttpServer httpServer;
    private WsServer wsServer;

    @Inject
    public WebAppImpl(HttpServer httpServer, WsServer wsServer) {
        this.httpServer = httpServer;
        this.wsServer = wsServer;
    }

    @Override
    public Observable<Void> start() {
        return Observable.zip(
                httpServer.start(),
                wsServer.start(),
                (V1, V2) -> null)
                .ignoreElements()
                .cast(Void.class);
    }
}
