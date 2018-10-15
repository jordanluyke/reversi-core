package com.jordanluyke.reversi.web.api;

import com.jordanluyke.reversi.web.api.events.IncomingEvents;
import com.jordanluyke.reversi.web.api.model.Api;
import com.jordanluyke.reversi.web.api.model.HttpRoute;
import com.jordanluyke.reversi.web.api.model.WebSocketEvent;
import com.jordanluyke.reversi.web.api.routes.AccountRoutes;
import com.jordanluyke.reversi.web.api.routes.MatchRoutes;
import com.jordanluyke.reversi.web.api.routes.SessionRoutes;
import com.jordanluyke.reversi.web.api.routes.SystemRoutes;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Arrays;
import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class ApiV1 implements Api {

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public List<HttpRoute> getHttpRoutes() {
        return Arrays.asList(
                new HttpRoute("/status", HttpMethod.GET, SystemRoutes.GetStatus.class),
//                new HttpRoute("/accounts", HttpMethod.GET, AccountRoutes.GetAccounts.class),
//                new HttpRoute("/accounts", HttpMethod.POST, AccountRoutes.CreateAccount.class),
                new HttpRoute("/accounts/:ownerId", HttpMethod.GET, AccountRoutes.GetAccount.class),
                new HttpRoute("/accounts/:ownerId/stats", HttpMethod.GET, AccountRoutes.GetPlayerStats.class),
                new HttpRoute("/matches", HttpMethod.POST, MatchRoutes.CreateMatch.class),
                new HttpRoute("/matches/:matchId", HttpMethod.GET, MatchRoutes.GetMatch.class),
                new HttpRoute("/matches/:matchId/move", HttpMethod.POST, MatchRoutes.Move.class),
                new HttpRoute("/matches/:matchId/join", HttpMethod.POST, MatchRoutes.Join.class),
                new HttpRoute("/sessions", HttpMethod.POST, SessionRoutes.CreateSession.class),
                new HttpRoute("/sessions/:sessionId", HttpMethod.DELETE, SessionRoutes.DeleteSession.class)
        );
    }

    @Override
    public List<WebSocketEvent> getWebSocketEvents() {
        return Arrays.asList(
                new WebSocketEvent<>(IncomingEvents.KeepAlive.class),
                new WebSocketEvent<>(IncomingEvents.Account.class),
                new WebSocketEvent<>(IncomingEvents.Match.class),
                new WebSocketEvent<>(IncomingEvents.Receipt.class)
//                new WebSocketEvent<>(MatchEvents.SubscribeMatch.class)
//                new WebSocketEvent<>(MatchEvents.UnsubscribeMatch.class)
        );
    }
}
