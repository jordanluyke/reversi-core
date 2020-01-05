package com.jordanluyke.reversi.web.model;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class FieldRequiredException extends WebException {
    public static final long serialVersionUID = 103L;

    public FieldRequiredException(String fieldName) {
        this(fieldName, HttpResponseStatus.BAD_REQUEST);
    }

    public FieldRequiredException(String fieldName, HttpResponseStatus status) {
        super(status, "Field required: " + fieldName, FieldRequiredException.class.getSimpleName());
    }
}
