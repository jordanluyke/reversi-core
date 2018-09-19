package com.jordanluyke.reversi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger logger = LogManager.getLogger(NodeUtil.class);

    public static ObjectMapper mapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
//            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
//            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
//            .setSerializationInclusion(Include.NON_NULL);

    public static boolean isValidJSON(byte[] json) {
        try {
            return !mapper.readTree(json).isNull();
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static JsonNode getJsonNode(byte[] json) {
        try {
            return mapper.readTree(json);
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static byte[] writeValueAsBytes(Object o) {
        try {
            return mapper.writeValueAsBytes(o);
        } catch(JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> Observable<T> parseObjectNodeInto(JsonNode body, Class<T> clazz) {
        try {
            return Observable.just(mapper.treeToValue(body, clazz));
        } catch (Exception e) {
            logger.error("Json serialize fail: {}", e.getMessage());
            e.printStackTrace();
            for(Field field : clazz.getFields()) {
                field.setAccessible(true);
                String name = field.getName();
                if(body.get(name) == null)
                    return Observable.error(new FieldRequiredException(name));
            }
            return Observable.error(new RuntimeException(e.getMessage()));
        }
    }

    public static <T> Observable<T> parseObjectNodeInto(Optional<JsonNode> body, Class<T> clazz) {
        return body.map(jsonNode -> parseObjectNodeInto(jsonNode, clazz)).orElseGet(() -> Observable.error(new WebException("Empty body", HttpResponseStatus.BAD_REQUEST)));
    }

    public static Optional<String> getText(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if(fieldNode == null)
            return Optional.empty();
        return Optional.of(fieldNode.asText());
    }

    public static Optional<Boolean> getBoolean(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if(fieldNode == null)
            return Optional.empty();
        return Optional.of(fieldNode.asBoolean());
    }

    public static Optional<Integer> getInteger(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if(fieldNode == null)
            return Optional.empty();
        return Optional.of(fieldNode.asInt());
    }
}
