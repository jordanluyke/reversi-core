package com.jordanluyke.reversi.web;

import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Inject;
import com.jordanluyke.reversi.web.netty.AggregateWebSocketChannelHandlerContext;
import com.jordanluyke.reversi.web.netty.NettyServerInitializer;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class WebManagerImpl implements WebManager {
    private static final Logger logger = LogManager.getLogger(WebManager.class);

    private NettyServerInitializer nettyServerInitializer;

    private final ArrayListMultimap<String, AggregateWebSocketChannelHandlerContext> connections = ArrayListMultimap.create();

    @Override
    public Observable<Void> start() {
        return nettyServerInitializer.initialize();
    }

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
