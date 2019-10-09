package com.jordanluyke.reversi.web.api;

import com.jordanluyke.reversi.web.api.model.SocketChannel;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface SocketManager {
    void send(SocketChannel channel, String event);
}
