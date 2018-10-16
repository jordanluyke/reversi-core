package com.jordanluyke.reversi.account.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore private String ownerId;
    @JsonIgnore private Instant createdAt;
    @JsonIgnore private Instant updatedAt;
    private int matches;

    public static PlayerStats fromRecord(PlayerStatsRecord record) {
        return new PlayerStats(record.getOwnerid(), record.getCreatedat(), record.getUpdatedat(), record.getMatches());
    }
}
