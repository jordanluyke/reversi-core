package com.jordanluyke.reversi.web.api;

import com.jordanluyke.reversi.web.api.events.OutgoingEvents;
import com.jordanluyke.reversi.web.model.WebSocketConnection;
import io.reactivex.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface SocketManager {

    void addConnection(WebSocketConnection connection);

    void removeConnection(WebSocketConnection connection);

    Observable<WebSocketConnection> getConnections(OutgoingEvents event, String channel);

    void sendUpdateEvent(OutgoingEvents event, String channel);
}
