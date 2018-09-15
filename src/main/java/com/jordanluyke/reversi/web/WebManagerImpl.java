package com.jordanluyke.reversi.web;

import com.google.inject.Inject;
import com.jordanluyke.reversi.web.netty.NettyServerInitializer;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class WebManagerImpl implements WebManager {
    private static final Logger logger = LogManager.getLogger(WebManager.class);

    private NettyServerInitializer nettyServerInitializer;

    @Override
    public Observable<Void> start() {
        return nettyServerInitializer.initialize();
    }
}
