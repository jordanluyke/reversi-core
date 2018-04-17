package com.jordanluyke.reversi.web.netty;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.api.events.SystemEvents;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class WebSocketAggregateContext {
    private static final Logger logger = LogManager.getLogger(WebSocketAggregateContext.class);

    public Subject<Void, Void> onKeepAlive = PublishSubject.create();
    private ChannelHandlerContext ctx;
    private Subscription keepAliveSubscription;
    private Map<String, Subscription> responsesAwaitingReceipt = new HashMap<>();

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

    public WebSocketServerResponse markReceiptRequired(ObjectNode body) {
        body.put("receiptRequired", true);
        body.put("receiptId", RandomUtil.generateRandom(6));
        WebSocketServerResponse res = new WebSocketServerResponse();
        res.setBody(body);
        return res;
    }

    public void registerReceiptRequired(WebSocketServerResponse res) {
        responsesAwaitingReceipt.put(res.getBody().get("receiptId").asText(), Observable.just(res)
                .switchMap(Void -> Observable.timer(5, TimeUnit.SECONDS))
                .doOnNext(Void -> close())
                .subscribe(new ErrorHandlingSubscriber<>()));
    }

    public void onMessageReceiptReceived(String id) {
        Subscription s = responsesAwaitingReceipt.get(id);
        if(s != null)
            s.unsubscribe();
        else
            logger.error("Receipt not found: {}", id);
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
