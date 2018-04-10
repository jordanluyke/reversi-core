package com.jordanluyke.reversi.web.ws.server;

import com.google.inject.Inject;
import com.jordanluyke.reversi.web.ws.server.netty.NettyWsServerInitializer;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WsServerImpl implements WsServer {

    public NettyWsServerInitializer nettyWsServerInitializer;

    @Inject
    public WsServerImpl(NettyWsServerInitializer nettyWsServerInitializer) {
        this.nettyWsServerInitializer = nettyWsServerInitializer;
    }

    @Override
    public Observable<Void> start() {
        return nettyWsServerInitializer.initialize();
    }
}
