package com.jordanluyke.reversi.web.netty;

import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import com.jordanluyke.reversi.web.api.ApiManager;
import com.jordanluyke.reversi.web.api.SocketManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameDecoder;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameEncoder;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
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
    private SocketManager socketManager;

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        config.getSslContext().ifPresent(sslContext -> pipeline.addLast(sslContext.newHandler(channel.alloc())));
//        pipeline.addLast(new JdkZlibEncoder());
//        pipeline.addLast(new JdkZlibDecoder());
        pipeline.addLast(new HttpServerCodec())
                .addLast(new HttpContentCompressor())
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new WebSocket13FrameEncoder(false))
                .addLast(new WebSocket13FrameDecoder(true, true, 65536))
                .addLast(new NettyHttpChannelInboundHandler(apiManager, socketManager, config));
    }
}
