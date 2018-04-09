package com.jordanluyke.reversi.web.http.server.model;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class HttpException extends Exception {

    private HttpResponseStatus status;
    private String exceptionType;

    public HttpException(String message, HttpResponseStatus status, String exceptionType) {
        super(message);
        this.status = status;
        this.exceptionType = exceptionType;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public String getExceptionType() {
        return exceptionType;
    }
}
