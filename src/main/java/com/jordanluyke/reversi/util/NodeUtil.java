package com.jordanluyke.reversi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
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

    public static <T> Observable<T> parseObjectNodeInto(JsonNode body, Class<T> clazz) {
        try {
            return Observable.just(new ObjectMapper().treeToValue(body, clazz));
        } catch (JsonProcessingException e) {
            for(Field field : clazz.getFields()) {
                field.setAccessible(true);
                String name = field.getName();
                if(body.get(name).isNull())
                    return Observable.error(new FieldRequiredException(name));
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> Observable<T> parseObjectNodeInto(Optional<JsonNode> body, Class<T> clazz) {
        return body.map(jsonNode -> parseObjectNodeInto(jsonNode, clazz)).orElseGet(() -> Observable.error(new WebException("Empty body", HttpResponseStatus.BAD_REQUEST)));
    }
}
