package com.jordanluyke.reversi.web.netty;

import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import com.jordanluyke.reversi.web.api.ApiManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class NettyHttpChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LogManager.getLogger(NettyHttpChannelInitializer.class);

    private Config config;
    private ApiManager apiManager;

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        config.getSslContext().ifPresent(sslContext -> pipeline.addLast(sslContext.newHandler(channel.alloc())));
//        pipeline.addLast(new JdkZlibEncoder());
//        pipeline.addLast(new JdkZlibDecoder());
        pipeline.addLast(new HttpServerCodec())
                .addLast(new HttpContentCompressor())
                .addLast(new NettyHttpChannelInboundHandler(apiManager));
    }
}
