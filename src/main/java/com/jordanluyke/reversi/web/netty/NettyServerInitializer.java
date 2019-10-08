package com.jordanluyke.reversi.web.netty;

import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class NettyServerInitializer {
    private static final Logger logger = LogManager.getLogger(NettyServerInitializer.class);

    private Config config;
    private NettyHttpChannelInitializer nettyHttpChannelInitializer;

    public Completable init() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(nettyHttpChannelInitializer);

        return getChannel(bootstrap.bind(config.getPort()))
                .doOnSuccess(Void -> logger.info("Listening on port {}", config.getPort()))
                .flatMap(channel -> getChannel(channel.closeFuture()))
                .flatMapCompletable(Void -> Completable.complete());
    }

    private static Single<Channel> getChannel(ChannelFuture channelFuture) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        channelFuture.addListener((ChannelFuture future) -> {
            if(future.isSuccess())
                completableFuture.complete(future.channel());
            else
                completableFuture.completeExceptionally(future.cause());
        });
        return Single.fromFuture(completableFuture);
    }
}
