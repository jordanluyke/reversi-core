package com.jordanluyke.reversi.web.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.ApiManager;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyWebSocketChannelInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(NettyWebSocketChannelInboundHandler.class);

    private ApiManager apiManager;

    private WebSocketAggregateContext aggregateContext;
    private ByteBuf reqBuf = Unpooled.buffer();

    public NettyWebSocketChannelInboundHandler(ApiManager apiManager, ChannelHandlerContext ctx) {
        this.apiManager = apiManager;
        this.aggregateContext = apiManager.registerWebSocketChannelHandlerContext(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.info("channelRead: {} {}", ctx.channel().remoteAddress(), msg.getClass().getSimpleName());
        if(msg instanceof WebSocketFrame) {
            handleWebsocketFrame(ctx, (WebSocketFrame) msg);
        } else {
            logger.error("Not a WebSocketFrame: {}", msg.getClass().getCanonicalName());
            throw new RuntimeException("Not a WebSocketFrame");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught on {}", ctx.channel().remoteAddress());
        apiManager.deregisterWebSocketChannelHandlerContext(ctx);
        cause.printStackTrace();
        ctx.close();
    }

    private void handleWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if(frame instanceof CloseWebSocketFrame) {
            apiManager.deregisterWebSocketChannelHandlerContext(ctx);
        } else if(frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content()));
        } else if(frame instanceof TextWebSocketFrame ||
                frame instanceof BinaryWebSocketFrame ||
                frame instanceof ContinuationWebSocketFrame) {
            reqBuf = Unpooled.copiedBuffer(reqBuf, frame.content());
            if(frame.isFinalFragment()) {
                handleRequest(reqBuf, ctx)
                        .doOnNext(res -> {
                            writeResponse(ctx, res);
                            reqBuf = Unpooled.buffer();
                        })
                        .subscribe(new ErrorHandlingSubscriber<>());
            }
        } else {
            logger.error("Frame not supported: {}", frame.getClass().getSimpleName());
            throw new RuntimeException("Frame not supported");
        }
    }

    private Observable<WebSocketServerResponse> handleRequest(ByteBuf content, ChannelHandlerContext ctx) {
        try {
            NodeUtil.isValidJSON(content.array());
        } catch(RuntimeException e) {
            return Observable.just(new WebException(HttpResponseStatus.BAD_REQUEST).toWebSocketServerResponse());
        }

        JsonNode reqBody = NodeUtil.getJsonNode(content.array());
        logger.info("body: {}", reqBody.toString());

        if(reqBody.get("event").isNull())
            return Observable.just(new WebException(HttpResponseStatus.NOT_FOUND).toWebSocketServerResponse());

        WebSocketServerRequest request = new WebSocketServerRequest();
        request.setBody(reqBody);
        request.setAggregateContext(aggregateContext);

        return apiManager.handleRequest(request);
    }

    private void writeResponse(ChannelHandlerContext ctx, WebSocketServerResponse res) {
        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(Unpooled.copiedBuffer(NodeUtil.writeValueAsBytes(res.getBody())));
        ctx.write(frame);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }
}
