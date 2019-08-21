package com.jordanluyke.reversi.web.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.util.ErrorHandlingObserver;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.api.events.SocketEvent;
import com.jordanluyke.reversi.web.api.model.EventSubscription;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
public class WebSocketConnection {
    private static final Logger logger = LogManager.getLogger(WebSocketConnection.class);

    private ChannelHandlerContext ctx;
    private PublishSubject<Void> onClose = PublishSubject.create();
    private Map<SocketEvent, EventSubscription> eventSubscriptions = new HashMap<>();
    private Map<String, Disposable> responsesAwaitingReceipt = new HashMap<>();

    public WebSocketConnection(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        logger.info("Socket opened: {}", ctx.channel().remoteAddress());
    }

    public void close() {
        ctx.write(new CloseWebSocketFrame());
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        onClose.onComplete();
        ctx.close();
        disposeAllEventSubscriptions();
        logger.info("Socket closed: {}", ctx.channel().remoteAddress());
    }

    public WebSocketServerResponse markReceiptRequired(ObjectNode body) {
        body.put("receiptRequired", true);
        body.put("receiptId", RandomUtil.generateRandom(6));
        WebSocketServerResponse res = new WebSocketServerResponse();
        res.setBody(body);
        return res;
    }

    public void subscribeMessageReceipt(WebSocketServerResponse res) {
        ErrorHandlingObserver<Long> observer = new ErrorHandlingObserver<>();
        responsesAwaitingReceipt.put(res.getBody().get("receiptId").asText(), Observable.just(res)
                .switchMap(Void -> Observable.timer(10, TimeUnit.SECONDS))
                .doOnNext(Void -> close())
                .subscribe(observer::onNext, observer::onError));
    }

    public void unsubscribeMessageReceipt(String id) {
        Disposable sub = responsesAwaitingReceipt.get(id);
        if(sub != null)
            sub.dispose();
        else
            logger.error("Receipt not found: {}", id);
    }

    public void addEventSubscription(SocketEvent event, String channel, Optional<Disposable> disposable) {
        EventSubscription eventSubscription = new EventSubscription(event, channel, disposable);
        if(eventSubscriptions.containsKey(event))
            logger.error("EventSubscription {} already exists", event);
        eventSubscriptions.put(event, eventSubscription);
    }

    public void addEventSubscription(SocketEvent event, String channel) {
        addEventSubscription(event, channel, Optional.empty());
    }

    public void removeEventSubscription(SocketEvent event) {
        if(eventSubscriptions.containsKey(event)) {
            eventSubscriptions.get(event).getDisposable().ifPresent(Disposable::dispose);
            eventSubscriptions.remove(event);
        }
    }

    private void disposeAllEventSubscriptions() {
        eventSubscriptions.values()
                .forEach(eventSubscription -> {
                    eventSubscription.getDisposable().ifPresent(Disposable::dispose);
                });
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WebSocketConnection) {
            WebSocketConnection connection = (WebSocketConnection) obj;
            return ctx.channel().remoteAddress().equals(connection.getCtx().channel().remoteAddress());
        }
        return false;
    }
}
