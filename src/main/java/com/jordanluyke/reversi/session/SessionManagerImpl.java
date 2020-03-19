package com.jordanluyke.reversi.session;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.AccountManager;
import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.session.model.Session;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class SessionManagerImpl implements SessionManager {
    private static final Logger logger = LogManager.getLogger(SessionManager.class);

    private SessionDAO sessionDAO;
    private AccountManager accountManager;

    @Override
    public Single<Session> createSession(SessionCreationRequest sessionCreationRequest) {
        return accountManager.getAccountBySessionRequest(sessionCreationRequest)
                .flatMap(account -> sessionDAO.createSession(account.getAccount().getId()));
    }

    @Override
    public Single<Session> logout(String sessionId) {
        return sessionDAO.expireSession(sessionId);
    }

    @Override
    public Single<Session> validate(HttpServerRequest request) {
        return validate(Optional.ofNullable(request.getQueryParams().get("sessionId")));
    }

    private Single<Session> validate(Optional<String> sessionId) {
        if(!sessionId.isPresent())
            return Single.error(new FieldRequiredException("sessionId", HttpResponseStatus.UNAUTHORIZED));
        return sessionDAO.getSessionById(sessionId.get())
                .flatMap(session -> {
                    if(session == null || (session.getExpiresAt().isPresent() && Instant.now().isAfter(session.getExpiresAt().get())))
                        return Single.error(new WebException(HttpResponseStatus.UNAUTHORIZED));
                    return Single.just(session);
                });
    }
}
