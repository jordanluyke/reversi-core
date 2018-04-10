package com.jordanluyke.reversi.web;

import com.google.inject.AbstractModule;
import com.jordanluyke.reversi.web.http.api.HttpApiModule;
import com.jordanluyke.reversi.web.http.server.HttpServerModule;
import com.jordanluyke.reversi.web.ws.server.WsServerModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebAppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WebApp.class).to(WebAppImpl.class);
        install(new HttpApiModule());
        install(new HttpServerModule());
        install(new WsServerModule());
    }
}
