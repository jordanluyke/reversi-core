package com.jordanluyke.reversi.web.api.events;

import com.google.inject.Inject;
import com.jordanluyke.reversi.match.MatchManager;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.SocketManager;
import com.jordanluyke.reversi.web.api.model.WebSocketEventHandler;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class IncomingEvents {
    private static final Logger logger = LogManager.getLogger(IncomingEvents.class);

    public static class Receipt implements WebSocketEventHandler {
        @Inject SocketManager socketManager;
        @Override
        public Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMap(req -> {
                Optional<String> id = NodeUtil.get("id", req.getBody());
                if(!id.isPresent())
                    return Single.error(new FieldRequiredException("id"));
                req.getConnection().unsubscribeMessageReceipt(id.get());

                return Single.just(WebSocketServerResponse.builder()
                                .event(SocketEvent.Receipt)
                                .body(NodeUtil.mapper.createObjectNode().put("success", true))
                                .build());
            });
        }
    }

    public static class Account implements WebSocketEventHandler {
        @Override
        public Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMapCompletable(IncomingEvents::channelSubscriptionHandler)
                    .toSingle(() -> WebSocketServerResponse.builder()
                                .event(SocketEvent.Account)
                                .body(NodeUtil.mapper.createObjectNode().put("success", true))
                                .build());
        }
    }

    public static class Match implements WebSocketEventHandler {
        @Override
        public Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMapCompletable(IncomingEvents::channelSubscriptionHandler)
                    .toSingle(() -> WebSocketServerResponse.builder()
                                .event(SocketEvent.Match)
                                .body(NodeUtil.mapper.createObjectNode().put("success", true))
                                .build());
        }
    }

    public static class KeepAlive implements WebSocketEventHandler {
        @Override
        public Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.map(req -> WebSocketServerResponse.builder()
                    .event(SocketEvent.KeepAlive)
                    .body(NodeUtil.mapper.createObjectNode()
                            .put("time", Instant.now().toEpochMilli()))
                    .build());
        }
    }

    public static class FindMatch implements WebSocketEventHandler {
        @Inject protected MatchManager matchManager;
        @Inject protected SessionManager sessionManager;
        @Override
        public Single<WebSocketServerResponse> handle(Single<WebSocketServerRequest> o) {
            return o.flatMap(req -> sessionManager.validate(req)
                    .flatMapCompletable(session -> {
                        Optional<String> event = NodeUtil.get("event", req.getBody());
                        Optional<String> channel = NodeUtil.get("channel", req.getBody());
                        Optional<Boolean> unsubscribe = NodeUtil.getBoolean("unsubscribe", req.getBody());

                        if(!event.isPresent())
                            return Completable.error(new FieldRequiredException("event"));
                        if(!unsubscribe.isPresent() && !channel.isPresent())
                            return Completable.error(new FieldRequiredException("channel"));
                        if(unsubscribe.isPresent())
                            req.getConnection().removeEventSubscription(SocketEvent.valueOf(event.get()));
                        else {
                            Disposable findMatchDisposable = matchManager.findMatch(session.getOwnerId())
                                    .map(match -> WebSocketServerResponse.builder()
                                            .event(SocketEvent.FindMatch)
                                            .body(NodeUtil.mapper.createObjectNode().put("matchId", match.getId()))
                                            .build())
                                    .doOnSuccess(Void -> req.getConnection().removeEventSubscription(SocketEvent.valueOf(event.get())))
                                    .subscribe();
                            req.getConnection().addEventSubscription(SocketEvent.valueOf(event.get()), channel.get(), Optional.of(findMatchDisposable));
                        }
                        return Completable.complete();
                    })
                    .toSingle(() -> WebSocketServerResponse.builder()
                                        .event(SocketEvent.FindMatch)
                                        .body(NodeUtil.mapper.createObjectNode().put("success", true))
                                        .build()));
        }
    }

    private static Completable channelSubscriptionHandler(WebSocketServerRequest req, Optional<Disposable> disposable) {
        return Completable.defer(() -> {
            Optional<String> event = NodeUtil.get("event", req.getBody());
            Optional<String> channel = NodeUtil.get("channel", req.getBody());
            Optional<Boolean> unsubscribe = NodeUtil.getBoolean("unsubscribe", req.getBody());

            if(!event.isPresent())
                return Completable.error(new FieldRequiredException("event"));
            if(!unsubscribe.isPresent() && !channel.isPresent())
                return Completable.error(new FieldRequiredException("channel"));
            if(unsubscribe.isPresent())
                req.getConnection().removeEventSubscription(SocketEvent.valueOf(event.get()));
            else
                req.getConnection().addEventSubscription(SocketEvent.valueOf(event.get()), channel.get(), disposable);
            return Completable.complete();
        });
    }

    private static Completable channelSubscriptionHandler(WebSocketServerRequest req) {
        return channelSubscriptionHandler(req, Optional.empty());
    }
}
