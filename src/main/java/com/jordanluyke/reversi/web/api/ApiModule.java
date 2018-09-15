package com.jordanluyke.reversi.web.api;

import com.google.inject.AbstractModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class ApiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApiManager.class).to(ApiManagerImpl.class).asEagerSingleton();
    }
}
