package com.jordanluyke.reversi.web.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.util.RandomUtil;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
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

    public HttpResponseStatus getStatus() {
        return status;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public HttpServerResponse toHttpServerResponse() {
        HttpServerResponse response = new HttpServerResponse();
        response.setBody(exceptionToBody());
        response.setStatus(getStatus());
        return response;
    }

    public WebSocketServerResponse toWebSocketServerResponse() {
        WebSocketServerResponse response = new WebSocketServerResponse();
        ObjectNode body = exceptionToBody();
        body.put("event", WebException.class.getSimpleName());
        response.setBody(body);
        return response;
    }

    private ObjectNode exceptionToBody() {
        ObjectNode body = new ObjectMapper().createObjectNode();
        body.put("exceptionId", RandomUtil.generateRandom(6));
        body.put("message", getMessage());
        body.put("exceptionType", getExceptionType());
        return body;
    }
}
