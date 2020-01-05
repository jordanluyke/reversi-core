package com.jordanluyke.reversi.lobby;

import com.google.inject.AbstractModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class LobbyModule extends AbstractModule {

    @Override
    protected void configure() {
        // remove .asEagerSingleton when lobbies variable no longer used
        bind(LobbyManager.class).to(LobbyManagerImpl.class).asEagerSingleton();
    }
}
