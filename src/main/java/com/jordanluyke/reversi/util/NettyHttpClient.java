package com.jordanluyke.reversi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import rx.Emitter;
import rx.Observable;

import javax.net.ssl.SSLException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyHttpClient {

    public Observable<ClientResponse> get(String url) {
        return get(url, Collections.emptyMap());
    }

    public Observable<ClientResponse> get(String url, Map<String, Object> params) {
        return get(url, params, Collections.emptyMap());
    }

    public Observable<ClientResponse> get(String url, Map<String, Object> params, Map<String, String> headers) {
        return get(url, params, headers, HttpHeaderValues.APPLICATION_JSON.toString());
    }

    public Observable<ClientResponse> get(String url, Map<String, Object> params, Map<String, String> headers, String contentType) {
        if(params.size() > 0)
            url += "?" + toQuerystring(params);
        return request(url, HttpMethod.GET, new byte[0], headers, contentType);
    }

    public Observable<ClientResponse> post(String url) {
        return post(url, Collections.emptyMap());
    }

    public Observable<ClientResponse> post(String url, Map<String, Object> body) {
        return post(url, body, Collections.emptyMap());
    }

    public Observable<ClientResponse> post(String url, Map<String, Object> body, Map<String, String> headers) {
        return post(url, body, headers, HttpHeaderValues.APPLICATION_JSON.toString());
    }

    public Observable<ClientResponse> post(String url, Map<String, Object> body, Map<String, String> headers, String contentType) {
        return request(url, HttpMethod.POST, bodyToBytes(body, contentType), headers, contentType);
    }

    public Observable<ClientResponse> put(String url) {
        return put(url, Collections.emptyMap());
    }

    public Observable<ClientResponse> put(String url, Map<String, Object> body) {
        return put(url, body, Collections.emptyMap());
    }

    public Observable<ClientResponse> put(String url, Map<String, Object> body, Map<String, String> headers) {
        return put(url, body, headers, HttpHeaderValues.APPLICATION_JSON.toString());
    }

    public Observable<ClientResponse> put(String url, Map<String, Object> body, Map<String, String> headers, String contentType) {
        return request(url, HttpMethod.PUT, bodyToBytes(body, contentType), headers, contentType);
    }

    public Observable<ClientResponse> delete(String url) {
        return delete(url, Collections.emptyMap());
    }

    public Observable<ClientResponse> delete(String url, Map<String, Object> body) {
        return delete(url, body, Collections.emptyMap());
    }

    public Observable<ClientResponse> delete(String url, Map<String, Object> body, Map<String, String> headers) {
        return delete(url, body, headers, HttpHeaderValues.APPLICATION_JSON.toString());
    }

    public Observable<ClientResponse> delete(String url, Map<String, Object> body, Map<String, String> headers, String contentType) {
        return request(url, HttpMethod.DELETE, bodyToBytes(body, contentType), headers, contentType);
    }

    public Observable<ClientResponse> request(String url, HttpMethod method, byte[] body, Map<String, String> headers, String contentType) {
        URI uri;
        try {
            URI u = new URI(url);
            uri = new URI(u.getScheme(),
                    null,
                    u.getHost(),
                    HttpScheme.HTTPS.name().toString().equals(u.getScheme()) ? HttpScheme.HTTPS.port() : HttpScheme.HTTP.port(),
                    u.getPath(),
                    u.getQuery(),
                    null);
        } catch(URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        }

        SslContext sslCtx;
        try {
            if(uri.getPort() == HttpScheme.HTTPS.port())
                sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            else
                sslCtx = null;
        } catch (SSLException e) {
            throw new RuntimeException(e.getMessage());
        }

        EventLoopGroup group = new NioEventLoopGroup();
        ClientResponse res = new ClientResponse();
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        if(sslCtx != null)
                            pipeline.addLast(sslCtx.newHandler(channel.alloc()));
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpContentDecompressor());
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            ByteBuf data = Unpooled.buffer();

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                if(msg instanceof HttpResponse) {
                                    HttpResponse response = (HttpResponse) msg;

                                    res.setStatusCode(response.status().code());
                                    res.setHeaders(response.headers()
                                            .entries()
                                            .stream()
                                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                                } else if(msg instanceof HttpContent) {
                                    HttpContent content = (HttpContent) msg;
                                    data = Unpooled.copiedBuffer(data, content.content());

                                    if(content instanceof LastHttpContent) {
                                        res.setRawBody(data.array());
                                        ctx.close();
                                    }
                                }
                            }
                        });
                    }
                });

        return channelFutureToObservable(bootstrap.connect(uri.getHost(), uri.getPort()))
                .flatMap(channel -> {
                    String path = uri.getRawPath();
                    if(uri.getQuery() != null)
                        path += "?" + uri.getQuery();
                    ByteBuf content = Unpooled.copiedBuffer(body);
                    HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, path, content);
                    request.headers().set(HttpHeaderNames.HOST, uri.getHost());
                    request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                    request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
                    request.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
                    request.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
                    headers.forEach((key, value) -> request.headers().set(key, value));
                    return channelFutureToObservable(channel.writeAndFlush(request));
                })
                .flatMap(channel -> channelFutureToObservable(channel.closeFuture()))
                .doOnNext(Void -> group.shutdownGracefully())
                .map(Void -> {
                    res.validate();
                    return res;
                });
    }

    private Observable<Channel> channelFutureToObservable(ChannelFuture channelFuture) {
        return Observable.create(observer -> {
            channelFuture.addListener(future -> {
                if(future.isSuccess()) {
                    observer.onNext(channelFuture.channel());
                    observer.onCompleted();
                } else
                    observer.onError(future.cause());
            });
        }, Emitter.BackpressureMode.BUFFER);
    }

    private byte[] bodyToBytes(Map<String, Object> body, String contentType) {
        if(body.size() == 0)
            return new byte[0];
        if(contentType.equals(HttpHeaderValues.APPLICATION_JSON.toString())) {
            try {
                return new ObjectMapper().writeValueAsBytes(body);
            } catch(JsonProcessingException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return toQuerystring(body).getBytes();
    }

    private String toQuerystring(Map<String, Object> params) {
        return params.entrySet()
                .stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue().toString()))
                .collect(Collectors.joining("&"));
    }

    private String encode(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    class ClientResponse {

        private int statusCode;
        private byte[] rawBody;
        private Map<String, String> headers = new HashMap<>();

        public ClientResponse() {
        }

        public void validate() {
            if(rawBody == null)
                throw new RuntimeException("body is null");
            if(statusCode == -1)
                throw new RuntimeException("statusCode is null");
        }

        public String getBody() {
            return new String(rawBody, StandardCharsets.UTF_8);
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public void setRawBody(byte[] rawBody) {
            this.rawBody = rawBody;
        }
    }
}
