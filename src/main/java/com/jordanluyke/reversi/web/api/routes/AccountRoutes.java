package com.jordanluyke.reversi.web.api.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.jordanluyke.reversi.account.AccountManager;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.model.AggregateAccount;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.api.model.PagingResponse;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class AccountRoutes {
    private static final Logger logger = LogManager.getLogger(AccountRoutes.class);

    public static class GetAccounts implements HttpRouteHandler {
        @Inject protected AccountManager accountManager;
        @Override
        public Observable<PagingResponse<Account>> handle(Observable<HttpServerRequest> o) {
            return o.flatMap(req -> accountManager.getAccounts())
                    .toList()
                    .map(accounts -> new PagingResponse<>(accounts, 0, accounts.size()));
        }
    }

    public static class GetAccount implements HttpRouteHandler {
        @Inject protected AccountManager accountManager;
        @Inject protected SessionManager sessionManager;
        @Override
        public Observable<AggregateAccount> handle(Observable<HttpServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMap(session -> {
                        String accountId = req.getQueryParams().get("ownerId");
                        if(!session.getOwnerId().equals(accountId))
                            return Observable.error(new WebException(HttpResponseStatus.FORBIDDEN));
                        return Observable.zip(
                                accountManager.getAccountById(accountId),
                                accountManager.getPlayerStats(accountId),
                                AggregateAccount::new);
                    }));
        }
    }

    public static class GetPlayerStats implements HttpRouteHandler {
        @Override
        public Observable<ObjectNode> handle(Observable<HttpServerRequest> o) {
            return o.map(req -> {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode();
                node.put("class", this.getClass().getCanonicalName());
                return node;
            });
        }
    }
}
