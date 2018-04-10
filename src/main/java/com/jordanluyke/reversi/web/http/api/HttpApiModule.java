package com.jordanluyke.reversi.web.http.api;

import com.google.inject.AbstractModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class HttpApiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HttpApiManager.class).to(HttpHttpApiManagerImpl.class);
    }
}
