package com.jordanluyke.reversi.web.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketServerRequest {
    private WebSocketConnection connection;
    private JsonNode body;
}
