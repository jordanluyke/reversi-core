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

    @Override
    public boolean equals(Object o) {
        if(o instanceof EventSubscription) {
            EventSubscription e = (EventSubscription) o;
            return event == e.event && channel.equals(e.channel);
        }
        return false;
    }
}
