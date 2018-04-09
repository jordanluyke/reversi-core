package com.jordanluyke.reversi.web;

import com.google.inject.AbstractModule;
import com.jordanluyke.reversi.web.http.api.ApiModule;
import com.jordanluyke.reversi.web.http.server.HttpServerModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebAppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WebApp.class).to(WebAppImpl.class);
        install(new ApiModule());
        install(new HttpServerModule());
    }
}
