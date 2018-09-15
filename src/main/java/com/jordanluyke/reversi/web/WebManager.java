package com.jordanluyke.reversi.web;

import com.jordanluyke.reversi.web.netty.AggregateWebSocketChannelHandlerContext;
import rx.Observable;

import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface WebManager {

    Observable<Void> start();

    void addConnection(String accountId, AggregateWebSocketChannelHandlerContext context);

    List<AggregateWebSocketChannelHandlerContext> getConnections(String accountId);
}
