package com.jordanluyke.reversi.web.api;

import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Inject;
import com.jordanluyke.reversi.web.netty.AggregateWebSocketChannelHandlerContext;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class WebSocketManagerImpl implements WebSocketManager {

    private final ArrayListMultimap<String, AggregateWebSocketChannelHandlerContext> connections = ArrayListMultimap.create();

    @Override
    public void addConnection(String accountId, AggregateWebSocketChannelHandlerContext aggregateContext) {
        connections.put(accountId, aggregateContext);
        aggregateContext.startKeepAliveTimer();
        // remove on close
    }

    @Override
    public List<AggregateWebSocketChannelHandlerContext> getConnections(String accountId) {
        return connections.get(accountId);
    }
}
