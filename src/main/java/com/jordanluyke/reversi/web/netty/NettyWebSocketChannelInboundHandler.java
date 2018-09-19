package com.jordanluyke.reversi.web.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.util.WebSocketUtil;
import com.jordanluyke.reversi.web.api.ApiManager;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
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
    private AggregateWebSocketChannelHandlerContext aggregateContext;

    private ByteBuf reqBuf = Unpooled.buffer();

    public NettyWebSocketChannelInboundHandler(ApiManager apiManager, AggregateWebSocketChannelHandlerContext aggregateContext) {
        this.apiManager = apiManager;
        this.aggregateContext = aggregateContext;
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
        aggregateContext.close();
        cause.printStackTrace();
    }

    private void handleWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if(frame instanceof CloseWebSocketFrame) {
            aggregateContext.close();
        } else if(frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content()));
        } else if(frame instanceof TextWebSocketFrame ||
                frame instanceof BinaryWebSocketFrame ||
                frame instanceof ContinuationWebSocketFrame) {
            reqBuf = Unpooled.copiedBuffer(reqBuf, frame.content());
            if(frame.isFinalFragment()) {
                ByteBuf b = Unpooled.copiedBuffer(reqBuf);
                reqBuf = Unpooled.buffer();
                handleRequest(b, ctx)
                        .doOnNext(res -> WebSocketUtil.writeResponse(ctx, res))
                        .subscribe(new ErrorHandlingSubscriber<>());
            }
        } else {
            logger.error("Frame not supported: {}", frame.getClass().getSimpleName());
            throw new RuntimeException("Frame not supported");
        }
    }

    private Observable<WebSocketServerResponse> handleRequest(ByteBuf content, ChannelHandlerContext ctx) {
        return Observable.defer(() -> {
            try {
                NodeUtil.isValidJSON(content.array());
            } catch(RuntimeException e) {
                return Observable.error(new WebException(HttpResponseStatus.BAD_REQUEST));
            }

            JsonNode reqBody = NodeUtil.getJsonNode(content.array());
            logger.info("received: {}", reqBody.toString());

            if(reqBody.get("event") == null)
                return Observable.error(new FieldRequiredException("event"));

            WebSocketServerRequest request = WebSocketServerRequest.builder()
                    .body(reqBody)
                    .aggregateContext(aggregateContext)
                    .build();

            return apiManager.handleRequest(request);
        })
                .onErrorResumeNext(err -> {
                    WebException e = (err instanceof WebException) ? (WebException) err : new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    logger.error("{}", e.toWebSocketServerResponse().toNode());
                    return Observable.just(e.toWebSocketServerResponse());
                });
    }
}
