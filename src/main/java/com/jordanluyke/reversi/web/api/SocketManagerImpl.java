package com.jordanluyke.reversi.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import com.jordanluyke.reversi.util.ErrorHandlingObserver;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.PusherChannel;
import com.jordanluyke.reversi.web.api.model.UserStatus;
import com.jordanluyke.reversi.web.model.WebException;
import com.pusher.rest.Pusher;
import com.pusher.rest.data.PresenceUser;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class SocketManagerImpl implements SocketManager {
    private static final Logger logger = LogManager.getLogger(SocketManager.class);

    private Config config;
    private Pusher pusher;

    private final Map<String, UserStatus> users = new HashMap<>();

    @Inject
    public SocketManagerImpl(Config config) {
        this.config = config;
        setup();
    }

    @Override
    public void send(PusherChannel channel) {
        send(channel, "update");
    }

    @Override
    public void send(PusherChannel channel, String event) {
        pusher.trigger(channel.getChannelName(), event, NodeUtil.mapper.createObjectNode());
    }

    @Override
    public Single<JsonNode> authenticate(String socketId, String channel, String accountId) {
        return Single.defer(() -> {
            try {
                String res = pusher.authenticate(socketId, channel, new PresenceUser(accountId));
                return Single.just(NodeUtil.mapper.readTree(res));
            } catch(JsonProcessingException e) {
                return Single.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
            }
        });
    }

    @Override
    public UserStatus getUserStatus(String accountId) {
        if(!users.containsKey(accountId))
            users.put(accountId, new UserStatus());
        return users.get(accountId);
    }

    private void setup() {
        pusher = new Pusher(config.getPusherAppId(), config.getPusherKey(), config.getPusherSecret());
        pusher.setCluster(config.getPusherCluster());
        pusher.setEncrypted(true);
        startUserStatusUpdateInterval();
    }

    private void startUserStatusUpdateInterval() {
        Observable.interval(5, TimeUnit.SECONDS)
                .map(t -> pusher.get("/channels/" + PusherChannel.Users.getChannelName() + "/users"))
                .flatMap(result -> {
                    if(result.getHttpStatus() != 200) {
                        logger.error("Pusher http error {}: {}", result.getHttpStatus(), result.getMessage());
                        return Observable.error(new RuntimeException("Pusher http error"));
                    }

                    try {
                        JsonNode message = NodeUtil.mapper.readTree(result.getMessage());
                        List<String> ids = new ArrayList<>();
                        for(JsonNode user : message.get("users")) {
                            Optional<String> id = NodeUtil.get("id", user);
                            if(!id.isPresent()) {
                                logger.error("id not present");
                                return Observable.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR));
                            }
                            ids.add(id.get());
                        }
                        return Observable.just(ids);
                    } catch(JsonProcessingException e) {
                        logger.error("Bad response: {}", e.getMessage());
                        return Observable.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR));
                    }
                })
                .doOnNext(channelIds -> {
                    channelIds.stream()
                            .filter(id -> !users.containsKey(id))
                            .forEach(id -> users.put(id, new UserStatus()));

                    List<String> toRemove = users.entrySet()
                            .stream()
                            .filter(user -> {
                                boolean userInChannel = channelIds.contains(user.getKey());
                                if(userInChannel && user.getValue().getStatus() != UserStatus.Status.ACTIVE)
                                    user.getValue().reset();
                                return !userInChannel;
                            })
                            .filter(user -> {
                                if(user.getValue().getOfflineChecksRemaining() == 0) {
                                    user.getValue().setStatus(UserStatus.Status.OFFLINE);
                                    user.getValue().getOnChange().onNext(UserStatus.Status.OFFLINE);
                                    user.getValue().getOnChange().onComplete();
                                    return true;
                                } else {
                                    user.getValue().setStatus(UserStatus.Status.IDLE);
                                    user.getValue().setOfflineChecksRemaining(user.getValue().getOfflineChecksRemaining() - 1);
                                    return false;
                                }
                            })
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());

                    toRemove.forEach(users::remove);
                })
                .subscribe(new ErrorHandlingObserver<>());
    }
}
