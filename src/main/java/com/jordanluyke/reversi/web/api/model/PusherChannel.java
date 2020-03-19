package com.jordanluyke.reversi.web.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@AllArgsConstructor
public enum PusherChannel {
    Account("account"),
    Match("match"),
    FindMatch("find-match"),
    Lobbies("lobbies"),
    Lobby("lobby"),
    Users("presence-users");

    private String channelName;
}
