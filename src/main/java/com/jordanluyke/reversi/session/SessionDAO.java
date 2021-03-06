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
import io.reactivex.rxjava3.core.Single;

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

    public Single<Session> createSession(String ownerId) {
        return Single.defer(() -> {
            String sessionId = RandomUtil.generateId();
            Instant expiresAt = Instant.now().plus(21, ChronoUnit.DAYS);
            return Single.just(dbManager.getDsl().insertInto(SESSION, SESSION.ID, SESSION.OWNERID, SESSION.EXPIRESAT)
                .values(sessionId, ownerId, expiresAt)
                .execute())
                .flatMap(Void -> getSessionById(sessionId));
        });
    }

    public Single<Session> expireSession(String sessionId) {
        return Single.just(dbManager.getDsl().update(SESSION)
                .set(SESSION.EXPIRESAT, Instant.now())
                .where(SESSION.ID.eq(sessionId))
                .execute())
                .flatMap(Void -> getSessionById(sessionId));
    }

    public Single<Session> getSessionById(String sessionId) {
        return Single.just(dbManager.getDsl().selectFrom(SESSION)
                .where(SESSION.ID.eq(sessionId))
                .fetchOptional())
                .flatMap(record -> {
                    if(!record.isPresent())
                        return Single.error(new WebException(HttpResponseStatus.NOT_FOUND));
                    return Single.just(Session.fromRecord(record.get()));
                });
    }
}
