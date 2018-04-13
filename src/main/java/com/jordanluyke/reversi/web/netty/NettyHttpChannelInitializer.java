package com.jordanluyke.reversi.web.netty;

import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import com.jordanluyke.reversi.web.api.ApiManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameDecoder;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyHttpChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LogManager.getLogger(NettyHttpChannelInitializer.class);

    private Config config;
    private ApiManager apiManager;

    @Inject
    public NettyHttpChannelInitializer(Config config, ApiManager apiManager) {
        this.config = config;
        this.apiManager = apiManager;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        if(config.sslContext != null)
            pipeline.addLast(config.sslContext.newHandler(channel.alloc()));
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new HttpContentCompressor());
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocket13FrameDecoder(false, false, 65536));
        pipeline.addLast(new NettyHttpChannelInboundHandler(apiManager));
    }
}
