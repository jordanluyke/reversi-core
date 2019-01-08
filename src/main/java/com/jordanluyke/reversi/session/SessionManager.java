package com.jordanluyke.reversi.session;

import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.session.model.Session;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface SessionManager {

    Observable<Session> createSession(SessionCreationRequest sessionCreationRequest);

    Observable<Session> logout(String sessionId);

    Observable<Session> validate(HttpServerRequest request);

    Observable<Session> validate(WebSocketServerRequest request);
}
