package com.jordanluyke.reversi.db;

import com.google.inject.AbstractModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class DbModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DbManager.class).to(DbManagerImpl.class);
    }
}
