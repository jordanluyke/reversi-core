package com.jordanluyke.reversi.web.netty;

import com.jordanluyke.reversi.util.ByteUtil;
import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.ApiManager;
import com.jordanluyke.reversi.web.api.SocketManager;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.HttpServerResponse;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyHttpChannelInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(NettyHttpChannelInboundHandler.class);

    private ApiManager apiManager;
    private SocketManager socketManager;

    private byte[] reqBytes = new byte[0];
    private HttpServerRequest httpServerRequest = new HttpServerRequest();

    public NettyHttpChannelInboundHandler(ApiManager apiManager, SocketManager socketManager) {
        this.apiManager = apiManager;
        this.socketManager = socketManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            URI uri = new URI(httpRequest.uri());

            httpServerRequest.setPath(uri.getRawPath());
            httpServerRequest.setMethod(httpRequest.method());
            httpServerRequest.setHeaders(httpRequest.headers()
                    .entries()
                    .stream()
                    .collect(Collectors.toMap(key -> key.getKey().toLowerCase(), Map.Entry::getValue)));
            httpServerRequest.setQueryParams(new QueryStringDecoder(httpRequest.uri())
                    .parameters()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0))));

            if(isHandshakeRequest(httpServerRequest))
                handleHandshake(ctx, httpRequest);
        } else if(msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            byte[] chunk = ByteUtil.getBytes(httpContent.content());
            reqBytes = ByteUtil.concat(reqBytes, chunk);
            if(msg instanceof LastHttpContent) {
                handleRequest(httpContent)
                        .doOnNext(httpServerResponse -> {
                            logger.info("{} {}", ctx.channel().remoteAddress(), httpServerResponse.getBody());
                            writeResponse(ctx, httpServerResponse);
                        })
                        .subscribe(new ErrorHandlingSubscriber<>());
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught on {}", ctx.channel().remoteAddress());
        cause.printStackTrace();
        ctx.close();
    }

    private Observable<HttpServerResponse> handleRequest(HttpContent httpContent) {
        return Observable.defer(() -> {
            if(httpContent.decoderResult().isFailure())
                return Observable.error(new WebException(HttpResponseStatus.BAD_REQUEST));

            if(reqBytes.length > 0) {
                try {
                    NodeUtil.isValidJSON(reqBytes);
                } catch(RuntimeException e) {
                    return Observable.error(new WebException(HttpResponseStatus.BAD_REQUEST));
                }

                httpServerRequest.setBody(Optional.of(NodeUtil.getJsonNode(reqBytes)));
            }

            return apiManager.handleRequest(httpServerRequest);
        })
                .onErrorResumeNext(err -> {
                    WebException e = (err instanceof WebException) ? (WebException) err : new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    return Observable.just(e.toHttpServerResponse());
                });
    }

    private void writeResponse(ChannelHandlerContext ctx, HttpServerResponse res) {
        ByteBuf content = Unpooled.copiedBuffer(NodeUtil.writeValueAsBytes(res.getBody()));
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, res.getStatus(), content);
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
        ctx.write(httpResponse);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    private void handleHandshake(ChannelHandlerContext ctx, HttpRequest req) {
        String webSocketUrl = "ws://" + req.headers().get(HttpHeaderNames.HOST) + req.uri();
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(webSocketUrl, null, true);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
        if(handshaker != null) {
            handshaker.handshake(ctx.channel(), req);

            AggregateWebSocketChannelHandlerContext aggregateContext = new AggregateWebSocketChannelHandlerContext();
            aggregateContext.setCtx(ctx);
            aggregateContext.startKeepAliveTimer();
            socketManager.addConnection(aggregateContext);

            ctx.pipeline().remove(HttpRequestDecoder.class);
            ctx.pipeline().remove(HttpResponseEncoder.class);
            ctx.pipeline().remove(HttpContentCompressor.class);
            ctx.pipeline().remove(NettyHttpChannelInboundHandler.class);
            ctx.pipeline().addLast(new NettyWebSocketChannelInboundHandler(apiManager, aggregateContext));
            logger.info("Handshake accepted: {}", ctx.channel().remoteAddress());
        } else {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            logger.error("Handshaker detected unsupported version");
        }
    }

    public boolean isHandshakeRequest(HttpServerRequest request) {
        return request.getMethod() == HttpMethod.GET &&
                request.getHeaders().containsKey(HttpHeaderNames.UPGRADE.toString()) &&
                request.getHeaders().containsKey(HttpHeaderNames.CONNECTION.toString()) &&
                request.getHeaders().containsKey(HttpHeaderNames.SEC_WEBSOCKET_KEY.toString()) &&
                request.getHeaders().containsKey(HttpHeaderNames.SEC_WEBSOCKET_VERSION.toString()) &&
                request.getHeaders().get(HttpHeaderNames.UPGRADE.toString()).equalsIgnoreCase(HttpHeaderValues.WEBSOCKET.toString()) &&
                request.getHeaders().get(HttpHeaderNames.CONNECTION.toString()).equalsIgnoreCase(HttpHeaderValues.UPGRADE.toString());
    }
}
