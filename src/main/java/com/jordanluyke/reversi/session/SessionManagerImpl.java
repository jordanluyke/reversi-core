package com.jordanluyke.reversi.session;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.AccountManager;
import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.session.model.Session;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

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
        return accountManager.getAccountByEmail(sessionCreationRequest.getEmail())
                .flatMap(account -> sessionDAO.createSession(account.getId()));
    }

    @Override
    public Observable<Session> createSession(String accountId) {
        return sessionDAO.createSession(accountId);
    }
}
