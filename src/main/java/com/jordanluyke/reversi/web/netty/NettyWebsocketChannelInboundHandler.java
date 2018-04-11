package com.jordanluyke.reversi.web.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyWebsocketChannelInboundHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LogManager.getLogger(NettyWebsocketChannelInboundHandler.class);

    private WebSocketServerHandshaker handshaker;
    private byte[] content = new byte[0];

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        logger.info("channel read {}", msg.getClass().getSimpleName());
//        if(msg instanceof WebSocketFrame) {
//            handleWebsocketFrame(ctx, (WebSocketFrame) msg);
//        } else {
//            logger.info("msg is not WebSocketFrame", msg.getClass().getSimpleName());
//        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void handleWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if(frame instanceof CloseWebSocketFrame)
            logger.info("close");
            //            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame);
        else if(frame instanceof PingWebSocketFrame)
            ctx.channel().write(new PongWebSocketFrame(frame.content()));
        else if(frame instanceof TextWebSocketFrame) {
            String t = ((TextWebSocketFrame) frame).text();
            logger.info("TextWebSocketFrame {}", t);
        } else {
            logger.error("Frame not supported: {}", frame.getClass().getSimpleName());
            throw new RuntimeException("Frame not supported");
        }
    }
}
