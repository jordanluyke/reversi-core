package com.jordanluyke.reversi.util;

import com.jordanluyke.reversi.web.model.HttpServerRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebSocketUtil {

    public static boolean isHandshakeRequest(HttpServerRequest request) {
        return request.getMethod() == HttpMethod.GET &&
                request.getHeaders().containsKey(HttpHeaderNames.UPGRADE.toString()) &&
                request.getHeaders().containsKey(HttpHeaderNames.CONNECTION.toString()) &&
                request.getHeaders().containsKey(HttpHeaderNames.SEC_WEBSOCKET_KEY.toString()) &&
                request.getHeaders().containsKey(HttpHeaderNames.SEC_WEBSOCKET_VERSION.toString()) &&
                request.getHeaders().get(HttpHeaderNames.UPGRADE.toString()).equalsIgnoreCase(HttpHeaderValues.WEBSOCKET.toString()) &&
                request.getHeaders().get(HttpHeaderNames.CONNECTION.toString()).equalsIgnoreCase(HttpHeaderValues.UPGRADE.toString());
    }
}
