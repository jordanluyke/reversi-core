package com.jordanluyke.reversi.web.api;

import com.jordanluyke.reversi.web.api.events.SystemEvents;
import com.jordanluyke.reversi.web.api.model.Api;
import com.jordanluyke.reversi.web.api.model.HttpRoute;
import com.jordanluyke.reversi.web.api.model.WebSocketEvent;
import com.jordanluyke.reversi.web.api.routes.AccountRoutes;
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
                new HttpRoute("/accounts", HttpMethod.GET, AccountRoutes.GetAccounts.class),
                new HttpRoute("/accounts", HttpMethod.POST, AccountRoutes.CreateAccount.class),
                new HttpRoute("/accounts/:accountId", HttpMethod.GET, AccountRoutes.GetAccount.class),
                new HttpRoute("/accounts/:accountId/messages/:messageId", HttpMethod.GET, AccountRoutes.GetAccountProfile.class)
        );
    }

    @Override
    public List<WebSocketEvent> getWebSocketEvents() {
        return Arrays.asList(
                new WebSocketEvent<>(SystemEvents.KeepAlive.class)
        );
    }
}
