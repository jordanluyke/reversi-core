package com.jordanluyke.reversi.web.model;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class FieldRequiredException extends WebException {

    public FieldRequiredException(String fieldName) {
        super("Field required: " + fieldName, HttpResponseStatus.BAD_REQUEST, FieldRequiredException.class.getSimpleName());
    }
}
