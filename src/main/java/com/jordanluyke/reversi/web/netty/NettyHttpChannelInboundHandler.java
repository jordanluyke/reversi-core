package com.jordanluyke.reversi.web.netty;

import com.jordanluyke.reversi.Config;
import com.jordanluyke.reversi.util.ErrorHandlingSingleObserver;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.ApiManager;
import com.jordanluyke.reversi.web.api.SocketManager;
import com.jordanluyke.reversi.web.model.WebSocketConnection;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import com.jordanluyke.reversi.web.model.HttpServerResponse;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.reactivex.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyHttpChannelInboundHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static final Logger logger = LogManager.getLogger(NettyHttpChannelInboundHandler.class);

    private ApiManager apiManager;
    private SocketManager socketManager;
    private Config config;

    private ByteBuf reqBuf = Unpooled.buffer();
    private HttpServerRequest httpServerRequest = new HttpServerRequest();

    public NettyHttpChannelInboundHandler(ApiManager apiManager, SocketManager socketManager, Config config) {
        this.apiManager = apiManager;
        this.socketManager = socketManager;
        this.config = config;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
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
            reqBuf = Unpooled.copiedBuffer(reqBuf, httpContent.content());
            if(msg instanceof LastHttpContent) {
                logger.info("HttpRequest: {} {} {}", ctx.channel().remoteAddress(), httpServerRequest.getMethod(), httpServerRequest.getPath());
                handleRequest(httpContent)
                        .doOnSuccess(res -> {
                            writeResponse(ctx, res);
                            reqBuf.release();
                            logger.info("HttpResponse: {} {}", ctx.channel().remoteAddress(), res.getBody());
                        })
                        .subscribe(new ErrorHandlingSingleObserver<>());
            }
        }
    }

//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        logger.info("channelReadComplete");
////        ctx.close();
//        super.channelReadComplete(ctx);
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught on {}", ctx.channel().remoteAddress());
        cause.printStackTrace();
        ctx.close();
    }

    private Single<HttpServerResponse> handleRequest(HttpContent httpContent) {
        return Single.defer(() -> {
            if(httpContent.decoderResult().isFailure())
                return Single.error(new WebException(HttpResponseStatus.BAD_REQUEST));

            if(reqBuf.readableBytes() > 0) {
                try {
                    NodeUtil.isValidJSON(reqBuf.array());
                } catch(RuntimeException e) {
                    return Single.error(new WebException(HttpResponseStatus.BAD_REQUEST));
                }

                httpServerRequest.setBody(Optional.of(NodeUtil.getJsonNode(reqBuf.array())));
            }

            return apiManager.handleRequest(httpServerRequest);
        })
                .onErrorResumeNext(err -> {
                    WebException e = (err instanceof WebException) ? (WebException) err : new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    return Single.just(e.toHttpServerResponse());
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
        String protocol = config.getSslContext().isPresent() ? "wss://" : "ws://";
        String webSocketUrl = protocol + req.headers().get(HttpHeaderNames.HOST) + req.uri();
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(webSocketUrl, null, true);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
        if(handshaker != null) {
            handshaker.handshake(ctx.channel(), req);
            WebSocketConnection connection = new WebSocketConnection(ctx);
            socketManager.addConnection(connection);
            ctx.pipeline().removeLast();
            ctx.pipeline().addLast(new NettyWebSocketChannelInboundHandler(apiManager, connection));
        } else {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            logger.error("Handshaker detected unsupported version");
        }
    }

    private boolean isHandshakeRequest(HttpServerRequest request) {
        return request.getMethod() == HttpMethod.GET &&
                request.getHeaders().containsKey(HttpHeaderNames.UPGRADE.toString()) &&
                request.getHeaders().containsKey(HttpHeaderNames.CONNECTION.toString()) &&
                request.getHeaders().containsKey(HttpHeaderNames.SEC_WEBSOCKET_KEY.toString()) &&
                request.getHeaders().containsKey(HttpHeaderNames.SEC_WEBSOCKET_VERSION.toString()) &&
                request.getHeaders().get(HttpHeaderNames.UPGRADE.toString()).equalsIgnoreCase(HttpHeaderValues.WEBSOCKET.toString()) &&
                request.getHeaders().get(HttpHeaderNames.CONNECTION.toString()).equalsIgnoreCase(HttpHeaderValues.UPGRADE.toString());
    }
}
