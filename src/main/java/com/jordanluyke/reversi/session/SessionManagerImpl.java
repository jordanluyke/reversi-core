package com.jordanluyke.reversi.session;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.AccountManager;
import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.session.model.Session;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

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
    public Observable<Session> createSession(SessionCreationRequest sessionCreationRequest) {
        return accountManager.getAccountBySessionRequest(sessionCreationRequest)
                .flatMap(account -> sessionDAO.createSession(account.getId()));
    }

    @Override
    public Observable<Session> logout(String sessionId) {
        return sessionDAO.expireSession(sessionId);
    }

    @Override
    public Observable<Session> validate(HttpServerRequest request) {
        Optional<String> sessionId = Optional.ofNullable(request.getQueryParams().get("sessionId"));
        if(!sessionId.isPresent())
            return Observable.error(new WebException(HttpResponseStatus.UNAUTHORIZED));
        return sessionDAO.getSessionById(sessionId.get())
                .defaultIfEmpty(null)
                .flatMap(session -> {
                    if(session == null || (session.getExpiresAt().isPresent() && Instant.now().isAfter(session.getExpiresAt().get())))
                        return Observable.error(new WebException(HttpResponseStatus.UNAUTHORIZED));
                    return Observable.just(session);
                });
    }
}
