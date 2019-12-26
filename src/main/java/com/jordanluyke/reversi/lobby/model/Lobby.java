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
    private Optional<String> name;
    private String playerIdDark;
    private Optional<String> playerIdLight;
    private Optional<Instant> closedAt;
    private boolean isPrivate = false;
    private boolean playerDarkReady = false;
    private boolean playerLightReady = false;

    public static Lobby fromRecord(LobbyRecord record) {
        return new Lobby(record.getId(), record.getCreatedat(), record.getUpdatedat(), Optional.ofNullable(record.getName()), record.getPlayeriddark(), Optional.ofNullable(record.getPlayeridlight()), Optional.ofNullable(record.getClosedat()), record.getIsprivate(), record.getPlayerdarkready(), record.getPlayerlightready());
    }
}
