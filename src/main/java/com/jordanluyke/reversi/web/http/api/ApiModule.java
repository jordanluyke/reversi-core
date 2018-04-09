package com.jordanluyke.reversi.web.http.api;

import com.google.inject.AbstractModule;
import com.jordanluyke.reversi.web.http.server.HttpServerModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class ApiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApiManager.class).to(ApiManagerImpl.class);
        install(new HttpServerModule());
    }
}
