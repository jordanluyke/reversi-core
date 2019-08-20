package com.jordanluyke.reversi.web.model;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.events.OutgoingEvents;
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
public class WebSocketServerResponse {
    private static final Logger logger = LogManager.getLogger(WebSocketServerResponse.class);

    private OutgoingEvents event;
    @Builder.Default private ObjectNode body = NodeUtil.mapper.createObjectNode();

    public WebSocketServerResponse(OutgoingEvents event) {
        this.event = event;
    }

    public JsonNode toNode() {
        if(event == null) {
            logger.error("event null");
            throw new RuntimeException("event null");
        }
        ObjectNode node = NodeUtil.mapper.createObjectNode();
        node.put("event", event.toString());
        return node.setAll(body);
    }
}
