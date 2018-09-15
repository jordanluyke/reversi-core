package com.jordanluyke.reversi.web.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.netty.AggregateWebSocketChannelHandlerContext;
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

    private JsonNode body = NodeUtil.mapper.createObjectNode();
    private AggregateWebSocketChannelHandlerContext aggregateContext;
}
