package com.jordanluyke.reversi.session;

import com.google.inject.Inject;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.session.model.Session;
import com.jordanluyke.reversi.util.RandomUtil;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.jooq.sources.Tables.SESSION;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class SessionDAO {
    private static final Logger logger = LogManager.getLogger(SessionDAO.class);

    private DbManager dbManager;

    public Observable<Session> createSession(String ownerId) {
        String id = RandomUtil.generateId();
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.DAYS);
        return Observable.just(dbManager.getDsl().insertInto(SESSION, SESSION.ID, SESSION.OWNERID, SESSION.EXPIRESAT)
                .values(id, ownerId, expiresAt)
                .execute())
                .flatMap(Void -> getSessionById(id));
    }

    public Observable<Session> getSessionById(String sessionId) {
        return Observable.just(dbManager.getDsl().selectFrom(SESSION)
                .where(SESSION.ID.eq(sessionId))
                .fetchAny())
                .map(Session::fromRecord);
    }
}
