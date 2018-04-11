package com.jordanluyke.reversi.web.model.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class BadRequestException extends HttpException {

    public BadRequestException() {
        super("Something went wrong", HttpResponseStatus.BAD_REQUEST, BadRequestException.class.getSimpleName());
    }
}
