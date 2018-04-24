package com.jordanluyke.reversi.web.api.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.jordanluyke.reversi.accounts.AccountsManager;
import com.jordanluyke.reversi.accounts.model.Account;
import com.jordanluyke.reversi.accounts.model.AccountCreationRequest;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.HttpRouteHandler;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class AccountRoutes {
    private static final Logger logger = LogManager.getLogger(AccountRoutes.class);

    public static class GetAccounts implements HttpRouteHandler {
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
