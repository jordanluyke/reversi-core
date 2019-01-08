package com.jordanluyke.reversi.web.model;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.events.OutgoingEvents;
import lombok.*;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebSocketServerResponse {

    private OutgoingEvents event;
    private ObjectNode body = NodeUtil.mapper.createObjectNode();

    public WebSocketServerResponse(OutgoingEvents event) {
        this.event = event;
    }

    public JsonNode toNode() {
        if(event == null)
            event = OutgoingEvents.valueOf(NodeUtil.get(body, "event").orElseThrow(() -> new RuntimeException("invalid event")));
        ObjectNode node = NodeUtil.mapper.createObjectNode();
        node.put("event", event.toString());
        return node.setAll(body);
    }
}
