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
    private Optional<String> channel;
    private Optional<Disposable> disposable = Optional.empty();
}
