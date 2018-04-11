package com.jordanluyke.reversi.web.netty;

import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import com.jordanluyke.reversi.web.api.ApiManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Emitter;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NettyServerInitializer {
    private static final Logger logger = LogManager.getLogger(NettyServerInitializer.class);

    private Config config;
    private ApiManager apiManager;

    @Inject
    public NettyServerInitializer(Config config, ApiManager apiManager) {
        this.config = config;
        this.apiManager = apiManager;
    }

    public Observable<Void> initialize() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new NettyChannelInitializer(config, apiManager));

        return channelFutureToObservable(bootstrap.bind(config.port))
                .doOnNext(Void -> logger.info("Listening on port {}", config.port))
                .flatMap(channel -> channelFutureToObservable(channel.closeFuture()))
                .ignoreElements()
                .cast(Void.class);
    }

    private Observable<Channel> channelFutureToObservable(ChannelFuture channelFuture) {
        return Observable.create(observer -> channelFuture
                .addListener(future -> {
                    if(future.isSuccess()) {
                        observer.onNext(channelFuture.channel());
                        observer.onCompleted();
                    } else
                        observer.onError(future.cause());
                }), Emitter.BackpressureMode.BUFFER);
    }
}
