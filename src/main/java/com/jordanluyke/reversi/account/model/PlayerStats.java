package com.jordanluyke.reversi.account.model;

import lombok.*;
import org.jooq.sources.tables.records.PlayerStatsRecord;

import java.time.Instant;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerStats {

    private String ownerId;
    private Instant createdAt;
    private Instant updatedAt;
    private int matches;

    public static PlayerStats fromRecord(PlayerStatsRecord record) {
        return new PlayerStats(record.getOwnerid(), record.getCreatedat(), record.getUpdatedat(), record.getMatches());
    }
}
