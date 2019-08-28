package com.jordanluyke.reversi.web.api;

import com.google.inject.Inject;
import com.jordanluyke.reversi.util.ErrorHandlingObserver;
import com.jordanluyke.reversi.web.api.events.SocketEvent;
import com.jordanluyke.reversi.web.api.model.EventSubscription;
import com.jordanluyke.reversi.web.model.WebException;
import com.jordanluyke.reversi.web.model.WebSocketConnection;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class SocketManagerImpl implements SocketManager {
    private static final Logger logger = LogManager.getLogger(SocketManager.class);

    private final List<WebSocketConnection> connections = new ArrayList<>();

    @Override
    public void addConnection(WebSocketConnection connection) {
        connections.add(connection);

        connection.getOnClose()
                .doOnComplete(() -> removeConnection(connection))
                .subscribe(new ErrorHandlingObserver<>());
    }

    @Override
    public void removeConnection(WebSocketConnection connection) {
        connections.remove(connection);
    }

    @Override
    public Observable<WebSocketConnection> getConnections(SocketEvent event, String channel) {
        return Observable.fromIterable(connections)
                .filter(connection -> {
                    EventSubscription eventSubscription = connection.getEventSubscriptions().get(event);
                    if(eventSubscription == null) {
                        logger.error("EventSubscription {} does not exist on channel {}", event, channel);
                        throw new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    }
                    return connection.getEventSubscriptions().containsKey(event) && eventSubscription.getChannel().equals(Optional.of(channel));
                });
    }

    @Override
    public void sendUpdateEvent(SocketEvent event, String channel) {
        getConnections(event, channel)
                .doOnNext(connection -> connection.send(WebSocketServerResponse.builder().event(event).build()))
                .subscribe(new ErrorHandlingObserver<>());
    }
}
