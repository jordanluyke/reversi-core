package com.jordanluyke.reversi.web.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.primitives.Bytes;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.api.ApiManager;
import com.jordanluyke.reversi.web.model.ServerRequest;
import com.jordanluyke.reversi.web.model.ServerResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyHttpChannelInboundHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LogManager.getLogger(NettyHttpChannelInboundHandler.class);

    private ApiManager apiManager;

    private byte[] content = new byte[0];
    private ServerRequest serverRequest = new ServerRequest();

    public NettyHttpChannelInboundHandler(ApiManager apiManager) {
        this.apiManager = apiManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("channel read {}", msg.getClass().getSimpleName());
        if(msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            URI uri = new URI(httpRequest.uri());

            serverRequest.setPath(uri.getRawPath());
            serverRequest.setMethod(httpRequest.method());
            serverRequest.setHeaders(httpRequest.headers()
                    .entries()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            serverRequest.setQueryParams(new QueryStringDecoder(httpRequest.uri())
                    .parameters()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0))));
        } else if(msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            byte[] chunk = new byte[httpContent.content().readableBytes()];
            httpContent.content().readBytes(chunk);
            content = Bytes.concat(content, chunk);

            if (msg instanceof LastHttpContent) {
                handleRequest(httpContent, content)
                        .subscribe(serverResponse -> {
                            writeResponse(ctx, serverResponse);
                        }, err -> {
                            System.err.println("Error: " + err.getMessage());
                        });
            }
        }
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

    private void writeResponse(ChannelHandlerContext ctx, ServerResponse serverResponse) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, serverResponse.getStatus(), Unpooled.copiedBuffer(NodeUtil.writeValueAsBytes(serverResponse.getBody())));
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
        ctx.write(httpResponse);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
