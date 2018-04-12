package com.jordanluyke.reversi.web.model.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NotFoundException extends HttpException {

    public NotFoundException() {
        super("Invalid path", HttpResponseStatus.NOT_FOUND);
    }
}
