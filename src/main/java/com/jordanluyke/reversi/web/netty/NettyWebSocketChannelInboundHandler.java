package com.jordanluyke.reversi.web.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.jordanluyke.reversi.util.ErrorHandlingCompletableObserver;
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
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.*;
import io.reactivex.Completable;
import io.reactivex.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyWebSocketChannelInboundHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LogManager.getLogger(NettyWebSocketChannelInboundHandler.class);

    private ApiManager apiManager;
    private AggregateWebSocketChannelHandlerContext aggregateContext;

    private ByteBuf reqBuf = Unpooled.buffer();

    public NettyWebSocketChannelInboundHandler(ApiManager apiManager, AggregateWebSocketChannelHandlerContext aggregateContext) {
        this.apiManager = apiManager;
        this.aggregateContext = aggregateContext;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
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

    private Single<WebSocketServerResponse> handleRequest() {
        return Single.defer(() -> {
            try {
                NodeUtil.isValidJSON(reqBuf.array());
            } catch(RuntimeException e) {
                return Single.error(new WebException(HttpResponseStatus.BAD_REQUEST));
            }

            JsonNode reqBody = NodeUtil.getJsonNode(reqBuf.array());
            reqBuf = Unpooled.buffer();
            logger.info("received: {}", reqBody.toString());

            if(reqBody.get("event") == null)
                return Single.error(new FieldRequiredException("event"));

            WebSocketServerRequest request = WebSocketServerRequest.builder()
                    .body(reqBody)
                    .aggregateContext(aggregateContext)
                    .build();

            return apiManager.handleRequest(request);
        })
                .onErrorResumeNext(err -> {
                    WebException e = (err instanceof WebException) ? (WebException) err : new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    logger.error("{}", e.toWebSocketServerResponse().toNode());
                    return Single.just(e.toWebSocketServerResponse());
                });
    }
}
