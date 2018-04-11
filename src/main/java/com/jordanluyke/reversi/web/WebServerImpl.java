package com.jordanluyke.reversi.web;

import com.google.inject.Inject;
import com.jordanluyke.reversi.web.netty.NettyServerInitializer;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebServerImpl implements WebServer {

    private NettyServerInitializer nettyServerInitializer;

    @Inject
    public WebServerImpl(NettyServerInitializer nettyServerInitializer) {
        this.nettyServerInitializer = nettyServerInitializer;
    }

    @Override
    public Observable<Void> start() {
        return this.nettyServerInitializer.initialize();
    }
}
