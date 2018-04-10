package com.jordanluyke.reversi.web.ws.server;

import com.google.inject.AbstractModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WsServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WsServer.class).to(WsServerImpl.class);
    }
}
