package com.jordanluyke.reversi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.util.CharsetUtil;

import java.io.IOException;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NodeUtil {

    public static boolean isValidJSON(String json) {
        try {
            new ObjectMapper().readTree(json);
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

    public static boolean isValidJSON(byte[] json) {
        try {
            new ObjectMapper().readTree(json);
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

    public static JsonNode getJsonNode(byte[] json) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.createObjectNode();
        try {
            new ObjectMapper().readTree(json);
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return node;
    }

    public static byte[] writeValueAsBytes(Object o) {
        try {
            return new ObjectMapper().writeValueAsBytes(o);
        } catch(JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
