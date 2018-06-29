package com.jordanluyke.reversi;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.web.WebManager;
import lombok.AllArgsConstructor;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class MainManagerImpl implements MainManager {

    private WebManager webManager;
    private DbManager dbManager;
    private Config config;

    @Override
    public Observable<Void> start(Injector injector) {
        config.setInjector(injector);
        return Observable.zip(
                webManager.start(),
                dbManager.start(),
                (V1, V2) -> null);
    }
}
