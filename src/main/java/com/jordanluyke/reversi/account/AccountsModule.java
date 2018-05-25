package com.jordanluyke.reversi.account;

import com.google.inject.AbstractModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class AccountsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AccountsManager.class).to(AccountsManagerImpl.class);
    }
}
