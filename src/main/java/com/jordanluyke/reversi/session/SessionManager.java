package com.jordanluyke.reversi.session;

import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.session.model.Session;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import io.reactivex.Single;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface SessionManager {

    Single<Session> createSession(SessionCreationRequest sessionCreationRequest);

    Single<Session> logout(String sessionId);

    Single<Session> validate(HttpServerRequest request);

    Single<Session> validate(WebSocketServerRequest request);
}
