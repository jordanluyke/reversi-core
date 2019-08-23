package com.jordanluyke.reversi.web.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.api.events.SocketEvent;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
public class WebException extends Exception {
    public static final long serialVersionUID = 100L;

    private HttpResponseStatus status;
    private String exceptionType;

    public WebException(HttpResponseStatus status) {
        this(status, status.reasonPhrase());
    }

    public WebException(HttpResponseStatus status, String message) {
        this(status, message, status.reasonPhrase().replace(" ", "") + "Exception");
    }

    public WebException(HttpResponseStatus status, String message, String exceptionType) {
        super(message);
        this.status = status;
        this.exceptionType = exceptionType;
    }

    public HttpServerResponse toHttpServerResponse() {
        HttpServerResponse response = new HttpServerResponse();
        response.setBody(exceptionToBody());
        response.setStatus(getStatus());
        return response;
    }

    public WebSocketServerResponse toWebSocketServerResponse() {
        return new WebSocketServerResponse(SocketEvent.WebException, exceptionToBody());
    }

    private ObjectNode exceptionToBody() {
        ObjectNode body = NodeUtil.mapper.createObjectNode();
        body.put("exceptionId", RandomUtil.generateRandom(6));
        body.put("message", getMessage());
        body.put("exceptionType", getExceptionType());
        return body;
    }
}
