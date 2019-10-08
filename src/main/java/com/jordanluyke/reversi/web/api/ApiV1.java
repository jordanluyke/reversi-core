package com.jordanluyke.reversi.web.api;

import com.jordanluyke.reversi.web.api.model.Api;
import com.jordanluyke.reversi.web.api.model.HttpRoute;
import com.jordanluyke.reversi.web.api.routes.AccountRoutes;
import com.jordanluyke.reversi.web.api.routes.MatchRoutes;
import com.jordanluyke.reversi.web.api.routes.SessionRoutes;
import com.jordanluyke.reversi.web.api.routes.SystemRoutes;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
public class ApiV1 implements Api {

    private int version = 1;

    public List<HttpRoute> getHttpRoutes() {
        return Arrays.asList(
                new HttpRoute("/status", HttpMethod.GET, SystemRoutes.GetStatus.class),
                new HttpRoute("/accounts/:accountId", HttpMethod.GET, AccountRoutes.GetAccount.class),
                new HttpRoute("/accounts/:accountId", HttpMethod.PUT, AccountRoutes.UpdateAccount.class),
                new HttpRoute("/accounts/:accountId/profile", HttpMethod.GET, AccountRoutes.GetProfile.class),
                new HttpRoute("/matches", HttpMethod.POST, MatchRoutes.CreateMatch.class),
                new HttpRoute("/matches/:matchId", HttpMethod.GET, MatchRoutes.GetMatch.class),
                new HttpRoute("/matches/:matchId/move", HttpMethod.POST, MatchRoutes.Move.class),
                new HttpRoute("/matches/:matchId/join", HttpMethod.POST, MatchRoutes.Join.class),
                new HttpRoute("/sessions", HttpMethod.POST, SessionRoutes.CreateSession.class),
                new HttpRoute("/sessions/:sessionId", HttpMethod.DELETE, SessionRoutes.DeleteSession.class)
        );
    }
}
