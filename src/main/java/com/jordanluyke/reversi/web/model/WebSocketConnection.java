package com.jordanluyke.reversi.web.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.util.ErrorHandlingObserver;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.api.events.OutgoingEvents;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private List<EventSubscription> eventSubscriptions = new ArrayList<>();
    private Map<String, Disposable> responsesAwaitingReceipt = new HashMap<>();

    public WebSocketConnection(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public void close() {
        ctx.write(new CloseWebSocketFrame());
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        onClose.onComplete();
        ctx.close();
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

    public void addEventSubscription(OutgoingEvents event, String channel) {
        EventSubscription eventSubscription = new EventSubscription(event, channel);
        if(!eventSubscriptions.contains(eventSubscription))
            eventSubscriptions.add(eventSubscription);
    }

    public void removeEventSubscription(OutgoingEvents event) {
        eventSubscriptions = eventSubscriptions.stream()
                .filter(sub -> sub.getEvent() != event)
                .collect(Collectors.toList());
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