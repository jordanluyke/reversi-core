package com.jordanluyke.reversi.lobby;

import com.google.inject.AbstractModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class LobbyModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(LobbyManager.class).to(LobbyManagerImpl.class);
    }
}
