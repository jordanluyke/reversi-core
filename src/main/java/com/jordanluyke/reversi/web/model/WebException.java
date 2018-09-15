package com.jordanluyke.reversi.web.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.api.events.OutgoingEvents;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
public class WebException extends Exception {

    private HttpResponseStatus status;
    private String exceptionType;

    public WebException(HttpResponseStatus status) {
        this(status.reasonPhrase(), status);
    }

    public WebException(String message, HttpResponseStatus status) {
        this(message, status, status.reasonPhrase().replace(" ", "") + "Exception");
    }

    public WebException(String message, HttpResponseStatus status, String exceptionType) {
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
        return new WebSocketServerResponse(OutgoingEvents.WebException, exceptionToBody());
    }

    private ObjectNode exceptionToBody() {
        ObjectNode body = NodeUtil.mapper.createObjectNode();
        body.put("exceptionId", RandomUtil.generateRandom(6));
        body.put("message", getMessage());
        body.put("exceptionType", getExceptionType());
        return body;
    }
}
