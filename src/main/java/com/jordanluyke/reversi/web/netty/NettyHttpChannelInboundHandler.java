package com.jordanluyke.reversi.web.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.primitives.Bytes;
import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.util.WebSocketUtil;
import com.jordanluyke.reversi.web.api.ApiManager;
import com.jordanluyke.reversi.web.model.ServerRequest;
import com.jordanluyke.reversi.web.model.ServerResponse;
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
import java.util.stream.Collectors;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyHttpChannelInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(NettyHttpChannelInboundHandler.class);

    private ApiManager apiManager;
    private WebSocketServerHandshaker handshaker;

    private byte[] content = new byte[0];
    private ServerRequest serverRequest = new ServerRequest();

    public NettyHttpChannelInboundHandler(ApiManager apiManager) {
        this.apiManager = apiManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            URI uri = new URI(httpRequest.uri());

            serverRequest.setPath(uri.getRawPath());
            serverRequest.setMethod(httpRequest.method());
            serverRequest.setHeaders(httpRequest.headers()
                    .entries()
                    .stream()
                    .collect(Collectors.toMap(key -> key.getKey().toLowerCase(), Map.Entry::getValue)));
            serverRequest.setQueryParams(new QueryStringDecoder(httpRequest.uri())
                    .parameters()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0))));

            if(WebSocketUtil.isHandshakeRequest(serverRequest)) {
                handleHandshake(ctx, httpRequest);
                ctx.pipeline().remove(this);
                ctx.pipeline().addLast(new NettyWebSocketChannelInboundHandler(apiManager, handshaker));
            }
        } else if(msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            byte[] chunk = new byte[httpContent.content().readableBytes()];
            httpContent.content().readBytes(chunk);
            content = Bytes.concat(content, chunk);

            if(msg instanceof LastHttpContent) {
                handleRequest(httpContent, content)
                        .doOnNext(serverResponse -> writeResponse(ctx, serverResponse))
                        .subscribe(new ErrorHandlingSubscriber<>());
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getStackTrace());
        ctx.close();
    }

    private Observable<ServerResponse> handleRequest(HttpContent httpContent, byte[] content) {
        if(httpContent.decoderResult().isFailure()) {
            ServerResponse response = new ServerResponse();
            response.setStatus(HttpResponseStatus.BAD_REQUEST);
            ObjectNode body = new ObjectMapper().createObjectNode();
            body.put("exceptionType", "BadRequestException");
            body.put("message", "Unable to decode request");
            body.put("exceptionId", RandomUtil.generateRandom(8));
            response.setBody(body);
            return Observable.just(response);
        }

        if(!NodeUtil.isValidJSON(content)) {
            ServerResponse response = new ServerResponse();
            response.setStatus(HttpResponseStatus.BAD_REQUEST);
            ObjectNode body = new ObjectMapper().createObjectNode();
            body.put("exceptionType", "JsonProcessingException");
            body.put("message", "Not valid JSON");
            body.put("exceptionId", RandomUtil.generateRandom(8));
            response.setBody(body);
            return Observable.just(response);
        }

        serverRequest.setBody(NodeUtil.getJsonNode(content));

        return apiManager.handleHttpRequest(serverRequest);
    }

    private void writeResponse(ChannelHandlerContext ctx, ServerResponse res) {
        ByteBuf content = Unpooled.copiedBuffer(NodeUtil.writeValueAsBytes(res.getBody()));
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, res.getStatus(), content);
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
//        res.getHeaders().forEach((key, value) -> httpResponse.headers().set(key, value));
        ctx.write(httpResponse);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    private void handleHandshake(ChannelHandlerContext ctx, HttpRequest req) {
        String webSocketUrl = "ws://" + req.headers().get(HttpHeaderNames.HOST) + req.uri();
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(webSocketUrl, null, true);
        handshaker = wsFactory.newHandshaker(req);
        if(handshaker == null)
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        else
            handshaker.handshake(ctx.channel(), req);
    }
}
