package com.jordanluyke.reversi.session;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.session.model.Session;
import com.jordanluyke.reversi.util.RandomUtil;
import lombok.AllArgsConstructor;
import rx.Observable;

import java.sql.Timestamp;
import java.util.Date;

import static org.jooq.sources.Tables.SESSION;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Singleton
@AllArgsConstructor(onConstructor = @__(@Inject))
public class SessionDAO {

    private DbManager dbManager;

    public Observable<Session> createSession(String ownerId) {
        String id = RandomUtil.generateId();
        Date date = new Date();
        return Observable.just(dbManager.getDsl().insertInto(SESSION, SESSION.ID, SESSION.CREATEDAT, SESSION.OWNERID)
                .values(id, new Timestamp(date.getTime()), ownerId)
                .execute())
                .map(Void -> Session.builder()
                        .id(id)
                        .createdAt(date)
                        .ownerId(ownerId)
                        .build());
    }
}
