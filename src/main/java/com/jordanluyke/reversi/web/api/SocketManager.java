package com.jordanluyke.reversi.web.api;

import com.jordanluyke.reversi.web.api.events.OutgoingEvents;
import com.jordanluyke.reversi.web.netty.AggregateWebSocketChannelHandlerContext;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface SocketManager {

    void addConnection(AggregateWebSocketChannelHandlerContext context);

    void removeConnection(AggregateWebSocketChannelHandlerContext context);

    Observable<AggregateWebSocketChannelHandlerContext> getConnections(OutgoingEvents event, String channel);

    void sendUpdateEvent(OutgoingEvents event, String channel);
}
