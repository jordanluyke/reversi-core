package com.jordanluyke.reversi.session;

import com.google.inject.AbstractModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class SessionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SessionManager.class).to(SessionManagerImpl.class);
    }
}
