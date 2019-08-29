package com.jordanluyke.reversi.web.model;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.api.events.SocketEvent;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebSocketServerResponse {
    private static final Logger logger = LogManager.getLogger(WebSocketServerResponse.class);

    private final String receiptId = RandomUtil.generateRandom(6);

    private SocketEvent event;
    @Builder.Default private ObjectNode body = NodeUtil.mapper.createObjectNode();

    public JsonNode toNode() {
        if(event == null)
            throw new RuntimeException("event null");
        ObjectNode node = NodeUtil.mapper.createObjectNode();
        node.put("event", event.toString());
        if(!Arrays.asList(SocketEvent.Receipt, SocketEvent.KeepAlive).contains(event))
            node.put("receiptId", receiptId);
        return node.setAll(body);
    }
}
