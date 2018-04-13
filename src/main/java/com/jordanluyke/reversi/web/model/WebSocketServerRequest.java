package com.jordanluyke.reversi.web.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebSocketServerRequest {

    JsonNode body = new ObjectMapper().createObjectNode();

    public JsonNode getBody() {
        return body;
    }

    public void setBody(JsonNode body) {
        this.body = body;
    }
}
