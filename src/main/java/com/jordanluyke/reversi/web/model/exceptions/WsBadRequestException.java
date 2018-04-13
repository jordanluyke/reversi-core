package com.jordanluyke.reversi.web.model.exceptions;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WsBadRequestException extends WsException {

    public WsBadRequestException() {
        super("Bad request");
    }
}
