package com.jordanluyke.reversi.web.model.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class HttpException extends Exception {

    private HttpResponseStatus status;
    private String exceptionType;

    public HttpException(String message, HttpResponseStatus status) {
        super(message);
        this.status = status;
        this.exceptionType = this.getClass().getSimpleName();
    }

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
