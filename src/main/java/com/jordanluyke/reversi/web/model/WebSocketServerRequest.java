package com.jordanluyke.reversi.web.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordanluyke.reversi.web.netty.WebSocketAggregateContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketServerRequest {

    private JsonNode body = new ObjectMapper().createObjectNode();
    private WebSocketAggregateContext aggregateContext;
}
