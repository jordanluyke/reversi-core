package com.jordanluyke.reversi.web.api;

import com.jordanluyke.reversi.web.netty.AggregateWebSocketChannelHandlerContext;

import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface WebSocketManager {

    void addConnection(String accountId, AggregateWebSocketChannelHandlerContext context);

    List<AggregateWebSocketChannelHandlerContext> getConnections(String accountId);
}
