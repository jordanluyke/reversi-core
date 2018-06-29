package com.jordanluyke.reversi;

import com.google.inject.AbstractModule;
import com.jordanluyke.reversi.account.AccountModule;
import com.jordanluyke.reversi.db.DbModule;
import com.jordanluyke.reversi.match.MatchModule;
import com.jordanluyke.reversi.session.SessionModule;
import com.jordanluyke.reversi.web.WebModule;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class MainModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MainManager.class).to(MainManagerImpl.class);
        install(new WebModule());
        install(new DbModule());
        install(new AccountModule());
        install(new MatchModule());
        install(new SessionModule());
    }
}
