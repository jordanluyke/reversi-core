package com.jordanluyke.reversi.web.api;

import com.jordanluyke.reversi.web.api.model.SocketEvent;
import io.reactivex.rxjava3.core.Completable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface SocketManager {
    void send(SocketEvent event, String channel);
}
