package com.jordanluyke.reversi.web.model;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebSocketServerResponse {

    private ObjectNode body = new ObjectMapper().createObjectNode();

    public ObjectNode getBody() {
        return body;
    }

    public void setBody(ObjectNode body) {
        this.body = body;
    }
}
