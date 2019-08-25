package com.jordanluyke.reversi.web.api.model;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebSocketRoute {

    private Class<? extends WebSocketEventHandler> type;

    public WebSocketRoute(Class<? extends WebSocketEventHandler> type) {
        this.type = type;
    }

    public Class<? extends WebSocketEventHandler> getType() {
        return type;
    }
}
