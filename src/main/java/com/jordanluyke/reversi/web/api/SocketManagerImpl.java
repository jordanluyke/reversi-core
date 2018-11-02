package com.jordanluyke.reversi.web.api;

import com.google.inject.Inject;
import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.util.WebSocketUtil;
import com.jordanluyke.reversi.web.api.events.OutgoingEvents;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import com.jordanluyke.reversi.web.netty.AggregateWebSocketChannelHandlerContext;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class SocketManagerImpl implements SocketManager {
    private static final Logger logger = LogManager.getLogger(SocketManager.class);

    private final Map<String, AggregateWebSocketChannelHandlerContext> connections = new HashMap<>();

    @Override
    public void addConnection(AggregateWebSocketChannelHandlerContext context) {
        connections.put(context.getCtx().channel().remoteAddress().toString(), context);

        context.getOnClose()
                .doOnCompleted(() -> removeConnection(context))
                .subscribe(new ErrorHandlingSubscriber<>());
    }

    @Override
    public void removeConnection(AggregateWebSocketChannelHandlerContext context) {
        connections.remove(context.getCtx().channel().remoteAddress().toString());
    }

    @Override
    public Observable<AggregateWebSocketChannelHandlerContext> getConnections(OutgoingEvents event, String channel) {
        return Observable.from(connections.values())
                .flatMap(aggregateContext -> Observable.from(aggregateContext.getEventSubscriptions())
                        .filter(eventSubscription -> eventSubscription.getEvent() == event)
                        .take(1)
                        .map(Void -> aggregateContext));
    }

    @Override
    public void sendUpdateEvent(OutgoingEvents event, String channel) {
        getConnections(event, channel)
                .doOnNext(connection -> WebSocketUtil.writeResponse(connection.getCtx(), new WebSocketServerResponse(event)))
                .subscribe(new ErrorHandlingSubscriber<>());
    }
}
