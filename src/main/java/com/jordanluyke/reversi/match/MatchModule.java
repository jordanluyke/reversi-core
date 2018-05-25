package com.jordanluyke.reversi.match;

import com.google.inject.AbstractModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class MatchModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MatchManager.class).to(MatchManagerImpl.class).asEagerSingleton();
    }
}
