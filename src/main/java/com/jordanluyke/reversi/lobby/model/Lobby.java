package com.jordanluyke.reversi.lobby.model;

import lombok.*;
import org.jooq.sources.tables.records.LobbyRecord;

import java.time.Instant;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Lobby {
    private String id;
    private Instant createdAt;
    private Instant updatedAt;
    private Optional<Instant> startingAt = Optional.empty();
    private Optional<String> name = Optional.empty();
    private String playerIdDark;
    private Optional<String> playerIdLight = Optional.empty();
    private Optional<Instant> closedAt = Optional.empty();
    private boolean isPrivate = false;
    private boolean playerReadyDark = false;
    private boolean playerReadyLight = false;
    private Optional<String> matchId = Optional.empty();

    public static Lobby fromRecord(LobbyRecord record) {
        return new Lobby(record.getId(), record.getCreatedat(), record.getUpdatedat(), Optional.ofNullable(record.getStartingat()), Optional.ofNullable(record.getName()), record.getPlayeriddark(), Optional.ofNullable(record.getPlayeridlight()), Optional.ofNullable(record.getClosedat()), record.getIsprivate(), record.getPlayerreadydark(), record.getPlayerreadylight(), Optional.ofNullable(record.getMatchid()));
    }
}
