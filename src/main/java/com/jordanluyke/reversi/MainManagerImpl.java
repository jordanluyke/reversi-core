package com.jordanluyke.reversi;

import com.google.inject.Inject;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.web.WebManager;
import io.reactivex.rxjava3.core.Completable;
import lombok.AllArgsConstructor;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class MainManagerImpl implements MainManager {

    private WebManager webManager;
    private DbManager dbManager;

    @Override
    public Completable start() {
        return Completable.mergeArray(
                webManager.start(),
                dbManager.start()
        );
    }
}
