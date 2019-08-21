package com.jordanluyke.reversi.web.api.model;

import com.jordanluyke.reversi.web.api.events.SocketEvent;
import io.reactivex.disposables.Disposable;
import lombok.*;

import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventSubscription {

    private SocketEvent event;
    private String channel;
    private Optional<Disposable> disposable = Optional.empty();

    @Override
    public boolean equals(Object e) {
        if(e instanceof EventSubscription)
            return event == ((EventSubscription) e).event && channel.equals(((EventSubscription) e).channel);
        return false;
    }
}
