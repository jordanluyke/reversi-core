package com.jordanluyke.reversi.util;

import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebSocketUtil {
    private static final Logger logger = LogManager.getLogger(WebSocketUtil.class);

    public static void writeResponse(ChannelHandlerContext ctx, WebSocketServerResponse res) {
        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(Unpooled.copiedBuffer(NodeUtil.writeValueAsBytes(res.toNode())));
        ctx.write(frame);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }
}
