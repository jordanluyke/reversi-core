package com.jordanluyke.reversi.util;

import com.jordanluyke.reversi.web.model.ServerRequest;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebsocketUtil {

    private static String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public static boolean isHandshakeRequest(ServerRequest request) {
        return request.getMethod() == HttpMethod.GET &&
                request.getHeaders().get("upgrade").equals("websocket") &&
                request.getHeaders().get("connection").equals("Upgrade") &&
                request.getHeaders().containsKey("sec-websocket-key") &&
                request.getHeaders().get("sec-websocket-version").equals("13");
    }

    public static String getAcceptValue(String key) {
        return Hasher.sha1base64(key + magicString);
    }
}
