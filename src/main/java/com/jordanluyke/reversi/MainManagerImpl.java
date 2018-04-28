package com.jordanluyke.reversi;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.web.WebManager;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class MainManagerImpl implements MainManager {

    private WebManager webManager;
    private DbManager dbManager;
    private Config config;

    @Inject
    public MainManagerImpl(WebManager webManager, DbManager dbManager, Config config) {
        this.webManager = webManager;
        this.dbManager = dbManager;
        this.config = config;
    }

    @Override
    public Observable<Void> start(Injector injector) {
        config.injector = injector;
        return Observable.zip(
                webManager.start(),
                dbManager.start(),
                (V1, V2) -> null);
    }
}
