package com.jordanluyke.reversi.web.http.server;

import com.google.inject.AbstractModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class HttpServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HttpServer.class).to(HttpServerImpl.class);
    }
}
