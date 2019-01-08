package com.jordanluyke.reversi.session;

import com.google.inject.Inject;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.session.model.Session;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
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
        String sessionId = RandomUtil.generateId();
        Instant expiresAt = Instant.now().plus(21, ChronoUnit.DAYS);
        return Observable.just(dbManager.getDsl().insertInto(SESSION, SESSION.ID, SESSION.OWNERID, SESSION.EXPIRESAT)
                .values(sessionId, ownerId, expiresAt)
                .execute())
                .flatMap(Void -> getSessionById(sessionId));
    }

    public Observable<Session> expireSession(String sessionId) {
        return Observable.just(dbManager.getDsl().update(SESSION)
                .set(SESSION.EXPIRESAT, Instant.now())
                .where(SESSION.ID.eq(sessionId))
                .execute())
                .flatMap(Void -> getSessionById(sessionId));
    }

    public Observable<Session> getSessionById(String sessionId) {
        return Observable.just(dbManager.getDsl().selectFrom(SESSION)
                .where(SESSION.ID.eq(sessionId))
                .fetchAny())
                .defaultIfEmpty(null)
                .flatMap(record -> {
                    if(record == null)
                        return Observable.error(new WebException(HttpResponseStatus.UNAUTHORIZED));
                    return Observable.just(record);
                })
                .map(Session::fromRecord);
    }
}
