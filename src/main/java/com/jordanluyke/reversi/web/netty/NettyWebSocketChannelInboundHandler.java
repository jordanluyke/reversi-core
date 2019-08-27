package com.jordanluyke.reversi.web.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.jordanluyke.reversi.util.ErrorHandlingCompletableObserver;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.util.WebSocketUtil;
import com.jordanluyke.reversi.web.api.ApiManager;
import com.jordanluyke.reversi.web.api.events.SocketEvent;
import com.jordanluyke.reversi.web.model.WebSocketConnection;
import com.jordanluyke.reversi.web.model.FieldRequiredException;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.*;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyWebSocketChannelInboundHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LogManager.getLogger(NettyWebSocketChannelInboundHandler.class);

    private ApiManager apiManager;
    private WebSocketConnection connection;

    private ByteBuf reqBuf = Unpooled.buffer();

    public NettyWebSocketChannelInboundHandler(ApiManager apiManager, WebSocketConnection connection) {
        this.apiManager = apiManager;
        this.connection = connection;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
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
        connection.close();
        cause.printStackTrace();
    }

    private void handleWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if(frame instanceof CloseWebSocketFrame) {
            connection.close();
        } else if(frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content()));
        } else if(frame instanceof TextWebSocketFrame ||
                frame instanceof BinaryWebSocketFrame ||
                frame instanceof ContinuationWebSocketFrame) {
            reqBuf = Unpooled.copiedBuffer(reqBuf, frame.content());
            if(frame.isFinalFragment()) {
                handleRequest()
                        .doOnSuccess(res -> WebSocketUtil.writeResponse(ctx, res))
                        .flatMapCompletable(Void -> Completable.complete())
                        .subscribe(new ErrorHandlingCompletableObserver());
            }
        } else {
            logger.error("Frame not supported: {}", frame.getClass().getSimpleName());
            throw new RuntimeException("Frame not supported");
        }
    }

    private Maybe<WebSocketServerResponse> handleRequest() {
        return Maybe.defer(() -> {
            reqBuf = Unpooled.buffer();
            JsonNode body;
            try {
                body = NodeUtil.getJsonNode(reqBuf.array());
            } catch(RuntimeException e) {
                return Maybe.error(new WebException(HttpResponseStatus.BAD_REQUEST));
            } finally {
                reqBuf.release();
            }

            Optional<String> event = NodeUtil.get("event", body);
            if(!event.isPresent())
                return Maybe.error(new FieldRequiredException("event"));

            if(!event.get().equals(SocketEvent.KeepAlive.toString()))
                logger.info("WebSocketRequest: {} {}", connection.getCtx().channel().remoteAddress(), body.toString());

            return apiManager.handleRequest(new WebSocketServerRequest(connection, body));
        })
                .doOnSuccess(res -> {
                    if(res.getEvent() != SocketEvent.KeepAlive)
                        logger.info("WebSocketResponse: {} {}", connection.getCtx().channel().remoteAddress(), res.toNode());
                })
                .onErrorResumeNext(err -> {
                    WebException e = (err instanceof WebException) ? (WebException) err : new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    logger.error("WebSocketResponse: {} {}", connection.getCtx().channel().remoteAddress(), e.toWebSocketServerResponse().toNode());
                    if(!(err instanceof WebException))
                        err.printStackTrace();
                    return Maybe.just(e.toWebSocketServerResponse());
                });
    }
}
