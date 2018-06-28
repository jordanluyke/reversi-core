package com.jordanluyke.reversi.account;

import com.google.inject.AbstractModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class AccountModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AccountManager.class).to(AccountManagerImpl.class);
    }
}
