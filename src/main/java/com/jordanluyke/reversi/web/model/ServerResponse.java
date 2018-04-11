package com.jordanluyke.reversi.web.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class ServerResponse {

    private HttpResponseStatus status;
    private ObjectNode body;
    private Map<String, String> headers = new HashMap<>();

    public ServerResponse() {
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public void setStatus(HttpResponseStatus status) {
        this.status = status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public ObjectNode getBody() {
        return body;
    }

    public void setBody(ObjectNode body) {
        this.body = body;
    }
}
