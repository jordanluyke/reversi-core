package com.jordanluyke.reversi.web;

import com.google.inject.AbstractModule;
import com.jordanluyke.reversi.web.api.ApiModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WebServer.class).to(WebServerImpl.class);
        install(new ApiModule());
    }
}
