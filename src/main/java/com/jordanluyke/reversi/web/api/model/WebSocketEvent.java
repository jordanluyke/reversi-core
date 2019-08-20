package com.jordanluyke.reversi.web.api.model;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebSocketEvent<T extends WebSocketEventHandler> {

    private Class<? extends WebSocketEventHandler> type;

    public WebSocketEvent(Class<? extends WebSocketEventHandler> type) {
        this.type = type;
    }

    public Class<? extends WebSocketEventHandler> getType() {
        return type;
    }
}
