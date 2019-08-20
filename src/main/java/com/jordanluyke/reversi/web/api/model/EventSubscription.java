package com.jordanluyke.reversi.web.api.model;

import com.jordanluyke.reversi.web.api.events.OutgoingEvents;
import lombok.*;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventSubscription {

    private OutgoingEvents event;
    private String channel;

    public boolean equals(EventSubscription e) {
        return event == e.event && channel.equals(e.channel);
    }
}
