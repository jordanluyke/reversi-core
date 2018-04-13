package com.jordanluyke.reversi.web.model.exceptions;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WsException extends Exception {
    private String exceptionType;

    public WsException(String message) {
        super(message);
        this.exceptionType = this.getClass().getSimpleName();
    }

    public WsException(String message, String exceptionType) {
        super(message);
        this.exceptionType = exceptionType;
    }

    public String getExceptionType() {
        return exceptionType;
    }
}
