package com.jordanluyke.reversi.web.http.server;

import com.google.inject.Inject;
import com.jordanluyke.reversi.web.http.server.netty.NettyHttpServerInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class HttpServerImpl implements HttpServer {
    private static final Logger logger = LogManager.getLogger(HttpServer.class);

    NettyHttpServerInitializer nettyHttpServerInitializer;

    @Inject
    public HttpServerImpl(NettyHttpServerInitializer nettyHttpServerInitializer) {
        this.nettyHttpServerInitializer = nettyHttpServerInitializer;
    }

    public Observable<Void> start() {
        return nettyHttpServerInitializer.initialize();
    }
}
