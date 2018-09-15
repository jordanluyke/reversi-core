package com.jordanluyke.reversi.web;

import com.google.inject.AbstractModule;
import com.jordanluyke.reversi.web.api.ApiModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WebManager.class).to(WebManagerImpl.class).asEagerSingleton();
        install(new ApiModule());
    }
}
