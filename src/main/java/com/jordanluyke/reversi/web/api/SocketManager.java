package com.jordanluyke.reversi.web.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.jordanluyke.reversi.web.api.model.PusherChannel;
import com.jordanluyke.reversi.web.api.model.UserStatus;
import io.reactivex.rxjava3.core.Single;

import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface SocketManager {
    void send(PusherChannel channel);
    void send(PusherChannel channel, String event);
    Single<JsonNode> authenticate(String socketId, String channel, String accountId);
    Optional<UserStatus> getUserStatus(String accountId);
}
