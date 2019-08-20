package com.jordanluyke.reversi.web.netty;

import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.reactivex.Completable;
import io.reactivex.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyServerInitializer {
    private static final Logger logger = LogManager.getLogger(NettyServerInitializer.class);

    private Config config;
    private NettyHttpChannelInitializer nettyHttpChannelInitializer;

    @Inject
    public NettyServerInitializer(Config config, NettyHttpChannelInitializer nettyHttpChannelInitializer) {
        this.config = config;
        this.nettyHttpChannelInitializer = nettyHttpChannelInitializer;
    }

    public Completable initialize() {
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
