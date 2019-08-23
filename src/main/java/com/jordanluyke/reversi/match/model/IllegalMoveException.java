package com.jordanluyke.reversi.match.model;

import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class IllegalMoveException extends WebException {
    public static final long serialVersionUID = 101L;

    public IllegalMoveException() {
        super(HttpResponseStatus.BAD_REQUEST, "Illegal move");
    }
}
