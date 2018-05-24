package com.jordanluyke.reversi.web.api.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.jordanluyke.reversi.accounts.AccountsManager;
import com.jordanluyke.reversi.accounts.model.Account;
import com.jordanluyke.reversi.accounts.model.AccountCreationRequest;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.api.model.PagingResponse;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class AccountRoutes {
    private static final Logger logger = LogManager.getLogger(AccountRoutes.class);

    public static class GetAccounts implements HttpRouteHandler {
        @Inject protected AccountsManager accountsManager;
        @Override
        public Observable<PagingResponse<Account>> handle(Observable<HttpServerRequest> o) {
            return o.flatMap(req -> accountsManager.getAccounts())
                    .toList()
                    .map(accounts -> new PagingResponse<>(accounts, 0, accounts.size()));
        }
    }

    public static class CreateAccount implements HttpRouteHandler {
        @Inject protected AccountsManager accountsManager;
        @Override
        public Observable<ObjectNode> handle(Observable<HttpServerRequest> o) {
            return o.map(req -> NodeUtil.parseObjectNodeInto(req.getBody(), AccountCreationRequest.class))
                    .doOnNext(Void -> logger.info("attemping to create account"))
                    .flatMap(accountsManager::createAccount)
                    .map(account -> {
                        ObjectNode body = new ObjectMapper().createObjectNode();
                        body.put("email", account.getEmail());
                        return body;
                    });
        }
    }

    public static class GetAccount implements HttpRouteHandler {
        @Inject protected AccountsManager accountsManager;
        @Override
        public Observable<Account> handle(Observable<HttpServerRequest> o) {
            return o.flatMap(req -> {
                String accountId = req.getQueryParams().get("accountId");
                return accountsManager.getAccountById(accountId);
            });
        }
    }

    public static class GetAccountProfile implements HttpRouteHandler {
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
