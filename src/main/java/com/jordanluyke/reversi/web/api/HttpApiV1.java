package com.jordanluyke.reversi.web.api;

import com.jordanluyke.reversi.web.api.model.HttpApi;
import com.jordanluyke.reversi.web.api.model.HttpRoute;
import com.jordanluyke.reversi.web.api.routes.AccountRoutes;
import com.jordanluyke.reversi.web.api.routes.SystemRoutes;
import io.netty.handler.codec.http.HttpMethod;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class HttpApiV1 implements HttpApi {

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public List<HttpRoute> getRoutes() {
        return Stream.of(
                new HttpRoute("/", HttpMethod.GET, SystemRoutes.GetRoot.class),
                new HttpRoute("/status", HttpMethod.GET, SystemRoutes.GetStatus.class),
                new HttpRoute("/accounts", HttpMethod.GET, AccountRoutes.GetAccounts.class),
                new HttpRoute("/accounts", HttpMethod.POST, AccountRoutes.CreateAccount.class),
                new HttpRoute("/accounts/:id", HttpMethod.GET, AccountRoutes.GetAccount.class),
                new HttpRoute("/accounts/:accountId/messages/:messageId", HttpMethod.GET, AccountRoutes.GetAccountProfile.class)
        )
                .peek(route -> route.setVersion(getVersion()))
                .collect(Collectors.toList());
    }
}
