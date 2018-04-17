package com.jordanluyke.reversi;

import com.google.inject.Inject;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.web.WebManager;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class MainManagerImpl implements MainManager {

    private WebManager webManager;
    private DbManager dbManager;

    @Inject
    public MainManagerImpl(WebManager webManager, DbManager dbManager) {
        this.webManager = webManager;
        this.dbManager = dbManager;
    }

    @Override
    public Observable<Void> start() {
        return Observable.zip(
                webManager.start(),
                dbManager.start(),
                (V1, V2) -> null);
    }
}
