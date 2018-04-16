package com.jordanluyke.reversi.web.netty;

import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.web.api.events.SystemEvents;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.util.concurrent.TimeUnit;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebSocketAggregateContext {
    private static final Logger logger = LogManager.getLogger(WebSocketAggregateContext.class);

    private ChannelHandlerContext ctx;
    public Subject<Void, Void> onKeepAlive = PublishSubject.create();
    private Subscription keepAliveSubscription;

    public WebSocketAggregateContext(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        startKeepAliveTimer();
    }

    public void close() {
        ctx.write(new CloseWebSocketFrame());
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        onKeepAlive.onCompleted();
        keepAliveSubscription.unsubscribe();
        logger.info("Socket closed: {}", ctx.channel().remoteAddress());
    }

    private void startKeepAliveTimer() {
        onKeepAlive.onNext(null);

        keepAliveSubscription = onKeepAlive
                .switchMap(Void -> Observable.timer(5, TimeUnit.SECONDS))
                .doOnNext(Void -> {
                    logger.info("{} timeout", SystemEvents.KeepAlive.class.getSimpleName());
                    close();
                })
                .subscribe(new ErrorHandlingSubscriber<>());
    }
}
