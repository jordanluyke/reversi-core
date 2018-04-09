package com.jordanluyke.reversi.web;

import com.google.inject.Inject;
import com.jordanluyke.reversi.web.http.server.HttpServer;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebAppImpl implements WebApp {

    private HttpServer httpServer;

    @Inject
    public WebAppImpl(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    @Override
    public Observable<Void> start() {
        return httpServer.start();
    }
}
