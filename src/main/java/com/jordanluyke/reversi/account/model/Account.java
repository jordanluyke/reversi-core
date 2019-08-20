package com.jordanluyke.reversi.account.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.jooq.sources.tables.records.AccountRecord;

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
public class Account {

    protected String id;
    protected Instant createdAt;
    @JsonIgnore protected Instant updatedAt;
    protected Optional<String> name;
    protected Optional<String> facebookUserId;
    protected Optional<String> googleUserId;
    protected boolean guest;

    public static Account fromRecord(AccountRecord record) {
        return new Account(record.getId(), record.getCreatedat(), record.getUpdatedat(), Optional.ofNullable(record.getName()), Optional.ofNullable(record.getFacebookuserid()), Optional.ofNullable(record.getGoogleuserid()), record.getGuest());
    }
}
