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
        })
                .doOnSuccess(Void -> users.put(accountId, new UserStatus()));
    }

    @Override
    public Optional<UserStatus> getUserStatus(String accountId) {
        return Optional.ofNullable(users.get(accountId));
    }

    private void setup() {
        pusher = new Pusher(config.getPusherAppId(), config.getPusherKey(), config.getPusherSecret());
        pusher.setCluster(config.getPusherCluster());
        pusher.setEncrypted(true);
        startUserStatusUpdateInterval();
    }

    private Single<List<String>> getPresenceChannelUserIds(String channelName) {
        return Single.just(pusher.get("/channels/" + channelName + "/users"))
                .flatMap(result -> {
                    if(result.getHttpStatus() != 200) {
                        logger.error("Pusher http error {}: {}", result.getHttpStatus(), result.getMessage());
                        return Single.error(new RuntimeException("Pusher http error"));
                    }

                    try {
                        JsonNode message = NodeUtil.mapper.readTree(result.getMessage());
                        List<String> ids = new ArrayList<>();
                        for(JsonNode user : message.get("users")) {
                            Optional<String> id = NodeUtil.get("id", user);
                            if(!id.isPresent()) {
                                logger.error("id not present");
                                return Single.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR));
                            }
                            ids.add(id.get());
                        }
                        return Single.just(ids);
                    } catch(JsonProcessingException e) {
                        logger.error("Bad response: {}", e.getMessage());
                        return Single.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR));
                    }
                });

    }

    private void startUserStatusUpdateInterval() {
        Observable.interval(1, TimeUnit.SECONDS)
                .flatMapSingle(Void -> getPresenceChannelUserIds(PusherChannel.Users.getChannelName()))
                .doOnNext(channelIds -> {
                    channelIds.stream()
                            .filter(id -> !users.containsKey(id))
                            .forEach(id -> users.put(id, new UserStatus()));

                    List<String> toRemove = users.entrySet()
                            .stream()
                            .filter(user -> {
                                boolean userInChannel = channelIds.contains(user.getKey());
                                UserStatus userStatus = user.getValue();
                                if(userInChannel) {
                                    if(userStatus.getStatus() != UserStatus.Status.ACTIVE)
                                        user.getValue().reset();
                                    return false;
                                }
                                if(userStatus.getOfflineChecksRemaining() == 0) {
                                    userStatus.getOnChange().onNext(UserStatus.Status.OFFLINE);
                                    userStatus.getOnChange().onComplete();
                                    return true;
                                } else {
                                    if(userStatus.getStatus() != UserStatus.Status.DISCONNECTED) {
                                        userStatus.setStatus(UserStatus.Status.DISCONNECTED);
                                        userStatus.getOnChange().onNext(UserStatus.Status.DISCONNECTED);
                                    }
                                    userStatus.setOfflineChecksRemaining(userStatus.getOfflineChecksRemaining() - 1);
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
