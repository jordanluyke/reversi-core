package com.jordanluyke.reversi.web.model.exceptions;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WsNotFoundException extends WsException {

    public WsNotFoundException() {
        super("Event not found");
    }
}
