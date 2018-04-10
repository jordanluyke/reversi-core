package com.jordanluyke.reversi.web.http.api;

import com.jordanluyke.reversi.web.http.api.model.Api;
import com.jordanluyke.reversi.web.http.api.model.ApiRoute;
import com.jordanluyke.reversi.web.http.api.routes.AccountRoutes;
import com.jordanluyke.reversi.web.http.api.routes.SystemRoutes;
import io.netty.handler.codec.http.HttpMethod;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class HttpApiV1 implements Api {

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public List<ApiRoute> getRoutes() {
        return Stream.of(
                new ApiRoute("/status", HttpMethod.GET, SystemRoutes.GetStatus.class),
                new ApiRoute("/accounts", HttpMethod.GET, AccountRoutes.GetAccounts.class),
                new ApiRoute("/accounts", HttpMethod.POST, AccountRoutes.CreateAccount.class),
                new ApiRoute("/accounts/:id", HttpMethod.GET, AccountRoutes.GetAccount.class),
                new ApiRoute("/accounts/:accountId/messages/:messageId", HttpMethod.GET, AccountRoutes.GetAccountProfile.class)
        )
                .peek(route -> route.setVersion(getVersion()))
                .collect(Collectors.toList());
    }
}
