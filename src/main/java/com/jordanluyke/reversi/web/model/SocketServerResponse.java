package com.jordanluyke.reversi.web.model;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.api.model.SocketEvent;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SocketServerResponse {
    private static final Logger logger = LogManager.getLogger(SocketServerResponse.class);

    private SocketEvent event;
    @Builder.Default private ObjectNode body = NodeUtil.mapper.createObjectNode();

    public JsonNode toNode() {
        if(event == null)
            throw new RuntimeException("event null");
        ObjectNode node = NodeUtil.mapper.createObjectNode();
        node.put("event", event.toString());
        return node.setAll(body);
    }
}
