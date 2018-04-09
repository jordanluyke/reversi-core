package com.jordanluyke.reversi.web.http.server.netty;

import com.jordanluyke.reversi.web.Config;
import com.jordanluyke.reversi.web.http.api.ApiManager;
import com.jordanluyke.reversi.web.http.server.HttpServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyHttpChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LogManager.getLogger(HttpServer.class);

    private Config config;
    private ApiManager apiManager;

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
        pipeline.addLast(new NettyHttpChannelInboundHandler(apiManager));
    }
}
