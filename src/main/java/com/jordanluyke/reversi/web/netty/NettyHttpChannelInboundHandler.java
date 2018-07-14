package com.jordanluyke.reversi.web.netty;

import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.ApiManager;
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
    private ByteBuf reqBuf = Unpooled.buffer();
    private HttpServerRequest httpServerRequest = new HttpServerRequest();

    public NettyHttpChannelInboundHandler(ApiManager apiManager) {
        this.apiManager = apiManager;
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
            reqBuf = Unpooled.copiedBuffer(reqBuf, httpContent.content());

            if(msg instanceof LastHttpContent) {
                handleRequest(httpContent, reqBuf)
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

    private Observable<HttpServerResponse> handleRequest(HttpContent httpContent, ByteBuf content) {
        if(httpContent.decoderResult().isFailure())
            return Observable.just(new WebException(HttpResponseStatus.BAD_REQUEST).toHttpServerResponse());

        if(content.readableBytes() > 0) {
            try {
                NodeUtil.isValidJSON(content.array());
            } catch(RuntimeException e) {
                return Observable.just(new WebException(HttpResponseStatus.BAD_REQUEST).toHttpServerResponse());
            }

            httpServerRequest.setBody(Optional.of(NodeUtil.getJsonNode(content.array())));
        }

        return apiManager.handleRequest(httpServerRequest);
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
            ctx.pipeline().remove(this);
            ctx.pipeline().addLast(new NettyWebSocketChannelInboundHandler(apiManager, ctx));
            logger.info("Handshake accepted: {}", ctx.channel().remoteAddress());
        } else {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            logger.error("Handshaker detected supported version");
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
