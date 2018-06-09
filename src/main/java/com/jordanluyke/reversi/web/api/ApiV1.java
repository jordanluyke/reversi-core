package com.jordanluyke.reversi.web.api;

import com.jordanluyke.reversi.web.api.events.SystemEvents;
import com.jordanluyke.reversi.web.api.model.Api;
import com.jordanluyke.reversi.web.api.model.HttpRoute;
import com.jordanluyke.reversi.web.api.model.WebSocketEvent;
import com.jordanluyke.reversi.web.api.routes.AccountRoutes;
import com.jordanluyke.reversi.web.api.routes.MatchRoutes;
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
//                new HttpRoute("/account", HttpMethod.GET, AccountRoutes.GetAccounts.class),
                new HttpRoute("/account", HttpMethod.POST, AccountRoutes.CreateAccount.class),
                new HttpRoute("/account/:accountId", HttpMethod.GET, AccountRoutes.GetAccount.class),
                new HttpRoute("/account/:accountId/messages/:messageId", HttpMethod.GET, AccountRoutes.GetAccountProfile.class),
                new HttpRoute("/match", HttpMethod.POST, MatchRoutes.CreateMatch.class),
                new HttpRoute("/match/:matchId", HttpMethod.GET, MatchRoutes.GetMatch.class),
                new HttpRoute("/match/:matchId/move", HttpMethod.POST, MatchRoutes.Move.class)
        );
    }

    @Override
    public List<WebSocketEvent> getWebSocketEvents() {
        return Arrays.asList(
                new WebSocketEvent<>(SystemEvents.KeepAlive.class),
                new WebSocketEvent<>(SystemEvents.MessageReceipt.class)
        );
    }
}
