package com.jordanluyke.reversi.session.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.jooq.sources.tables.records.SessionRecord;

import java.time.Instant;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Session {

    @JsonProperty("sessionId") private String id;
    @JsonProperty("accountId") private String ownerId;
    @JsonIgnore private Instant createdAt;
    @JsonIgnore private Instant updatedAt;
    private Optional<Instant> expiresAt;

    public static Session fromRecord(SessionRecord record) {
        return new Session(record.getId(), record.getOwnerid(), record.getCreatedat(), record.getUpdatedat(), Optional.ofNullable(record.getExpiresat()));
    }
}
