package com.jordanluyke.reversi.web.ws.server.netty;

import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyWsChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Config config;

    @Inject
    public NettyWsChannelInitializer(Config config) {
        this.config = config;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        if(config.sslContext != null)
            pipeline.addLast(config.sslContext.newHandler(channel.alloc()));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast(new NettyWsChannelInboundHandler());
    }
}
