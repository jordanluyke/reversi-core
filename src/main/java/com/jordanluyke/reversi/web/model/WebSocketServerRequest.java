package com.jordanluyke.reversi.web.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordanluyke.reversi.web.netty.WebSocketAggregateContext;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebSocketServerRequest {

    private JsonNode body = new ObjectMapper().createObjectNode();
    private WebSocketAggregateContext aggregateContext;

    public JsonNode getBody() {
        return body;
    }

    public void setBody(JsonNode body) {
        this.body = body;
    }

    public WebSocketAggregateContext getAggregateContext() {
        return aggregateContext;
    }

    public void setAggregateContext(WebSocketAggregateContext aggregateContext) {
        this.aggregateContext = aggregateContext;
    }
}
