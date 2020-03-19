package com.jordanluyke.reversi.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.PusherChannel;
import com.jordanluyke.reversi.web.model.WebException;
import com.pusher.rest.Pusher;
import com.pusher.rest.data.PresenceUser;
import com.pusher.rest.data.Result;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class SocketManagerImpl implements SocketManager {
    private static final Logger logger = LogManager.getLogger(SocketManager.class);

    private Config config;
    private Pusher pusher;

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
    public Single<JsonNode> authenticate(String socketId, String channel, PresenceUser user) {
        try {
            String res = pusher.authenticate(socketId, channel, user);
            return Single.just(NodeUtil.mapper.readTree(res));
        } catch(JsonProcessingException e) {
            return Single.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    @Override
    public Observable<String> getActiveUserIds() {
        Result result = pusher.get("/channels/" + PusherChannel.Users.getChannelName() + "/users");
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
            return Observable.fromIterable(ids);
        } catch(JsonProcessingException e) {
            return Observable.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR));
        }
    }

    private void setup() {
        pusher = new Pusher(config.getPusherAppId(), config.getPusherKey(), config.getPusherSecret());
        pusher.setCluster(config.getPusherCluster());
        pusher.setEncrypted(true);
    }
}
