package com.jordanluyke.reversi.web.api.routes;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.AccountManager;
import com.jordanluyke.reversi.account.dto.AccountResponse;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.session.dto.ProfileResponse;
import com.jordanluyke.reversi.session.dto.AccountUpdateRequest;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class AccountRoutes {
    private static final Logger logger = LogManager.getLogger(AccountRoutes.class);

    public static class GetAccount implements HttpRouteHandler {
        @Inject protected AccountManager accountManager;
        @Inject protected SessionManager sessionManager;
        @Override
        public Single<AccountResponse> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> {
                        String accountId = req.getQueryParams().get("accountId");
                        if(!session.getOwnerId().equals(accountId))
                            return Single.error(new WebException(HttpResponseStatus.FORBIDDEN));
                        return accountManager.getAccountById(accountId);
                    }))
                    .map(AccountResponse::fromAggregateAccount);
        }
    }

    public static class UpdateAccount implements HttpRouteHandler {
        @Inject protected AccountManager accountManager;
        @Inject protected SessionManager sessionManager;
        @Override
        public Single<AccountResponse> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> {
                        String accountId = req.getQueryParams().get("accountId");
                        if(!session.getOwnerId().equals(accountId))
                            return Single.error(new WebException(HttpResponseStatus.FORBIDDEN));
                        return NodeUtil.parseNodeInto(AccountUpdateRequest.class, req.getBody())
                                .flatMap(updateRequest -> accountManager.updateAccount(accountId, updateRequest));
                    }))
                    .map(AccountResponse::fromAggregateAccount);
        }
    }

    public static class GetProfile implements HttpRouteHandler {
        @Inject protected AccountManager accountManager;
        @Override
        public Single<ProfileResponse> handle(Single<HttpServerRequest> o) {
            return o.flatMap(req -> {
                String accountId = req.getQueryParams().get("accountId");
                return accountManager.getProfile(accountId);
            });
        }
    }
}
