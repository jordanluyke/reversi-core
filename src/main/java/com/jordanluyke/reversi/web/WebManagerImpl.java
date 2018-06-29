package com.jordanluyke.reversi.web;

import com.google.inject.Inject;
import com.jordanluyke.reversi.web.netty.NettyServerInitializer;
import lombok.AllArgsConstructor;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class WebManagerImpl implements WebManager {

    private NettyServerInitializer nettyServerInitializer;

    @Override
    public Observable<Void> start() {
        return nettyServerInitializer.initialize();
    }
}
