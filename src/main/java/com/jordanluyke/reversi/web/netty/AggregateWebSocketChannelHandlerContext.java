package com.jordanluyke.reversi.web.netty;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.util.WebSocketUtil;
import com.jordanluyke.reversi.web.api.events.OutgoingEvents;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscription;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
public class AggregateWebSocketChannelHandlerContext {
    private static final Logger logger = LogManager.getLogger(AggregateWebSocketChannelHandlerContext.class);

    private ChannelHandlerContext ctx;
    private Subscription keepAliveSubscription;
    private Map<String, Subscription> responsesAwaitingReceipt = new HashMap<>();

    public AggregateWebSocketChannelHandlerContext(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public void close() {
        ctx.write(new CloseWebSocketFrame());
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
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

    public void subscribeMessageReceipt(WebSocketServerResponse res) {
        responsesAwaitingReceipt.put(res.getBody().get("receiptId").asText(), Observable.just(res)
                .switchMap(Void -> Observable.timer(5, TimeUnit.SECONDS))
                .doOnNext(Void -> close())
                .subscribe(new ErrorHandlingSubscriber<>()));
    }

    public void unsubscribeMessageReceipt(String id) {
        Subscription s = responsesAwaitingReceipt.get(id);
        if(s != null)
            s.unsubscribe();
        else
            logger.error("Receipt not found: {}", id);
    }

    public void startKeepAliveTimer() {
        keepAliveSubscription = Observable.interval(15, 15, TimeUnit.SECONDS)
                .doOnNext(Void -> {
                    ObjectNode body = NodeUtil.mapper.createObjectNode()
                            .put("time", Instant.now().toEpochMilli());
                    WebSocketUtil.writeResponse(ctx, new WebSocketServerResponse(OutgoingEvents.KeepAlive, body));
                })
                .subscribe(new ErrorHandlingSubscriber<>());
    }
}
