package com.jordanluyke.reversi.web.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.netty.AggregateWebSocketChannelHandlerContext;
import lombok.*;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebSocketServerRequest {

    private JsonNode body;
    private AggregateWebSocketChannelHandlerContext aggregateContext;
}
