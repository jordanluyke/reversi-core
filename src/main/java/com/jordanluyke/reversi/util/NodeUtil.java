package com.jordanluyke.reversi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.util.CharsetUtil;
import rx.functions.Func1;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NodeUtil {

    public static boolean isValidJSON(byte[] json) {
        try {
            return !new ObjectMapper().readTree(json).isNull();
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static JsonNode getJsonNode(byte[] json) {
        try {
            return new ObjectMapper().readTree(json);
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static byte[] writeValueAsBytes(Object o) {
        try {
            return new ObjectMapper().writeValueAsBytes(o);
        } catch(JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> T parseObjectNodeInto(JsonNode body, Class<T> clazz) {
        try {
            // validate optionals util
            return new ObjectMapper().treeToValue(body, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObjectNodeInto(Optional<JsonNode> body, Class<T> clazz) {
        if(!body.isPresent())
            throw new RuntimeException("Empty body in request");
        return parseObjectNodeInto(body.get(), clazz);
    }
}
