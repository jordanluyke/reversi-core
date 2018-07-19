package com.jordanluyke.reversi.session;

import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.session.model.Session;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface SessionManager {

    Observable<Session> createSession(SessionCreationRequest sessionCreationRequest);
}
