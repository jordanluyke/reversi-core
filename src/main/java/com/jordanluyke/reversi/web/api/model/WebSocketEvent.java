package com.jordanluyke.reversi.web.api.model;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebSocketEvent<T> {

    private Class<T> type;

    public WebSocketEvent(Class<T> type) {
        this.type = type;
    }

    public Class<T> getType() {
        return type;
    }
}
