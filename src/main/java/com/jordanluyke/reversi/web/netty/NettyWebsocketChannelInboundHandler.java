package com.jordanluyke.reversi.web.netty;

import com.jordanluyke.reversi.web.api.ApiManager;
import com.jordanluyke.reversi.web.model.ServerRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyWebSocketChannelInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(NettyWebSocketChannelInboundHandler.class);

//    private byte[] content = new byte[0];
    private ApiManager apiManager;
    private WebSocketServerHandshaker handshaker;

    public NettyWebSocketChannelInboundHandler(ApiManager apiManager, WebSocketServerHandshaker handshaker) {
        this.apiManager = apiManager;
        this.handshaker = handshaker;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.info("websocket read: {}", msg.getClass().getSimpleName());
        if(msg instanceof WebSocketFrame)
            handleWebsocketFrame(ctx, (WebSocketFrame) msg);
        else
            logger.info("msg is not WebSocketFrame", msg.getClass().getSimpleName());
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
        if(frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame);
        } else if(frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content()));
        } else if(frame instanceof TextWebSocketFrame) {
            String t = ((TextWebSocketFrame) frame).text();
            logger.info("TextWebSocketFrame {}", t);
        } else {
            logger.error("Frame not supported: {}", frame.getClass().getSimpleName());
            throw new RuntimeException("Frame not supported");
        }
    }
}
