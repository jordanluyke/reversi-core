package com.jordanluyke.reversi.web.model;

import com.jordanluyke.reversi.util.ErrorHandlingObserver;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.events.SocketEvent;
import com.jordanluyke.reversi.web.api.model.EventSubscription;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

    public void send(WebSocketServerResponse res) {
        subscribeReceipt(res);
        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(Unpooled.copiedBuffer(NodeUtil.writeValueAsBytes(res.toNode())));
        ctx.write(frame);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    public void close() {
        ctx.write(new CloseWebSocketFrame());
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        ctx.close();
        onClose.onComplete();
        disposeAllSubscriptions();
        logger.info("Socket closed: {}", ctx.channel().remoteAddress());
    }

    public void subscribeReceipt(WebSocketServerResponse res) {
        ErrorHandlingObserver<Long> observer = new ErrorHandlingObserver<>();
        Disposable disposable = Observable.timer(2, TimeUnit.SECONDS)
                .doOnNext(Void -> send(res))
                .subscribe(observer::onNext, observer::onError);
        responsesAwaitingReceipt.put(res.getReceiptId(), disposable);
    }

    public void unsubscribeReceipt(String id) {
        Disposable sub = responsesAwaitingReceipt.get(id);
        if(sub != null) {
            sub.dispose();
            responsesAwaitingReceipt.remove(id);
        } else {
            logger.error("Receipt not found: {}", id);
        }
    }

    public Completable handleSubscriptionRequest(WebSocketServerRequest req, boolean channelRequiredOnSubscribe, Optional<Single<WebSocketServerResponse>> sub) {
        return Completable.defer(() -> {
            Optional<String> event = NodeUtil.get("event", req.getBody());
            Optional<String> channel = NodeUtil.get("channel", req.getBody());
            Optional<Boolean> unsubscribe = NodeUtil.getBoolean("unsubscribe", req.getBody());

            if(!event.isPresent())
                return Completable.error(new FieldRequiredException("event"));
            if(channelRequiredOnSubscribe && !unsubscribe.isPresent() && !channel.isPresent())
                return Completable.error(new FieldRequiredException("channel"));

            SocketEvent e;
            try {
                e = SocketEvent.valueOf(event.get());
            } catch(Exception err) {
                return Completable.error(new WebException(HttpResponseStatus.NOT_FOUND));
            }

            if(unsubscribe.isPresent() && unsubscribe.get())
                req.getConnection().removeEventSubscription(e);
            else {
                req.getConnection().addEventSubscription(e, channel, sub.map(s -> s
                        .doOnSuccess(Void -> req.getConnection().removeEventSubscription(e))
                        .subscribe(Void -> {}, err -> logger.error("{}", err))));
            }
            return Completable.complete();
        });
    }

    public Completable handleSubscriptionRequest(WebSocketServerRequest req, boolean channelRequiredOnSubscribe) {
        return handleSubscriptionRequest(req, channelRequiredOnSubscribe, Optional.empty());
    }

    private void addEventSubscription(SocketEvent event, Optional<String> channel, Optional<Disposable> disposable) {
        EventSubscription eventSubscription = new EventSubscription(event, channel, disposable);
        if(eventSubscriptions.containsKey(event))
            logger.error("EventSubscription {} already exists", event);
        else
            eventSubscriptions.put(event, eventSubscription);
    }

    private void removeEventSubscription(SocketEvent event) {
        if(eventSubscriptions.containsKey(event)) {
            eventSubscriptions.get(event).getDisposable().ifPresent(Disposable::dispose);
            eventSubscriptions.remove(event);
        }
    }

    private void disposeAllSubscriptions() {
        eventSubscriptions.values()
                .forEach(eventSubscription -> eventSubscription.getDisposable().ifPresent(Disposable::dispose));
        responsesAwaitingReceipt.forEach((key, value) -> value.dispose());
    }
}
