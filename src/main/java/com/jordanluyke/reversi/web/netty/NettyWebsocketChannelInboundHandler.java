package com.jordanluyke.reversi.web.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.ApiManager;
import com.jordanluyke.reversi.web.api.events.SystemEvents;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.util.concurrent.TimeUnit;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyWebSocketChannelInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(NettyWebSocketChannelInboundHandler.class);

    private ByteBuf reqContent = Unpooled.buffer();
    private ApiManager apiManager;

    private Subject<ChannelHandlerContext, ChannelHandlerContext> onKeepAlive = PublishSubject.create();
    private Subscription keepAliveSubscription;

    public NettyWebSocketChannelInboundHandler(ApiManager apiManager, ChannelHandlerContext ctx) {
        this.apiManager = apiManager;
        startKeepAliveTimer(ctx);
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
        logger.error("Error {}: {}", ctx.channel().remoteAddress(), cause.getMessage());
        ctx.close();
    }

    private void startKeepAliveTimer(ChannelHandlerContext context) {
        onKeepAlive.onNext(context);

        keepAliveSubscription = onKeepAlive
                .switchMap(ctx -> Observable.timer(5, TimeUnit.SECONDS).map(timer -> ctx))
                .doOnNext(ctx -> {
                    logger.info("KeepAlive timeout");
                    closeChannel(ctx);
                })
                .subscribe(new ErrorHandlingSubscriber<>());
    }

    private void handleWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if(frame instanceof CloseWebSocketFrame) {
            closeChannel(ctx);
        } else if(frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content()));
        } else if(frame instanceof TextWebSocketFrame ||
                frame instanceof BinaryWebSocketFrame ||
                frame instanceof ContinuationWebSocketFrame) {
            reqContent = Unpooled.copiedBuffer(reqContent, frame.content());
            if(frame.isFinalFragment()) {
                handleRequest(reqContent)
                        .doOnNext(res -> {
                            writeResponse(ctx, res);
                            reqContent = Unpooled.buffer();
                            if(res.getBody().get("event").asText().equals(SystemEvents.KeepAlive.class.getSimpleName()))
                                onKeepAlive.onNext(ctx);
                        })
                        .subscribe(new ErrorHandlingSubscriber<>());
            }
        } else {
            logger.error("Frame not supported: {}", frame.getClass().getSimpleName());
            throw new RuntimeException("Frame not supported");
        }
    }

    private Observable<WebSocketServerResponse> handleRequest(ByteBuf content) {
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

        return apiManager.handleWebSocketRequest(request);
    }

    private void writeResponse(ChannelHandlerContext ctx, WebSocketServerResponse res) {
        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(Unpooled.copiedBuffer(NodeUtil.writeValueAsBytes(res.getBody())));
        ctx.write(frame);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    private void closeChannel(ChannelHandlerContext ctx) {
        ctx.write(new CloseWebSocketFrame());
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        onKeepAlive.onCompleted();
        keepAliveSubscription.unsubscribe();
        logger.info("Socket closed: {}", ctx.channel().remoteAddress());
    }
}
